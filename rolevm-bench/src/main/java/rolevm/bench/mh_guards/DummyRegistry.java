package rolevm.bench.mh_guards;

import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Simulates a registry of object-to-role mappings.
 * 
 * The mappings are filled with a lot of dummy entries to get an idea of the
 * runtime characteristics when used in a program with many object–role
 * relationships and in the context of a MH guard. It is intentional that the
 * {@link DummyRegistry#isPureObject(Object)} guard will return true for every
 * object given to it. What I care for is the amount of time it takes to perform
 * the {@link Map#containsKey(Object)} check in the context of a MH guard.
 * 
 * @author Martin Morgenstern
 */
public class DummyRegistry {
    private static final MethodHandle HANDLE;
    static {
        final Lookup lookup = MethodHandles.lookup();
        try {
            HANDLE = lookup.findVirtual(DummyRegistry.class, "isPureObject", methodType(Boolean.TYPE, Object.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw (AssertionError) new AssertionError().initCause(e);
        }
    }

    private final Map<Object, Object> map;
    private final MethodHandle handle;

    public DummyRegistry(final int size) {
        map = new IdentityHashMap<>(size);
        for (int i = 0; i < size; ++i) {
            map.put(new Object(), Integer.valueOf(0));
        }
        MethodHandle boundHandle = HANDLE.bindTo(this);
        handle = boundHandle.asType(boundHandle.type().changeParameterType(0, String.class));
    }

    /**
     * Returns true if the argument is a pure object (= it does not play a role).
     */
    public boolean isPureObject(final Object o) {
        return !map.containsKey(o);
    }

    /**
     * Returns a MH to the {@link DummyRegistry#isPureObject(Object)} guard, adapted
     * to the type <code>(String)boolean</code>.
     */
    public MethodHandle getGuardHandle() {
        return handle;
    }
}
