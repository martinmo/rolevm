package rolevm.runtime.linker;

import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.filterArguments;
import static java.lang.invoke.MethodHandles.foldArguments;
import static jdk.dynalink.StandardOperation.CALL;
import static rolevm.runtime.linker.DispatchContext.NEXT_HANDLE;
import static rolevm.runtime.linker.DispatchContext.TARGET_HANDLE;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.Collections;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.DynamicLinker;
import jdk.dynalink.DynamicLinkerFactory;
import jdk.dynalink.NamedOperation;
import jdk.dynalink.Operation;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.support.Guards;
import jdk.dynalink.support.ChainedCallSite;

public class ProceedFactory {
    private final DynamicLinker linker = initLinker();

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

    /**
     * Create a <em>proceed handle</em>.
     * 
     * @param dynamicInvoker
     *            the dynamic invoker of the floating call site
     */
    static MethodHandle proceedHandle(final MethodHandle dynamicInvoker) {
        ensureCorrectType(dynamicInvoker);
        return foldArguments(filterArguments(dynamicInvoker, 1, NEXT_HANDLE), TARGET_HANDLE);
    }

    private static void ensureCorrectType(final MethodHandle handle) {
        MethodType type = handle.type();
        assert type.parameterType(0) == Object.class; // unbound receiver
        assert type.parameterType(1) == DispatchContext.class; // first arg
        assert type.parameterCount() >= 3; // third is some core type
    }

    MethodHandle dynamicInvoker(Lookup lookup, String name, MethodType type) {
        return dynamicInvoker(new CallSiteDescriptor(lookup, CALL.named(name), type));
    }

    MethodHandle dynamicInvoker(CallSiteDescriptor descriptor) {
        return linker.link(new ChainedCallSite(descriptor)).dynamicInvoker();
    }

    static class ProceedLinker implements GuardingDynamicLinker {
        @Override
        public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices)
                throws Exception {
            CallSiteDescriptor descriptor = linkRequest.getCallSiteDescriptor();
            Object receiver = linkRequest.getReceiver();
            Lookup lookup = descriptor.getLookup();
            if (receiver != null) {
                MethodHandle handle = lookup.findVirtual(receiver.getClass(), getOperationName(descriptor),
                        lookupType(descriptor.getMethodType()));
                return new GuardedInvocation(handle,
                        Guards.isInstance(receiver.getClass(), descriptor.getMethodType()));
            }
            Class<?> coreType = descriptor.getMethodType().parameterType(2);
            MethodType coreMethodType = descriptor.getMethodType().dropParameterTypes(0, 3);
            MethodHandle handle = lookup.findVirtual(coreType, getOperationName(descriptor), coreMethodType);
            return new GuardedInvocation(dropArguments(handle, 0, Object.class, DispatchContext.class),
                    Guards.isNull());
        }
    }

    private static MethodType lookupType(MethodType type) {
        return type.dropParameterTypes(0, 1);
    }

    /**
     * Unwraps the method name from the {@link Operation} referenced by the
     * {@link CallSiteDescriptor}.
     */
    private static String getOperationName(final CallSiteDescriptor descriptor) {
        Operation op = descriptor.getOperation();
        if (op instanceof NamedOperation) {
            return ((NamedOperation) op).getName().toString();
        }
        throw new AssertionError();
    }
}
