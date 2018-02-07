package rolevm.examples.simple;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import rolevm.examples.simple.SimpleCompartment.RoleType;

public class SimpleCompartmentTest {
    @Test
    public void test() throws Exception {
        SimpleCompartment c = new SimpleCompartment();
        BaseType b = new BaseType();
        RoleType r = c.new RoleType(42);

        assertEquals(6, b.calculate(3));
        assertEquals(6, b.delegation());

        c.bind(b, r);
        assertEquals(48, b.calculate(3));
        assertEquals(48, b.delegation());
    }
}
