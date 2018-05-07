package rolevm.runtime.binder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import rolevm.api.DispatchContext;
import rolevm.runtime.GuardedValue;
import rolevm.runtime.TestCompartment;
import rolevm.runtime.TestCompartment.ValidRole;

public class ContextSwitchpointsTest {
    private CacheAwareBinder binder;
    private Object core1, core2, core3;
    private ValidRole role;

    @Before
    public void setUp() {
        binder = new CacheAwareBinder();
        role = new TestCompartment().new ValidRole();
        core1 = new Object();
        core2 = new Object();
        core3 = new Object();
    }

    @Test
    public void initiallyValid() {
        for (final Object core : List.of(core1, core2, core3)) {
            GuardedValue<Optional<DispatchContext>> guardedCtx = binder.getGuardedDispatchContext(core);
            assertFalse(guardedCtx.switchpoint().hasBeenInvalidated());
            assertEquals(Optional.empty(), guardedCtx.value());
        }
    }

    @Test
    public void initiallyValidNonEmpty() {
        for (final Object core : List.of(core1, core2, core3)) {
            binder.bind(core, role);
        }
        for (final Object core : List.of(core1, core2, core3)) {
            GuardedValue<Optional<DispatchContext>> guardedCtx = binder.getGuardedDispatchContext(core);
            assertFalse(guardedCtx.switchpoint().hasBeenInvalidated());
            assertTrue(guardedCtx.value().isPresent());
        }
    }

    @Test
    public void invalidAfterBind() {
        GuardedValue<Optional<DispatchContext>> guardedCtx1 = binder.getGuardedDispatchContext(core1);
        GuardedValue<Optional<DispatchContext>> guardedCtx2 = binder.getGuardedDispatchContext(core2);
        GuardedValue<Optional<DispatchContext>> guardedCtx3 = binder.getGuardedDispatchContext(core3);
        binder.bind(core1, role);
        assertTrue(guardedCtx1.switchpoint().hasBeenInvalidated());
        assertFalse(guardedCtx2.switchpoint().hasBeenInvalidated());
        assertFalse(guardedCtx3.switchpoint().hasBeenInvalidated());
        binder.bind(core2, role);
        assertTrue(guardedCtx2.switchpoint().hasBeenInvalidated());
        assertFalse(guardedCtx3.switchpoint().hasBeenInvalidated());
        binder.bind(core3, role);
        assertTrue(guardedCtx3.switchpoint().hasBeenInvalidated());
    }

    @Test
    public void invalidAfterUnbind() {
        for (final Object core : List.of(core1, core2, core3)) {
            binder.bind(core, role);
        }
        GuardedValue<Optional<DispatchContext>> guardedCtx1 = binder.getGuardedDispatchContext(core1);
        GuardedValue<Optional<DispatchContext>> guardedCtx2 = binder.getGuardedDispatchContext(core2);
        GuardedValue<Optional<DispatchContext>> guardedCtx3 = binder.getGuardedDispatchContext(core3);
        binder.unbind(core1, role);
        assertTrue(guardedCtx1.switchpoint().hasBeenInvalidated());
        assertFalse(guardedCtx2.switchpoint().hasBeenInvalidated());
        assertFalse(guardedCtx3.switchpoint().hasBeenInvalidated());
        binder.unbind(core2, role);
        assertTrue(guardedCtx2.switchpoint().hasBeenInvalidated());
        assertFalse(guardedCtx3.switchpoint().hasBeenInvalidated());
        binder.unbind(core3, role);
        assertTrue(guardedCtx3.switchpoint().hasBeenInvalidated());
    }

    @Test
    public void unbindWithNoModification1() {
        GuardedValue<Optional<DispatchContext>> guardedCtx = binder.getGuardedDispatchContext(core1);
        binder.unbind(core1, role); // currently not bound, no modification
        assertFalse(guardedCtx.switchpoint().hasBeenInvalidated());
        assertSame(guardedCtx.switchpoint(), binder.getGuardedDispatchContext(core1).switchpoint());
    }

    @Test
    public void unbindWithNoModification2() {
        binder.bind(core1, new TestCompartment().new ValidRole());
        GuardedValue<Optional<DispatchContext>> guardedCtx = binder.getGuardedDispatchContext(core1);
        binder.unbind(core1, role); // currently not bound, no modification
        assertFalse(guardedCtx.switchpoint().hasBeenInvalidated());
        assertSame(guardedCtx.switchpoint(), binder.getGuardedDispatchContext(core1).switchpoint());
    }
}
