package rolevm.runtime;

import java.lang.invoke.SwitchPoint;

public interface GuardedValue<T> {
    public SwitchPoint switchpoint();

    public T value();
}
