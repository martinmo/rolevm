package rolevm.bench.mh_combos;

import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Registry of object-to-proxy mappings.
 * 
 * @author Martin Morgenstern
 */
public class ProxyRegistry {
    private static final MethodHandle HANDLE;
    static {
        final Lookup lookup = MethodHandles.lookup();
        try {
            HANDLE = lookup.findVirtual(Map.class, "get", methodType(Object.class, Object.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw (AssertionError) new AssertionError().initCause(e);
        }
    }

    private final Map<String, Object> map;
    private final MethodHandle boundHandle;

    public ProxyRegistry(final int size) {
        map = new IdentityHashMap<>(size);
        for (int i = 0; i < size; ++i) {
            String str = new String("Use the force, Luke.");
            map.put(str, new StringProxy(str));
        }
        boundHandle = HANDLE.bindTo(map);
    }

    public String[] getKeys() {
        return map.keySet().toArray(new String[0]);
    }

    public Object get(final Object o) {
        return map.get(o);
    }

    public MethodHandle getGetProxyHandle() {
        return boundHandle;
    }
}
