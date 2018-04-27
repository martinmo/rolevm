package rolevm.runtime;

import rolevm.api.Compartment;
import rolevm.api.DispatchContext;
import rolevm.api.Role;

public class TestCompartment extends Compartment {
    public @Role class ValidRole {
    }

    public @Role class RoleForSomeCore {
        public int someMethod(DispatchContext ctx, SomeCore core, int arg) {
            return -arg;
        }
    }
}
