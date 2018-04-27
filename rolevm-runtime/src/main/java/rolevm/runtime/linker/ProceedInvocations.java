package rolevm.runtime.linker;

import static java.lang.invoke.MethodHandles.dropArguments;
import static rolevm.runtime.linker.Utils.createDynamicLinker;
import static rolevm.runtime.linker.Utils.unwrapName;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.DynamicLinker;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.support.Guards;
import rolevm.api.DispatchContext;

public class ProceedInvocations {
    private final DynamicLinker linker = createDynamicLinker(new ProceedLinker());

    public ProceedInvocation getInvocation(Lookup lookup, String name, MethodType type) {
        return new ProceedInvocation(linker, lookup, name, type);
    }

    public ProceedInvocation getAdaptedInvocation(Lookup lookup, String name, MethodType type) {
        return new AdaptedProceedInvocation(linker, lookup, name, type);
    }

    private class ProceedLinker implements GuardingDynamicLinker {
        @Override
        public GuardedInvocation getGuardedInvocation(LinkRequest request, LinkerServices unused) throws Exception {
            CallSiteDescriptor descriptor = request.getCallSiteDescriptor();
            Object receiver = request.getReceiver();
            if (receiver == null) {
                return coreInvocation(descriptor);
            }
            return roleOrProceedInvocation(receiver.getClass(), descriptor);
        }

        private GuardedInvocation coreInvocation(CallSiteDescriptor descriptor) throws ReflectiveOperationException {
            Lookup lookup = descriptor.getLookup();
            Class<?> coreType = descriptor.getMethodType().parameterType(2);
            MethodType coreMethodType = descriptor.getMethodType().dropParameterTypes(0, 3);
            MethodHandle handle = lookup.findVirtual(coreType, unwrapName(descriptor), coreMethodType);
            return new GuardedInvocation(dropArguments(handle, 0, Object.class, DispatchContext.class),
                    Guards.isNull());
        }

        private GuardedInvocation roleOrProceedInvocation(Class<?> receiverType, CallSiteDescriptor descriptor)
                throws IllegalAccessException {
            Lookup lookup = descriptor.getLookup();
            String name = unwrapName(descriptor);
            MethodType type = descriptor.getMethodType();
            MethodHandle guard = Guards.isInstance(receiverType, type);
            try {
                return new GuardedInvocation(lookup.findVirtual(receiverType, name, type.dropParameterTypes(0, 1)),
                        guard);
            } catch (NoSuchMethodException e) {
                return new GuardedInvocation(getAdaptedInvocation(lookup, name, type).getHandle(), guard);
            }
        }
    }
}
