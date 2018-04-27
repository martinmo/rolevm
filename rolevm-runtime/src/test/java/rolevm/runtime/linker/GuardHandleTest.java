package rolevm.runtime.linker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.invoke.MethodHandle;

import org.junit.Before;
import org.junit.Test;

import rolevm.runtime.TestCompartment;
import rolevm.runtime.TestCompartment.ValidRole;
import rolevm.runtime.binder.Binder;

public class GuardHandleTest {
    private Binder binder;
    private StateBasedLinker linker;
    private MethodHandle isPure;
    private MethodHandle isNotPure;
    private ValidRole role;

    @Before
    public void setUp() {
        binder = new Binder();
        linker = new StateBasedLinker(binder);
        isPure = linker.getIsPureHandle();
        isNotPure = linker.getIsNotPureHandle();
        role = new TestCompartment().new ValidRole();
    }

    @Test
    public void basicAssumptions() throws Throwable {
        Object someObject = new Object();
        assertTrue((boolean) isPure.invokeExact(someObject));
        assertFalse((boolean) isNotPure.invokeExact(someObject));
    }

    @Test
    public void whenBound() throws Throwable {
        Object player = new Object();
        binder.bind(player, role);
        assertFalse((boolean) isPure.invoke(player));
        assertTrue((boolean) isNotPure.invoke(player));
    }

    @Test
    public void otherObjectNotAffected() throws Throwable {
        Object player = new Object();
        Object someObject = new Object();
        binder.bind(player, role);
        assertTrue((boolean) isPure.invokeExact(someObject));
        assertFalse((boolean) isNotPure.invokeExact(someObject));
    }
}
