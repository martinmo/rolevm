package rolevm.runtime.linker;

import java.lang.invoke.SwitchPoint;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import rolevm.runtime.binding.BindingObserver;

/**
 * Creates and manages a {@link SwitchPoint} per static receiver type at a call
 * site. When a binding is added for an object of runtime type {@code T}, the
 * switch points of static receiver types that {@code T} is
 * assignment-compatible to are invalidated.
 * 
 * @author Martin Morgenstern
 */
public class SwitchPointRegistry extends ClassValue<SwitchPoint> implements BindingObserver {
    @Override
    protected SwitchPoint computeValue(final Class<?> clazz) {
        return new SwitchPoint();
    }

    public void invalidateSwitchPoints(Class<?> type) {
        final List<SwitchPoint> switchPoints = new LinkedList<>();
        for (Class<?> computed : assignmentCompatibleTypes(type)) {
            switchPoints.add(get(computed));
        }
        SwitchPoint.invalidateAll(switchPoints.toArray(new SwitchPoint[0]));
        // note: to help the GC, invalidated switchpoints should be removed
        // (tricky, maybe it is easier to use a WeakReference)
    }

    List<Class<?>> assignmentCompatibleTypes(Class<?> type) {
        final List<Class<?>> types = new LinkedList<>();
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
