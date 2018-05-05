package rolevm.runtime.linker;

import java.lang.invoke.SwitchPoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rolevm.runtime.binder.BindingObserver;

/**
 * Creates and manages a {@link SwitchPoint} per static receiver type at a call
 * site. When a binding is added for an object of runtime type {@code T}, the
 * switch points of static receiver types that {@code T} is
 * assignment-compatible to are invalidated.
 * 
 * @implNote Using {@link ClassValue} avoids memory leaks, e.g., in the case
 *           when the {@link Class} objects used as a key are otherwise
 *           unreferenced.
 * 
 * @author Martin Morgenstern
 */
public class SwitchPointManager implements BindingObserver {
    private final ClassValue<Set<Class<?>>> supertypes = new Supertypes();
    private final ClassValue<SwitchPoint> switchpoints = new ClassValue<>() {
        @Override
        protected SwitchPoint computeValue(final Class<?> type) {
            return new SwitchPoint();
        }
    };

    public SwitchPoint getSwitchPointForType(final Class<?> type) {
        return switchpoints.get(type);
    }

    public void invalidateSwitchPoints(final Class<?> type) {
        final List<SwitchPoint> switchPoints = new ArrayList<>();
        for (Class<?> computed : supertypes.get(type)) {
            switchPoints.add(switchpoints.get(computed));
        }
        SwitchPoint.invalidateAll(switchPoints.toArray(new SwitchPoint[0]));
        // We do not manually remove invalidated SwitchPoints from the mapping.
        // Since we use ClassValue, an invalidated SwitchPoint may be automatically
        // reclaimed by the GC if
        // 1) its corresponding Class isn't referenced elsewhere anymore, and
        // 2) if the SwitchPoint's guard handle was purged from all ChainedCallSites.
    }

    @Override
    public void bindingAdded(final Object player, final Object role) {
        invalidateSwitchPoints(player.getClass());
    }

    @Override
    public void bindingRemoved(final Object player, final Object role) {
        // intentionally left empty
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
}
