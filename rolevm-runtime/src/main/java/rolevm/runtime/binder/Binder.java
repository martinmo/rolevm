package rolevm.runtime.binder;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;

import rolevm.api.RoleBindingException;
import rolevm.api.service.BindingService;
import rolevm.runtime.RoleTypeConstants;

/**
 * Manages object-to-role bindings, provides binding operations, and answers
 * queries related to the object/role mappings.
 * 
 * @author Martin Morgenstern
 */
public class Binder implements BindingService, RoleTypeConstants {
    /** Lock object used to guard bind/unbind operations. */
    private final Object mutex = new Object();

    /**
     * Maps objects to roles using reference equality instead of object equality.
     * <p>
     * Note: this mapping prevents garbage collection of the keys, i.e., the player
     * objects. The resulting memory leak is a fundamental problem and cannot be
     * fixed easily, e.g., using {@link java.util.WeakHashMap} or
     * {@link java.lang.ref.WeakReference}, unless the JVM garbage collector
     * implements support for ephemerons.
     */
    private final Map<Object, Object> registry = new IdentityHashMap<>();

    /** List of objects which subscribed to binding events. */
    private final List<BindingObserver> bindingObservers = new LinkedList<>();

    private final ClassValue<Optional<Field>> baseFields = new BaseFields();

    /**
     * Lazily loads and caches base field references per type.
     * <p>
     * Using {@link ClassValue} avoids memory leaks and is better than using a
     * {@link WeakHashMap} for this purpose (the latter one would fail here because
     * {@link Field} strongly references the {@link Class}, i.e., the key of the
     * map).
     */
    static class BaseFields extends ClassValue<Optional<Field>> {
        @Override
        protected Optional<Field> computeValue(final Class<?> type) {
            for (Field field : type.getDeclaredFields()) {
                if (field.getDeclaredAnnotationsByType(BASE_ANNOTATION).length > 0) {
                    return Optional.of(field);
                }
            }
            return Optional.empty();
        }
    };

    public void bind(final Object player, final Object role) {
        Objects.requireNonNull(player);
        if (player == role) {
            throw new IllegalArgumentException("player and role must be distinct objects");
        }
        Class<?> roleType = TypeChecks.validateRoleType(role.getClass());
        Field baseField = baseFields.get(roleType)
                .orElseThrow(() -> new RoleBindingException("role type has no base field: " + roleType));
        TypeChecks.validatePlayer(player);
        TypeChecks.checkFieldAssignment(baseField, player);
        // TODO: handle binding of multiple roles
        synchronized (mutex) {
            if (registry.putIfAbsent(player, role) == null) {
                safeSet(baseField, role, player);
                bindingObservers.stream().forEach(o -> o.bindingAdded(player, role));
            }
        }
    }

    public void unbind(final Object player, final Object role) {
        synchronized (mutex) {
            if (registry.remove(player, role)) {
                Field baseField = baseFields.get(role.getClass())
                        .orElseThrow(() -> new AssertionError("bound role that has no base field"));
                bindingObservers.stream().forEach(o -> o.bindingRemoved(player, role));
                safeSet(baseField, role, null);
            }
        }
    }

    static void safeSet(Field field, final Object obj, final Object value) {
        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
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
}
