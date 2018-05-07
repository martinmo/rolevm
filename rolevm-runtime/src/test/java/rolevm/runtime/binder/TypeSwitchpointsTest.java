package rolevm.runtime.binder;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.invoke.SwitchPoint;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import rolevm.runtime.GuardedValue;
import rolevm.runtime.TestCompartment;
import rolevm.runtime.TestCompartment.ValidRole;

public class TypeSwitchpointsTest {
    private CacheAwareBinder binder;
    private ValidRole role;

    @Before
    public void setUp() {
        binder = new CacheAwareBinder();
        role = new TestCompartment().new ValidRole();
    }

    @Test
    public void initiallyValid() {
        assertTrue(allPureTypes());
        assertTrue(allValid(switchpoints()));
    }

    @Test
    public void allInvalidated() {
        List<SwitchPoint> switchpointsBefore = switchpoints();
        binder.bind(new B(), role);
        assertTrue(allInvalidated(switchpointsBefore));
        assertTrue(allInvalidated(switchpoints()));
        assertTrue(allUnpureTypes());
    }

    @Test
    public void remainsInvalidated() {
        List<SwitchPoint> switchpointsBefore = switchpoints();
        Object core = new B();
        binder.bind(core, role);
        binder.unbind(core, role);
        assertTrue(allInvalidated(switchpointsBefore));
        assertTrue(allPureTypes());
    }

    @Test
    public void invalidatedExceptForB() {
        SwitchPoint switchPointForB = binder.getGuardedIsPureType(B.class).switchpoint();
        List<SwitchPoint> switchPointsExceptForB = switchpoints().stream() //
                .filter(x -> x != switchPointForB) //
                .collect(toList());
        binder.bind(new A(), role);
        assertFalse(switchPointForB.hasBeenInvalidated());
        assertTrue(allInvalidated(switchPointsExceptForB));
    }

    @Test
    @Ignore // future work
    public void completeRevalidation() {
        Object core = new B();
        binder.bind(core, role);
        binder.unbind(core, role);
        assertTrue(allPureTypes());
        assertTrue(allValid(switchpoints()));
    }

    @Test
    @Ignore // future work
    public void partialRevalidation() {
        Object core = new B();
        binder.bind(core, role);
        binder.bind(new A(), role);
        binder.unbind(core, role);
        assertFalse(binder.getGuardedIsPureType(A.class).value());
        assertTrue(binder.getGuardedIsPureType(A.class).switchpoint().hasBeenInvalidated());
        assertFalse(binder.getGuardedIsPureType(I1.class).value());
        assertTrue(binder.getGuardedIsPureType(I1.class).switchpoint().hasBeenInvalidated());
        assertTrue(binder.getGuardedIsPureType(B.class).value());
        assertFalse(binder.getGuardedIsPureType(B.class).switchpoint().hasBeenInvalidated());
    }

    // utilities

    private List<SwitchPoint> switchpoints() {
        return TYPES.stream().map(t -> binder.getGuardedIsPureType(t)).map(GuardedValue::switchpoint).collect(toList());
    }

    private boolean allPureTypes() {
        return TYPES.stream().map(t -> binder.getGuardedIsPureType(t)).allMatch(GuardedValue::value);
    }

    private boolean allUnpureTypes() {
        return !TYPES.stream().map(t -> binder.getGuardedIsPureType(t)).anyMatch(GuardedValue::value);
    }

    private static boolean allInvalidated(final List<SwitchPoint> switchpoints) {
        return switchpoints.stream().allMatch(SwitchPoint::hasBeenInvalidated);
    }

    private static boolean allValid(final List<SwitchPoint> switchpoints) {
        return !switchpoints.stream().anyMatch(SwitchPoint::hasBeenInvalidated);
    }

    // test interfaces and classes

    private static final List<Class<?>> TYPES = List.of(Object.class, I1.class, I2.class, I3.class, A.class, B.class);

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
