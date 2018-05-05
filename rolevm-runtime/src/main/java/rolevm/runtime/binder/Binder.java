package rolevm.runtime.binder;

import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import rolevm.api.DispatchContext;
import rolevm.api.service.BindingService;

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
     * Saves roles of an object in a canonical mapping. Whenever this map is
     * updated, the corresponding cached entry in {@link #contexts} must be updated
     * as well.
     */
    private final Map<Object, List<Object>> registry = createMap();

    /**
     * Alternative, cached representation of {@link #registry} in form of a mapping
     * to {@link DispatchContext}s. Must be recomputed whenever {@link #registry}
     * changes.
     */
    private final Map<Object, DispatchContext> contexts = createMap();

    /** List of objects which subscribed to binding events. */
    private final List<BindingObserver> bindingObservers = new ArrayList<>();

    /**
     * Allows the user to select the fast, but memory-leaking
     * {@link IdentityHashMap}, or the slower {@link ConcurrentWeakHashMap} as the
     * backing storage, by setting the system property {@code rolevm.map} to the
     * desired class name. By default, {@link IdentityWeakHashMap} is used.
     */
    private static <K, V> Map<K, V> createMap() {
        String implementation = System.getProperty("rolevm.map");
        if ("IdentityHashMap".equalsIgnoreCase(implementation)) {
            return new IdentityHashMap<>();
        }
        return new ConcurrentWeakHashMap<>();
    }

    /**
     * Binds {@code role} to {@code player}. Both must be distinct, non-null
     * objects. Furthermore, the class of {@code role} must be a valid role type,
     * and player must not be a role type (i.e., <em>deep roles</em> are not
     * possible).
     */
    public void bind(final Object player, final Object role) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(role);
        if (player == role) {
            throw new IllegalArgumentException("player and role must be distinct objects");
        }
        TypeChecks.validateRoleType(role.getClass());
        TypeChecks.validatePlayer(player);
        synchronized (mutex) {
            List<Object> currentRoles = registry.get(player);
            if (currentRoles == null) {
                currentRoles = new ArrayList<>();
            }
            currentRoles.add(role);
            registry.put(player, currentRoles);
            contexts.put(player, DispatchContext.of(currentRoles));
        }
        // "alien" methods should be called outside synchronized blocks:
        bindingObservers.stream().forEach(o -> o.bindingAdded(player, role));
    }

    /**
     * Deletes the first found binding of {@code role} to {@code player}.
     */
    public void unbind(final Object player, final Object role) {
        boolean modified = false;
        synchronized (mutex) {
            List<Object> currentRoles = registry.get(player);
            if (currentRoles != null) {
                modified = currentRoles.remove(role);
                if (modified) {
                    if (currentRoles.isEmpty()) {
                        registry.remove(player);
                        contexts.remove(player);
                    } else {
                        contexts.put(player, DispatchContext.of(currentRoles));
                    }
                }
            }
        }
        if (modified) {
            bindingObservers.stream().forEach(o -> o.bindingRemoved(player, role));
        }
    }

    /** Adds an observer that will be notified on binding events. */
    public void addObserver(BindingObserver observer) {
        bindingObservers.add(observer);
    }

    /** Removes an observer from the subscription list. */
    public boolean removeObserver(BindingObserver observer) {
        return bindingObservers.remove(observer);
    }

    /** Returns a (possibly empty) list with the bound roles of {@code player}. */
    public List<Object> getRoles(final Object player) {
        return registry.getOrDefault(player, List.of());
    }

    /**
     * Returns an {@link Optional} containing the {@link DispatchContext} for
     * {@code player}, or an empty Optional if {@code player} has no bound roles.
     */
    public Optional<DispatchContext> getDispatchContext(final Object player) {
        return Optional.ofNullable(contexts.get(player));
    }

    /**
     * Returns true if and only if {@code player} has no bound roles.
     */
    public boolean isPureObject(final Object player) {
        return !contexts.containsKey(player);
    }

    /**
     * Returns true if and only if {@code type} is a pure type, i.e., if there is
     * currently no registered player that is a subtype of {@code type}.
     */
    public boolean isPureType(final Class<?> type) {
        Objects.requireNonNull(type);
        for (Object player : contexts.keySet()) {
            // player could be GC'ed during iteration
            if (player != null && type.isAssignableFrom(player.getClass())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a direct method handle to {@link Map#getOrDefault(Object, Object)},
     * bound to the internal object/dispatch context map.
     */
    public MethodHandle createGetContextHandle() {
        MethodHandle mh = bind(contexts, "getOrDefault", Object.class, Object.class, Object.class);
        mh = insertArguments(mh, 1, DispatchContext.END);
        return mh.asType(methodType(DispatchContext.class, Object.class));
    }

    /**
     * Convenience wrapper around
     * {@link Lookup#bind(Object, String, java.lang.invoke.MethodType)}.
     */
    private static MethodHandle bind(Object receiver, String name, Class<?> rtype, Class<?>... ptypes) {
        try {
            return lookup().bind(receiver, name, methodType(rtype, ptypes));
        } catch (ReflectiveOperationException e) {
            throw (AssertionError) new AssertionError().initCause(e);
        }
    }
}
