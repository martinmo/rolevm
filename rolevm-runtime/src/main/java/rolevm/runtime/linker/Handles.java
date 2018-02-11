package rolevm.runtime.linker;

import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import rolevm.runtime.binder.Binder;

/**
 * Factory for {@link MethodHandle}s used by the linker.
 * 
 * @author Martin Morgenstern
 */
public class Handles {
    private static final MethodHandle getRoleHandle;

    static {
        Lookup lookup = MethodHandles.lookup();
        try {
            getRoleHandle = lookup.findVirtual(Binder.class, "getRole", methodType(Object.class, Object.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw (AssertionError) new AssertionError().initCause(e);
        }
    }

    private Handles() {
    }

    public static MethodHandle createGetRoleHandle(final Binder binder) {
        return MethodHandles.insertArguments(getRoleHandle, 0, binder);
    }
}
