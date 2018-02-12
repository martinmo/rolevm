package rolevm.examples.noop;

import rolevm.api.Base;
import rolevm.api.Compartment;
import rolevm.api.OverrideBase;
import rolevm.api.Role;

public class NoopCompartment extends Compartment {
    public @Role class NoopRole {
        private @Base BaseType base;

        @OverrideBase
        public Object noArgs() {
            return base.noArgs();
        }

        @OverrideBase
        public Object referenceArgAndReturn(Object o) {
            return base.referenceArgAndReturn(o);
        }

        @OverrideBase
        public int primitiveArgsAndReturn(int x, int y) {
            return base.primitiveArgsAndReturn(x, y);
        }
    }
}
