package rolevm.runtime.binder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import rolevm.api.RoleBindingException;
import rolevm.runtime.binder.TestCompartment.ValidRole;
import rolevm.runtime.linker.DispatchContext;

public class BinderTest {
    private BinderNG binder;
    private Object player;
    private TestCompartment compartment;
    private ValidRole role;

    @Before
    public void setUp() {
        binder = new BinderNG();
        player = new Object();
        compartment = new TestCompartment();
        role = compartment.new ValidRole();
    }

    @Test(expected = RoleBindingException.class)
    public void bindInvalidRoleType() {
        binder.bind(player, new InvalidRole());
    }

    @Test(expected = RoleBindingException.class)
    public void bindInvalidPlayer() {
        TestCompartment compartment = new TestCompartment();
        binder.bind(compartment.new ValidRole(), compartment.new ValidRole());
    }

    @Test
    public void observerNotification() {
        BindingObserver observer1 = mock(BindingObserver.class);
        BindingObserver observer2 = mock(BindingObserver.class);
        binder.addObserver(observer1);
        binder.addObserver(observer2);
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

    @Test
    public void basicAssumptions() {
        assertEquals(List.of(), binder.getRoles(player));
        assertEquals(Optional.empty(), binder.getDispatchContext(player));
        assertTrue(binder.isPureObject(player));
    }

    @Test
    public void basicAssumptionsWithHandle() throws Throwable {
        MethodHandle getContext = binder.createGetContextHandle();
        MethodHandle containsKey = binder.createContainsKeyHandle();
        assertNull((DispatchContext) getContext.invokeExact(player));
        assertFalse((boolean) containsKey.invokeExact(player));
    }

    @Test
    public void singleRoleBinding() {
        binder.bind(player, role);
        assertEquals(List.of(role), binder.getRoles(player));
        Optional<DispatchContext> context = binder.getDispatchContext(player);
        assertTrue(context.isPresent());
        assertEquals(role, context.get().target());
        assertFalse(binder.isPureObject(player));
    }

    @Test
    public void multipleRoleBindings() {
        binder.bind(player, role);
        binder.bind(player, compartment.new ValidRole());
        binder.bind(player, compartment.new ValidRole());
        assertFalse(binder.isPureObject(player));
        assertEquals(3, binder.getRoles(player).size());
        Optional<DispatchContext> context = binder.getDispatchContext(player);
        assertTrue(context.isPresent());
        assertEquals(role, context.get().target());
        assertNotNull(context.get().next());
        assertNotNull(context.get().next().next());
        assertNull(context.get().next().next().next().next());
    }
}
