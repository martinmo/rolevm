package rolevm.runtime.binder;

import java.util.Optional;

import rolevm.api.DispatchContext;

public interface GuardedQuery {
    GuardedValue<Boolean> getGuardedIsPureType(Class<?> type);

    GuardedValue<Optional<DispatchContext>> getGuardedDispatchContext(Object player);
}
