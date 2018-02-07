package rolevm.examples.simple;

import rolevm.examples.simple.SimpleCompartment.RoleType;

public class SimpleCompartmentDemo {
    public static void main(String args[]) throws Exception {
        SimpleCompartment c = new SimpleCompartment();
        BaseType b = new BaseType();
        RoleType r = c.new RoleType(42);

        System.out.println(b.calculate(3));
        System.out.println(b.delegation());

        c.bind(b, r);
        System.out.println(b.calculate(3));
        System.out.println(b.delegation());
    }
}
