package rolevm.runtime;

import java.lang.invoke.SwitchPoint;

/**
 * Wraps a value of an arbitrary type {@code T} (objects of this type should be
 * immutable) and an associated switchpoint that can be used to check if the
 * wrapped value is still valid.
 * 
 * @param <T>
 *            the type of the guarded value
 * @author Martin Morgenstern
 */
public interface GuardedValue<T> {
    /**
     * Returns the switchpoint that can be used to check if the wrapped value is
     * still valid.
     * 
     * @see SwitchPoint#hasBeenInvalidated()
     */
    public SwitchPoint switchpoint();

    /**
     * Returns the guarded value.
     */
    public T value();
}
