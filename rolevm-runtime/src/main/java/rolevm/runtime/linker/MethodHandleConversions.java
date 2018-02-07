package rolevm.runtime.linker;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Contains often used method handle conversions.
 * 
 * @author Martin Morgenstern
 */
public class MethodHandleConversions {
    private MethodHandleConversions() {
    }

    public static MethodHandle dropSenderArgument(final MethodHandle handle) {
        return MethodHandles.dropArguments(handle, handle.type().parameterCount(), Object.class);
    }

    public static MethodType lookupType(final MethodType type) {
        return dropLastParameterType(type.dropParameterTypes(0, 1));
    }

    public static MethodType dropLastParameterType(final MethodType type) {
        int numParams = type.parameterCount();
        return type.dropParameterTypes(numParams - 1, numParams);
    }
}
