package rolevm.runtime.linker;

import java.lang.invoke.SwitchPoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rolevm.runtime.binder.BindingObserver;

/**
 * Creates and manages a {@link SwitchPoint} per static receiver type at a call
 * site. When a binding is added for an object of runtime type {@code T}, the
 * switch points of static receiver types that {@code T} is
 * assignment-compatible to are invalidated.
 * <p>
 * Using {@link ClassValue} avoids memory leaks, e.g., in the case when the
 * {@link Class} objects used as a key are otherwise unreferenced.
 * 
 * @author Martin Morgenstern
 */
public class SwitchPointManager extends ClassValue<SwitchPoint> implements BindingObserver {
    @Override
    protected SwitchPoint computeValue(final Class<?> type) {
        return new SwitchPoint();
    }

    public void invalidateSwitchPoints(Class<?> type) {
        final List<SwitchPoint> switchPoints = new ArrayList<>();
        for (Class<?> computed : assignmentCompatibleTypes(type)) {
            switchPoints.add(get(computed));
        }
        SwitchPoint.invalidateAll(switchPoints.toArray(new SwitchPoint[0]));
        // We do not manually remove invalidated SwitchPoints from the mapping.
        // Since we use ClassValue, an invalidated SwitchPoint may be automatically
        // reclaimed by the GC if
        // 1) its corresponding Class isn't referenced elsewhere anymore, and
        // 2) if the SwitchPoint's guard handle was purged from all ChainedCallSites.
    }

    List<Class<?>> assignmentCompatibleTypes(Class<?> type) {
        final List<Class<?>> types = new ArrayList<>();
        final boolean itf = type.isInterface();
        types.addAll(Arrays.asList(type.getInterfaces()));
        do {
            types.add(type);
            type = type.getSuperclass();
        } while (type != null);
        if (itf) {
            types.add(Object.class);
        }
        return types;
    }

    @Override
    public void bindingAdded(final Object player, final Object role) {
        invalidateSwitchPoints(player.getClass());
    }

    @Override
    public void bindingRemoved(final Object player, final Object role) {
        // intentionally left empty
    }
}
