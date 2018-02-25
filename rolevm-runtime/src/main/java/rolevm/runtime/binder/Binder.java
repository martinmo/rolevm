package rolevm.runtime.binder;

import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import rolevm.api.service.BindingService;
import rolevm.runtime.binder.util.RefEqualWeakHashMap;

/**
 * Manages object-to-role bindings, provides binding operations, and answers
 * queries related to the object/role mappings.
 * 
 * @author Martin Morgenstern
 */
public class Binder implements BindingService {
    /** Lock object used to guard bind/unbind operations. */
    private final Object mutex = new Object();

    /**
     * Maps objects to roles using reference equality instead of object equality.
     */
    private final Map<Object, Object> registry = new RefEqualWeakHashMap<>();

    /** Direct method handle to {@link IdentityHashMap#containsKey(Object)} */
    private static final MethodHandle containsKeyHandle;

    /** Direct method handle to {@link IdentityHashMap#get(Object)} */
    private static final MethodHandle getRoleHandle;

    /**
     * Direct method handle to {@link IdentityHashMap#getOrDefault(Object, Object)}
     */
    private static final MethodHandle nextRoleHandle;

    static {
        Lookup lookup = MethodHandles.lookup();
        try {
            containsKeyHandle = lookup.findVirtual(RefEqualWeakHashMap.class, "containsKey",
                    methodType(boolean.class, Object.class));
            getRoleHandle = lookup.findVirtual(RefEqualWeakHashMap.class, "get",
                    methodType(Object.class, Object.class));
            nextRoleHandle = lookup.findVirtual(RefEqualWeakHashMap.class, "getOrDefault",
                    methodType(Object.class, Object.class, Object.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw (AssertionError) new AssertionError().initCause(e);
        }
    }

    /** List of objects which subscribed to binding events. */
    private final List<BindingObserver> bindingObservers = new ArrayList<>();

    public void bind(final Object player, final Object role) {
        Objects.requireNonNull(player);
        if (player == role) {
            throw new IllegalArgumentException("player and role must be distinct objects");
        }
        TypeChecks.validateRoleType(role.getClass());
        TypeChecks.validatePlayer(player);
        synchronized (mutex) {
            // TODO: enhance this data structure
            Object playerToTry = player;
            do {
                playerToTry = registry.putIfAbsent(playerToTry, role);
            } while (playerToTry != null);
            bindingObservers.stream().forEach(o -> o.bindingAdded(player, role));
        }
    }

    public void unbind(final Object player, final Object role) {
        synchronized (mutex) {
            // TODO: this doesn't work with multiple bound roles
            if (registry.remove(player, role)) {
                bindingObservers.stream().forEach(o -> o.bindingRemoved(player, role));
            }
        }
    }

    public void addObserver(BindingObserver observer) {
        bindingObservers.add(observer);
    }

    public Object getRole(final Object player) {
        return registry.get(player);
    }

    public boolean isPureObject(final Object player) {
        return !registry.containsKey(player);
    }

    public boolean isPlaying(final Object player) {
        return registry.containsKey(player);
    }

    public boolean isPlaying(final Object player, final Object role) {
        return registry.get(player) == role;
    }

    public boolean isPlayedBy(final Object role, final Object player) {
        return registry.get(player) == role;
    }

    public boolean isRoleTypePlayedBy(final Class<?> roleType, final Object player) {
        Object role = registry.get(player);
        return role != null && role.getClass().equals(roleType);
    }

    /**
     * Returns a direct method handle to
     * {@link IdentityHashMap#containsKey(Object)}, bound to the internal
     * object/role registry map.
     */
    public MethodHandle createContainsKeyHandle() {
        return MethodHandles.insertArguments(containsKeyHandle, 0, registry);
    }

    /**
     * Returns a direct method handle to {@link IdentityHashMap#get(Object)}, bound
     * to the internal object/role registry map.
     */
    public MethodHandle createGetRoleHandle() {
        return MethodHandles.insertArguments(getRoleHandle, 0, registry);
    }

    public MethodHandle createNextRoleHandle() {
        return MethodHandles.insertArguments(nextRoleHandle, 0, registry);
    }
}
