package rolevm.runtime.binder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

public class BinderTest {
    private BinderNG binder;

    @Before
    public void setUp() {
        binder = new BinderNG();
    }

    @Test
    public void observerNotification() {
        BindingObserver observer1 = mock(BindingObserver.class);
        BindingObserver observer2 = mock(BindingObserver.class);
        binder.addObserver(observer1);
        binder.addObserver(observer2);
        Object player = new Object();
        Object role = new Object();
        binder.unbind(player, role);
        verify(observer1, never()).bindingRemoved(player, role);
        verify(observer2, never()).bindingRemoved(player, role);
        binder.bind(player, role);
        verify(observer1).bindingAdded(player, role);
        verify(observer2).bindingAdded(player, role);
        binder.unbind(player, role);
        verify(observer1).bindingRemoved(player, role);
        verify(observer2).bindingRemoved(player, role);
    }
}
