package rolevm.examples.simple;

import rolevm.examples.simple.SimpleCompartment.RoleType;

public class SimpleCompartmentDemo {
    public static void main(String args[]) throws Exception {
        SimpleCompartment c = new SimpleCompartment();
        BaseType b = new BaseType("foo");
        RoleType r = c.new RoleType(42);

        System.out.println(b.calculate(3));
        System.out.println(b.delegation());

        c.bind(b, r);
        System.out.println("--- One role ---");
        System.out.println(b.calculate(3));
        System.out.println(b.delegation());

        c.bind(b, c.new RoleType(10));
        System.out.println("--- Two roles ---");
        System.out.println(b.calculate(3));
        System.out.println(b.delegation());

        c.bind(b, c.new AnotherRoleType("another1"));
        System.out.println("--- Three roles ---");
        System.out.println(b.calculate(3));
        System.out.println(b.delegation());

        c.bind(b, c.new EmptyRoleType());
        System.out.println("--- Four roles, but the 3rd ends the chain ---");
        System.out.println(b.calculate(3));
        System.out.println(b.delegation());

        BaseType b2 = new BaseType("bar");
        c.bind(b2, c.new RoleType(10));
        c.bind(b2, c.new EmptyRoleType());
        c.bind(b2, c.new AnotherRoleType("another2"));
        System.out.println("--- Three roles, the second must be bridged ---");
        System.out.println(b2.calculate(3));
        System.out.println(b2.delegation());
    }
}
