package rolevm.examples.simple;

import rolevm.api.Compartment;
import rolevm.api.OverrideBase;
import rolevm.api.Role;

public class SimpleCompartment extends Compartment {
    public @Role class RoleType {
        private int y;

        public RoleType(int y) {
            this.y = y;
        }

        @OverrideBase
        public int calculate(BaseType base, int x) {
            return base.calculate(x) + y;
        }

        public int getY() {
            return y;
        }
    }

    public @Role class AnotherRoleType {
        @OverrideBase
        public int calculate(BaseType base, int x) {
            return -x;
        }
    }
}
