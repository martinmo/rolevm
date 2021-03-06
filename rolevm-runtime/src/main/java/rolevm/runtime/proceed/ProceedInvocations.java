package rolevm.runtime.proceed;

import static java.lang.invoke.MethodHandles.dropArguments;
import static rolevm.runtime.Bootstrap.unwrapMethodName;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.DynamicLinker;
import jdk.dynalink.DynamicLinkerFactory;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.support.Guards;
import rolevm.api.DispatchContext;

/**
 * Provides the factory for different kinds of {@link ProceedInvocation}s as
 * well as the {@link DynamicLinker} implementation that is used to link the
 * nested dynamic call site.
 * 
 * @author Martin Morgenstern
 */
public class ProceedInvocations {
    private final DynamicLinker linker = createDynamicLinker();

    private DynamicLinker createDynamicLinker() {
        DynamicLinkerFactory factory = new DynamicLinkerFactory();
        factory.setPrioritizedLinker(new ProceedLinker());
        factory.setFallbackLinkers();
        return factory.createLinker();
    }

    /**
     * Make an ordinary {@link ProceedInvocation} with the given lookup, name, and
     * method type (intended to be called from an {@code invokedynamic} bootstrap
     * method).
     */
    public ProceedInvocation getInvocation(Lookup lookup, String name, MethodType type) {
        return new ProceedInvocation(linker, lookup, name, type);
    }

    /**
     * Make an {@link AdaptedProceedInvocation} with the given lookup, name, and
     * method type, which discards any leading argument (intended to be called from
     * an {@code invokedynamic} bootstrap method).
     */
    public ProceedInvocation getAdaptedInvocation(Lookup lookup, String name, MethodType type) {
        return new AdaptedProceedInvocation(linker, lookup, name, type);
    }

    /**
     * Component linker for the nested dynamic call sites in {@code proceed}
     * macro-instructions. It simply implements classic receiver-based dispatch and
     * links invocations with simple guards such as {@code instanceof}.
     */
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
            MethodHandle handle = lookup.findVirtual(coreType, unwrapMethodName(descriptor), coreMethodType);
            return new GuardedInvocation(dropArguments(handle, 0, Object.class, DispatchContext.class),
                    Guards.isNull());
        }

        private GuardedInvocation roleOrProceedInvocation(Class<?> receiverType, CallSiteDescriptor descriptor)
                throws IllegalAccessException {
            Lookup lookup = descriptor.getLookup();
            String name = unwrapMethodName(descriptor);
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
