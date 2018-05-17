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
            System.out.println(this + "::calculate(" + x + ")");
            return (int) ctx.proceed().invoke(ctx, base, x) + y;
        }

        public int getY() {
            return y;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "(" + y + ")";
        }
    }

    public @Role class AnotherRoleType {
        private final String id;

        public AnotherRoleType(String id) {
            this.id = id;
        }

        @OverrideBase
        public int calculate(DispatchContext ctx, BaseType base, int x) {
            System.out.println(this + "::calculate(" + x + ")");
            return -x;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "(" + id + ")";
        }
    }

    public @Role class EmptyRoleType {
    }
}
