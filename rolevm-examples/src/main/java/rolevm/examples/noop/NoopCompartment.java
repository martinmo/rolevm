package rolevm.examples.noop;

import rolevm.api.Compartment;
import rolevm.api.OverrideBase;
import rolevm.api.Role;

public class NoopCompartment extends Compartment {
    public @Role class NoopRole {
        @OverrideBase
        public Object noArgs(BaseType base) {
            return base.noArgs();
        }

        @OverrideBase
        public Object referenceArgAndReturn(BaseType base, Object o) {
            return base.referenceArgAndReturn(o);
        }

        @OverrideBase
        public int primitiveArgsAndReturn(BaseType base, int x, int y) {
            return base.primitiveArgsAndReturn(x, y);
        }
    }
}
