package rolevm.examples.simple;

import rolevm.api.Compartment;
import rolevm.api.DispatchContext;
import rolevm.api.OverrideBase;
import rolevm.api.Role;

public class SimpleCompartment extends Compartment {
    public @Role class RoleType {
        private int y;

        public RoleType(int y) {
            this.y = y;
        }

        @OverrideBase
        public int calculate(DispatchContext ctx, BaseType base, int x) throws Throwable {
            System.out.printf("RoleType(%s)::calculate()%n", this);
            return (int) ctx.proceed().invoke(ctx, base, x) + y;
        }

        public int getY() {
            return y;
        }
    }

    public @Role class AnotherRoleType {
        @OverrideBase
        public int calculate(DispatchContext ctx, BaseType base, int x) {
            System.out.printf("AnotherRoleType(%s)::calculate()%n", this);
            return -x;
        }
    }

    public @Role class EmptyRoleType {
    }
}
