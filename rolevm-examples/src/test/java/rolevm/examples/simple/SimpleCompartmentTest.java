package rolevm.examples.simple;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import rolevm.examples.simple.SimpleCompartment.AnotherRoleType;
import rolevm.examples.simple.SimpleCompartment.RoleType;

public class SimpleCompartmentTest {
    @Test
    public void oneRoleTypePerCallSite() {
        SimpleCompartment c = new SimpleCompartment();
        BaseType b = new BaseType();
        RoleType r = c.new RoleType(42);

        assertEquals(6, b.calculate(3));
        assertEquals(6, b.delegation());

        c.bind(b, r);
        assertEquals(48, b.calculate(3));
        assertEquals(48, b.delegation());
        c.unbind(b, r);
    }

    @Test
    public void multipleRoleTypesPerCallSite() {
        SimpleCompartment c = new SimpleCompartment();
        BaseType b = new BaseType();
        RoleType r1 = c.new RoleType(42);
        AnotherRoleType r2 = c.new AnotherRoleType();
        int[] expected = new int[] { 48, -3, 48, -3, 48 };
        Object[] roles = new Object[] { r1, r2, r1, r2, r1 };
        assert (expected.length == roles.length);
        for (int i = 0; i < roles.length; ++i) {
            c.bind(b, roles[i]);
            assertEquals(expected[i], b.calculate(3));
            assertEquals(expected[i], b.delegation());
            c.unbind(b, roles[i]);
        }
    }
}
