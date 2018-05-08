package rolevm.runtime;

import java.util.Optional;

import rolevm.api.DispatchContext;

/**
 * Specifies query methods, supposed to be implemented by a object/role manager,
 * which return immutable values that can be checked if they have been
 * externally invalidated.
 * 
 * @author Martin Morgenstern
 */
public interface GuardedQuery {
    /**
     * Returns the result of {@link Binder#isPureType(Class)} as a guarded value.
     */
    GuardedValue<Boolean> getGuardedIsPureType(Class<?> type);

    /**
     * Returns the result of {@link Binder#getDispatchContext(Object)} as a guarded
     * value.
     */
    GuardedValue<Optional<DispatchContext>> getGuardedDispatchContext(Object player);
}
