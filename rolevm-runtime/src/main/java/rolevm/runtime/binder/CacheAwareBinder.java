package rolevm.runtime.binder;

import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.SwitchPoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import rolevm.api.DispatchContext;
import rolevm.runtime.Binder;
import rolevm.runtime.GuardedQuery;
import rolevm.runtime.GuardedValue;
import rolevm.runtime.TypeChecks;

/**
 * This class implements a thread-safe, memleak-free object/role mapping that is
 * agnostic of call site inline caching using {@link SwitchPoint}s.
 * 
 * @author Martin Morgenstern
 */
public class CacheAwareBinder implements Binder, GuardedQuery {
    /** Lock object used to guard bind/unbind operations. */
    private final Object lock = new Object();

    /**
     * Saves roles of an object in a canonical mapping. Whenever this map is
     * updated, the corresponding cached entry in {@link #contexts} must be updated
     * as well.
     */
    private final Map<Object, List<Object>> registry = createMap();

    /**
     * Alternative, cached representation of {@link #registry} in form of a mapping
     * to {@link DispatchContext}s. The dispatch context must be recomputed whenever
     * the corresponding map entry in {@link #registry} changes.
     */
    private final Map<Object, DispatchContext> contexts = createMap();

    /**
     * A map that maintains switchpoints to support invalidation instance level
     * switchpoints. When the {@link #contexts} (and {@link #registry}) maps are
     * modified, the corresponding context switchpoint must be invalidated and
     * replaced with a new one which is valid until the next modification.
     * 
     * @see #getGuardedDispatchContext(Object)
     */
    private final Map<Object, SwitchPoint> contextSwitchpoints = createMap();

    /** Lazily computed mapping of supertypes for a given type. */
    private final ClassValue<Set<Class<?>>> supertypes = new Supertypes();

    /**
     * A mapping that maintains type level switch points.
     * 
     * @see #getGuardedIsPureType(Class)
     */
    private final ClassValue<SwitchPoint> typeSwitchpoints = new TypeSwitchpoints();

    /**
     * Allows the user to select another {@link Map} implementation via the system
     * property {@code rolevm.map}, for demonstration purposes. Available map
     * implementations are {@link IdentityHashMap} and {@link ConcurrentWeakHashMap}
     * (the default).
     */
    private static <K, V> Map<K, V> createMap() {
        String implementation = System.getProperty("rolevm.map");
        if ("IdentityHashMap".equalsIgnoreCase(implementation)) {
            return new IdentityHashMap<>();
        }
        return new ConcurrentWeakHashMap<>();
    }

    @Override
    public void bind(final Object player, final Object role) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(role);
        if (player == role) {
            throw new IllegalArgumentException("player and role must be distinct objects");
        }
        TypeChecks.validateRoleType(role.getClass());
        TypeChecks.validatePlayer(player);
        synchronized (lock) {
            List<Object> currentRoles = registry.get(player);
            if (currentRoles == null) {
                currentRoles = new ArrayList<>();
            }
            currentRoles.add(role);
            registry.put(player, currentRoles);
            contexts.put(player, DispatchContext.of(currentRoles));
            invalidateType(player.getClass());
            updateContextSwitchpoint(player);
        }
    }

    @Override
    public void unbind(final Object player, final Object role) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(role);
        synchronized (lock) {
            List<Object> currentRoles = registry.get(player);
            if (currentRoles != null && currentRoles.remove(role)) {
                if (currentRoles.isEmpty()) {
                    registry.remove(player);
                    contexts.remove(player);
                } else {
                    contexts.put(player, DispatchContext.of(currentRoles));
                }
                updateContextSwitchpoint(player);
            }
        }
    }

    @Override
    public List<Object> getRoles(final Object player) {
        return registry.getOrDefault(player, List.of());
    }

    @Override
    public Optional<DispatchContext> getDispatchContext(final Object player) {
        return Optional.ofNullable(contexts.get(player));
    }

    @Override
    public boolean isPureObject(final Object player) {
        return !contexts.containsKey(player);
    }

    @Override
    public boolean isPureType(final Class<?> type) {
        Objects.requireNonNull(type);
        for (final Object player : contexts.keySet()) {
            // player could be GC'ed during iteration
            if (player != null && type.isAssignableFrom(player.getClass())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public MethodHandle createGetContextHandle() {
        try {
            MethodHandle getOrDefault = lookup().bind(contexts, "getOrDefault",
                    methodType(Object.class, Object.class, Object.class));
            getOrDefault = insertArguments(getOrDefault, 1, DispatchContext.END);
            return getOrDefault.asType(methodType(DispatchContext.class, Object.class));
        } catch (ReflectiveOperationException e) {
            throw (AssertionError) new AssertionError().initCause(e);
        }
    }

    @Override
    public GuardedValue<Optional<DispatchContext>> getGuardedDispatchContext(final Object player) {
        synchronized (lock) {
            SwitchPoint switchpoint = contextSwitchpoints.get(player);
            if (switchpoint == null) {
                switchpoint = new SwitchPoint();
                contextSwitchpoints.put(player, switchpoint);
            }
            return new GuardedDispatchContext(getDispatchContext(player), switchpoint);
        }
    }

    @Override
    public GuardedValue<Boolean> getGuardedIsPureType(Class<?> type) {
        return new GuardedIsPureType(isPureType(type), typeSwitchpoints.get(type));
    }

    /**
     * Assigns a new context switchpoint to {@code player} and invalidates the old
     * one, if any.
     */
    private void updateContextSwitchpoint(final Object player) {
        final SwitchPoint oldSwitchPoint = contextSwitchpoints.put(player, new SwitchPoint());
        if (oldSwitchPoint != null) {
            SwitchPoint.invalidateAll(new SwitchPoint[] { oldSwitchPoint });
        }
    }

    /**
     * Invalidates the switchpoints of all supertypes of the given type
     * (re-validation is currently not implemented).
     */
    private void invalidateType(final Class<?> type) {
        final List<SwitchPoint> switchpoints = new ArrayList<>();
        for (final Class<?> supertype : supertypes.get(type)) {
            switchpoints.add(typeSwitchpoints.get(supertype));
        }
        SwitchPoint.invalidateAll(switchpoints.toArray(new SwitchPoint[0]));
    }

    /**
     * Thread-safe and leak-free mapping of {@link Class} to {@link SwitchPoint}
     * using {@link #get(Class)} and {@link #remove(Class)}.
     */
    static class TypeSwitchpoints extends ClassValue<SwitchPoint> {
        @Override
        protected SwitchPoint computeValue(final Class<?> type) {
            return new SwitchPoint();
        }
    }

    /**
     * Provides a {@link #get(Class)} method that lazily computes the supertypes for
     * a non-primitive type as per JLS §4.10 with a recursive algorithm in
     * {@link #computeValue(Class)}, and caches the result. The returned set always
     * contains {@link Object} and the type itself.
     * 
     * @author Martin Morgenstern
     */
    static class Supertypes extends ClassValue<Set<Class<?>>> {
        @Override
        protected Set<Class<?>> computeValue(final Class<?> type) {
            if (type.isPrimitive()) {
                throw new IllegalArgumentException();
            }
            if (type.equals(Object.class)) {
                return Collections.singleton(Object.class);
            }
            Set<Class<?>> result = new HashSet<>();
            result.add(type);
            for (Class<?> iface : type.getInterfaces()) {
                result.addAll(get(iface));
            }
            Class<?> superclass = type.getSuperclass();
            if (superclass != null) {
                result.addAll(get(superclass));
            } else {
                result.add(Object.class);
            }
            return Collections.unmodifiableSet(result);
        }
    }

    /** Wraps dispatch contexts as guarded values. */
    static class GuardedDispatchContext implements GuardedValue<Optional<DispatchContext>> {
        private final Optional<DispatchContext> context;
        private final SwitchPoint switchpoint;

        GuardedDispatchContext(final Optional<DispatchContext> context, final SwitchPoint switchpoint) {
            this.context = context;
            this.switchpoint = switchpoint;
        }

        @Override
        public SwitchPoint switchpoint() {
            return switchpoint;
        }

        @Override
        public Optional<DispatchContext> value() {
            return context;
        }
    }

    /** Wraps isPureType values as guarded values. */
    static class GuardedIsPureType implements GuardedValue<Boolean> {
        private final Boolean isPure;
        private final SwitchPoint switchpoint;

        GuardedIsPureType(final boolean isPure, final SwitchPoint switchpoint) {
            this.isPure = Boolean.valueOf(isPure);
            this.switchpoint = switchpoint;
        }

        @Override
        public SwitchPoint switchpoint() {
            return switchpoint;
        }

        @Override
        public Boolean value() {
            return isPure;
        }
    }
}
