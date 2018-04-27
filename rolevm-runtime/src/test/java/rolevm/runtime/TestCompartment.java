package rolevm.runtime;

import rolevm.api.Compartment;
import rolevm.api.Role;
import rolevm.runtime.linker.DispatchContext;

public class TestCompartment extends Compartment {
    public @Role class ValidRole {
    }

    public @Role class RoleForSomeCore {
        public int someMethod(DispatchContext ctx, SomeCore core, int arg) {
            return -arg;
        }
    }
}
