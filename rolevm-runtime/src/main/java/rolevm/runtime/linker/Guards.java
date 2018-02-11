package rolevm.runtime.linker;

import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import rolevm.runtime.binder.Binder;

/**
 * Factory for guarding {@link MethodHandle}s used by the linker.
 * 
 * @author Martin Morgenstern
 */
public class Guards {
    private static final MethodHandle pureObjectHandle;
    private static final MethodHandle roleTypePlayedByHandle;

    static {
        Lookup lookup = MethodHandles.lookup();
        try {
            pureObjectHandle = lookup.findVirtual(Binder.class, "isPureObject",
                    methodType(boolean.class, Object.class));
            roleTypePlayedByHandle = lookup.findVirtual(Binder.class, "isRoleTypePlayedBy",
                    methodType(boolean.class, Class.class, Object.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw (AssertionError) new AssertionError().initCause(e);
        }
    }

    private Guards() {
    }

    public static MethodHandle createPureObjectGuard(final Binder binder) {
        return MethodHandles.insertArguments(pureObjectHandle, 0, binder);
    }

    public static MethodHandle createRoleTypePlayedByGuard(final Binder binder, final Class<?> type) {
        return MethodHandles.insertArguments(roleTypePlayedByHandle, 0, binder, type);
    }
}
