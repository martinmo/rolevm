package rolevm.runtime.linker;

import static java.lang.invoke.MethodHandles.dropArguments;
import static rolevm.runtime.linker.Utils.unwrapName;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.Collections;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.DynamicLinker;
import jdk.dynalink.DynamicLinkerFactory;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.support.Guards;

public class ProceedInvocations {
    private final DynamicLinker linker = initLinker();

    public ProceedInvocation getInvocation(Lookup lookup, String name, MethodType type) {
        return new ProceedInvocation(linker, lookup, name, type);
    }

    /**
     * Initializes the {@link jdk.dynalink} linker which is used to link the nested
     * {@link java.lang.invoke.CallSite}s inside proceed handles.
     */
    private DynamicLinker initLinker() {
        DynamicLinkerFactory factory = new DynamicLinkerFactory();
        factory.setPrioritizedLinker(new ProceedLinker());
        factory.setFallbackLinkers(Collections.emptyList());
        return factory.createLinker();
    }

    private static class ProceedLinker implements GuardingDynamicLinker {
        @Override
        public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices)
                throws Exception {
            CallSiteDescriptor descriptor = linkRequest.getCallSiteDescriptor();
            Object receiver = linkRequest.getReceiver();
            Lookup lookup = descriptor.getLookup();
            if (receiver != null) {
                MethodHandle handle = lookup.findVirtual(receiver.getClass(), unwrapName(descriptor),
                        lookupType(descriptor.getMethodType()));
                return new GuardedInvocation(handle,
                        Guards.isInstance(receiver.getClass(), descriptor.getMethodType()));
            }
            Class<?> coreType = descriptor.getMethodType().parameterType(2);
            MethodType coreMethodType = descriptor.getMethodType().dropParameterTypes(0, 3);
            MethodHandle handle = lookup.findVirtual(coreType, unwrapName(descriptor), coreMethodType);
            return new GuardedInvocation(dropArguments(handle, 0, Object.class, DispatchContext.class),
                    Guards.isNull());
        }

        private static MethodType lookupType(MethodType type) {
            return type.dropParameterTypes(0, 1);
        }
    }
}
