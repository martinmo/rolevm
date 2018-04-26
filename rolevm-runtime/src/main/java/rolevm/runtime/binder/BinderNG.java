package rolevm.runtime.binder;

import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import rolevm.api.service.BindingService;
import rolevm.runtime.binder.util.ConcurrentWeakHashMap;
import rolevm.runtime.linker.DispatchContext;

/**
 * Manages object-to-role bindings, provides binding operations, and answers
 * queries related to the object/role mappings.
 * 
 * @author Martin Morgenstern
 */
public class BinderNG implements BindingService {
    /** Lock object used to guard bind/unbind operations. */
    private final Object mutex = new Object();

    /**
     * Saves roles of an object in a canonical mapping. Whenever this map is
     * updated, the corresponding cached entry in {@link #contexts} must be updated
     * as well.
     */
    private final Map<Object, List<Object>> registry = new ConcurrentWeakHashMap<>();

    /**
     * Alternative, cached representation of {@link #registry} in form of a mapping
     * to {@link DispatchContext}s. Must be recomputed whenever {@link #registry}
     * changes.
     */
    private final Map<Object, DispatchContext> contexts = new ConcurrentHashMap<>();

    /** List of objects which subscribed to binding events. */
    private final List<BindingObserver> bindingObservers = new ArrayList<>();

    /** Direct method handle to {@link Map#containsKey(Object)} */
    private static final MethodHandle containsKeyHandle;

    /** Direct method handle to {@link Map#get(Object)} */
    private static final MethodHandle getContextHandle;

    static {
        Lookup lookup = MethodHandles.lookup();
        try {
            containsKeyHandle = lookup.findVirtual(Map.class, "containsKey", methodType(boolean.class, Object.class));
            getContextHandle = lookup.findVirtual(Map.class, "get", methodType(Object.class, Object.class));
        } catch (ReflectiveOperationException e) {
            throw (AssertionError) new AssertionError().initCause(e);
        }
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
        }
        // "alien" methods should be called outside synchronized blocks:
        // bindingObservers.stream().forEach(o -> o.bindingAdded(player, role));
    }

    /**
     * Deletes the first found binding of {@code role} to {@code player}.
     */
    public void unbind(final Object player, final Object role) {
        synchronized (mutex) {

        }
        // bindingObservers.stream().forEach(o -> o.bindingRemoved(player, role));
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
     * Returns a direct method handle to {@link Map#containsKey(Object)}, bound to
     * the internal object/dispatch context map.
     */
    public MethodHandle createContainsKeyHandle() {
        return MethodHandles.insertArguments(containsKeyHandle, 0, contexts);
    }

    /**
     * Returns a direct method handle to {@link Map#get(Object)}, bound to the
     * internal object/dispatch context map.
     */
    public MethodHandle createGetContextHandle() {
        return MethodHandles.insertArguments(getContextHandle, 0, contexts);
    }
}
