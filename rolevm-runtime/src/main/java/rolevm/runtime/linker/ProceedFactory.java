package rolevm.runtime.linker;

import static java.lang.invoke.MethodHandles.filterArguments;
import static java.lang.invoke.MethodHandles.foldArguments;
import static rolevm.runtime.linker.DispatchContext.NEXT_HANDLE;
import static rolevm.runtime.linker.DispatchContext.TARGET_HANDLE;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public class ProceedFactory {
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
}
