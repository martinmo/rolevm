package rolevm.runtime.binder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.invoke.MethodHandle;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import rolevm.api.DispatchContext;
import rolevm.runtime.TestCompartment;
import rolevm.runtime.TestCompartment.ValidRole;

public class BinderRemovalTest {
    private Binder binder;
    private Object player;
    private TestCompartment compartment;
    private ValidRole role1, role2, role3;

    @Before
    public void setUp() {
        binder = new CacheAwareBinder();
        player = new Object();
        compartment = new TestCompartment();
        role1 = compartment.new ValidRole();
        role2 = compartment.new ValidRole();
        role3 = compartment.new ValidRole();
        binder.bind(player, role1);
        binder.bind(player, role2);
        binder.bind(player, role3);
    }

    @Test
    public void basicAssumptions() {
        assertTrue(binder.isPureObject(new Object()));
        assertEquals(List.of(role1, role2, role3), binder.getRoles(player));
        binder.unbind(player, role2);
        assertEquals(List.of(role1, role3), binder.getRoles(player));
        binder.unbind(player, role1);
        assertEquals(List.of(role3), binder.getRoles(player));
        binder.unbind(player, role3);
        assertEquals(List.of(), binder.getRoles(player));
        assertTrue(binder.isPureObject(player));
        assertFalse(binder.getDispatchContext(player).isPresent());
    }

    @Test
    public void basicAssumptionsWithHandle() throws Throwable {
        MethodHandle getContext = binder.createGetContextHandle();
        assertTrue((DispatchContext) getContext.invokeExact(player) instanceof DispatchContext);
        binder.unbind(player, role3);
        binder.unbind(player, role2);
        binder.unbind(player, role1);
        assertEquals(DispatchContext.END, (DispatchContext) getContext.invokeExact(player));
    }

    @Test
    public void contextGetsUpdated1() {
        assertEquals(role1, binder.getDispatchContext(player).get().target());
        binder.unbind(player, role1);
        assertEquals(role2, binder.getDispatchContext(player).get().target());
    }

    @Test
    public void contextGetsUpdated2() {
        assertEquals(role1, binder.getDispatchContext(player).get().target());
        binder.unbind(player, role2);
        assertEquals(role1, binder.getDispatchContext(player).get().target());
        assertEquals(role3, binder.getDispatchContext(player).get().next().target());
    }
}
