package rolevm.runtime.linker;

import static java.lang.invoke.MethodHandles.filterArguments;
import static java.lang.invoke.MethodHandles.foldArguments;
import static rolevm.runtime.linker.DispatchContext.NEXT_HANDLE;
import static rolevm.runtime.linker.DispatchContext.TARGET_HANDLE;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.DynamicLinker;
import jdk.dynalink.support.ChainedCallSite;

public class ProceedInvocation {
    private final DynamicLinker linker;
    private final CallSiteDescriptor descriptor;

    public ProceedInvocation(DynamicLinker linker, CallSiteDescriptor descriptor) {
        this.linker = linker;
        this.descriptor = descriptor;
    }

    public MethodHandle getHandle() {
        return combineWithContext(callSiteInvoker());
    }

    MethodHandle callSiteInvoker() {
        return linker.link(new ChainedCallSite(descriptor)).dynamicInvoker();
    }

    static MethodHandle combineWithContext(final MethodHandle invoker) {
        ensureCorrectType(invoker);
        return foldArguments(filterArguments(invoker, 1, NEXT_HANDLE), TARGET_HANDLE);
    }

    private static void ensureCorrectType(final MethodHandle handle) {
        MethodType type = handle.type();
        assert type.parameterType(0) == Object.class; // unbound receiver
        assert type.parameterType(1) == DispatchContext.class; // first arg
        assert type.parameterCount() >= 3; // third is some core type
    }
}
