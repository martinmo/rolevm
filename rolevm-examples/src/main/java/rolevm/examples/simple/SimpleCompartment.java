package rolevm.examples.simple;

import rolevm.api.Base;
import rolevm.api.Compartment;
import rolevm.api.OverrideBase;
import rolevm.api.Role;

public class SimpleCompartment extends Compartment {
    public @Role class RoleType {
        private @Base BaseType base;
        private int y;

        public RoleType(int y) {
            this.y = y;
        }

        @OverrideBase
        public int calculate(int x) {
            return base.calculate(x) + y;
        }

        public int getY() {
            return y;
        }
    }

    public @Role class AnotherRoleType {
        private @Base BaseType base;

        @OverrideBase
        public int calculate(int x) {
            return -x;
        }
    }
}
