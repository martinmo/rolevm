package rolevm.examples.simple;

import static org.junit.Assert.assertEquals;
import static rolevm.examples.simple.IOUtils.interceptSystemOut;
import static rolevm.examples.simple.IOUtils.record;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import rolevm.examples.simple.SimpleCompartment.AnotherRoleType;
import rolevm.examples.simple.SimpleCompartment.RoleType;

public class MultipleRoleFeatures {
    private SimpleCompartment compartment;
    private BaseType base;

    @Before
    public void setUp() {
        compartment = new SimpleCompartment();
        base = new BaseType("test");
    }

    @Test
    public void basicAssumptions() {
        String actual = interceptSystemOut(() -> {
            assertEquals(6, base.calculate(3));
            assertEquals(6, base.delegation());
        });
        String expected = record((writer) -> {
            writer.println("BaseType(test)::calculate(3)");
            writer.println("BaseType(test)::delegation()");
            writer.println("BaseType(test)::calculate(3)");
        });
        assertEquals(expected, actual);
    }

    @Test
    public void bindAndUnbind() {
        RoleType role = compartment.new RoleType(42);
        String actual = interceptSystemOut(() -> {
            compartment.bind(base, role);
            assertEquals(48, base.calculate(3));
            compartment.unbind(base, role);
            assertEquals(6, base.calculate(3));
        });
        String expected = record((writer) -> {
            writer.println("RoleType(42)::calculate(3)");
            writer.println("BaseType(test)::calculate(3)");
            writer.println("BaseType(test)::calculate(3)");
        });
        assertEquals(expected, actual);
    }

    @Test
    public void bindAndUnbindWithDelegation() {
        RoleType role = compartment.new RoleType(43);
        String actual = interceptSystemOut(() -> {
            compartment.bind(base, role);
            assertEquals(49, base.delegation());
            compartment.unbind(base, role);
            assertEquals(6, base.delegation());
        });
        String expected = record((writer) -> {
            writer.println("BaseType(test)::delegation()");
            writer.println("RoleType(43)::calculate(3)");
            writer.println("BaseType(test)::calculate(3)");
            writer.println("BaseType(test)::delegation()");
            writer.println("BaseType(test)::calculate(3)");
        });
        assertEquals(expected, actual);
    }

    @Test
    public void multipleRoles() {
        compartment.bind(base, compartment.new RoleType(3));
        compartment.bind(base, compartment.new RoleType(2));
        String actual = interceptSystemOut(() -> {
            assertEquals(11, base.calculate(3));
        });
        String expected = record((writer) -> {
            writer.println("RoleType(3)::calculate(3)");
            writer.println("RoleType(2)::calculate(3)");
            writer.println("BaseType(test)::calculate(3)");
        });
        assertEquals(expected, actual);
    }

    @Test
    public void multipleRolesWithDelegation() {
        compartment.bind(base, compartment.new RoleType(6));
        compartment.bind(base, compartment.new RoleType(4));
        String actual = interceptSystemOut(() -> {
            assertEquals(16, base.delegation());
        });
        String expected = record((writer) -> {
            writer.println("BaseType(test)::delegation()");
            writer.println("RoleType(6)::calculate(3)");
            writer.println("RoleType(4)::calculate(3)");
            writer.println("BaseType(test)::calculate(3)");
        });
        assertEquals(expected, actual);
    }

    @Test
    public void multipleRolesWithMissingMethod() {
        compartment.bind(base, compartment.new RoleType(11));
        compartment.bind(base, compartment.new EmptyRoleType()); // has no methods
        compartment.bind(base, compartment.new RoleType(12));
        String actual = interceptSystemOut(() -> {
            assertEquals(29, base.calculate(3));
        });
        String expected = record((writer) -> {
            // should print the same as the multipleRoles() test
            writer.println("RoleType(11)::calculate(3)");
            writer.println("RoleType(12)::calculate(3)");
            writer.println("BaseType(test)::calculate(3)");
        });
        assertEquals(expected, actual);
    }

    @Test
    public void multipleRolesWithDelegationAndTerminalMethod() {
        compartment.bind(base, compartment.new RoleType(9));
        compartment.bind(base, compartment.new AnotherRoleType("foo")); // terminates the chain
        compartment.bind(base, compartment.new RoleType(2));
        String actual = interceptSystemOut(() -> {
            assertEquals(6, base.delegation());
        });
        String expected = record((writer) -> {
            writer.println("BaseType(test)::delegation()");
            writer.println("RoleType(9)::calculate(3)");
            writer.println("AnotherRoleType(foo)::calculate(3)");
        });
        assertEquals(expected, actual);
    }

    @Test
    public void multipleRoleTypesPerCallSite() {
        RoleType role1 = compartment.new RoleType(42);
        AnotherRoleType role2 = compartment.new AnotherRoleType("bar");
        List<List<Object>> pairs = List.of(//
                List.of(role1, 48), List.of(role2, -3), List.of(role1, 48), List.of(role2, -3), List.of(role1, 48)//
        );
        for (List<Object> pair : pairs) {
            compartment.bind(base, pair.get(0));
            assertEquals(pair.get(1), base.calculate(3));
            assertEquals(pair.get(1), base.delegation());
            compartment.unbind(base, pair.get(0));
        }
    }
}
