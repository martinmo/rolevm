package rolevm.runtime.binder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import rolevm.api.DispatchContext;
import rolevm.api.RoleBindingException;
import rolevm.runtime.InvalidRole;
import rolevm.runtime.TestCompartment;
import rolevm.runtime.TestCompartment.ValidRole;

public class BinderTest {
    private CacheAwareBinder binder;
    private Object player;
    private TestCompartment compartment;
    private ValidRole role;

    @Before
    public void setUp() {
        binder = new CacheAwareBinder();
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
    public void basicAssumptions() {
        assertEquals(List.of(), binder.getRoles(player));
        assertEquals(Optional.empty(), binder.getDispatchContext(player));
        assertTrue(binder.isPureObject(player));
        assertTrue(binder.isPureType(Object.class));
        assertTrue(binder.isPureType(I1.class));
        assertTrue(binder.isPureType(A.class));
        assertTrue(binder.isPureType(B.class));
    }

    @Test
    public void basicAssumptionsWithHandle() throws Throwable {
        MethodHandle getContext = binder.createGetContextHandle();
        assertEquals(DispatchContext.END, (DispatchContext) getContext.invokeExact(player));
    }

    @Test
    public void singleRoleBinding() {
        binder.bind(player, role);
        assertEquals(List.of(role), binder.getRoles(player));
        Optional<DispatchContext> context = binder.getDispatchContext(player);
        assertTrue(context.isPresent());
        assertEquals(role, context.get().target());
        assertFalse(binder.isPureObject(player));
        assertFalse(binder.isPureType(Object.class));
        assertTrue(binder.isPureType(String.class));
        assertTrue(binder.isPureType(Comparable.class));
    }

    @Test
    public void multipleRoleBindings() {
        binder.bind(player, role);
        binder.bind(player, compartment.new ValidRole());
        binder.bind(player, compartment.new ValidRole());
        assertFalse(binder.isPureObject(player));
        assertFalse(binder.isPureType(Object.class));
        assertTrue(binder.isPureType(String.class));
        assertEquals(3, binder.getRoles(player).size());
        Optional<DispatchContext> context = binder.getDispatchContext(player);
        assertTrue(context.isPresent());
        assertEquals(role, context.get().target());
        assertNotNull(context.get().next());
        assertNotNull(context.get().next().next());
        assertNull(context.get().next().next().next().next());
    }

    @Test
    public void pureType() {
        binder.bind(new A(), role);
        assertFalse(binder.isPureType(Object.class));
        assertFalse(binder.isPureType(I1.class));
        assertFalse(binder.isPureType(I2.class));
        assertFalse(binder.isPureType(I3.class));
        assertFalse(binder.isPureType(A.class));
        assertTrue(binder.isPureType(B.class));
    }

    // some test classes for isPureType() tests:

    interface I1 {
    }

    interface I2 {
    }

    interface I3 extends I1, I2 {
    }

    static class A implements I3 {
    }

    static class B extends A {
    }
}
