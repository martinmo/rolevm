package rolevm.runtime.binder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static rolevm.runtime.binder.TypeChecks.isRole;
import static rolevm.runtime.binder.TypeChecks.validatePlayer;
import static rolevm.runtime.binder.TypeChecks.validateRoleType;

import org.junit.Test;

import rolevm.api.Compartment;
import rolevm.api.Role;
import rolevm.api.RoleBindingException;
import rolevm.runtime.binder.TestComparment.TestRole;

public class TypeCheckTests {
    @Test
    public void basicChecks() {
        assertFalse(isRole("this is a string, not a role"));
        assertTrue(isRole(new TestComparment().new TestRole()));
    }

    @Test
    public void validityChecks() {
        validatePlayer(new Object());
        validateRoleType(TestRole.class);
    }

    @Test(expected = RoleBindingException.class)
    public void roleAsPlayerThrowsException() {
        validatePlayer(new TestComparment().new TestRole());
    }

    @Test(expected = RoleBindingException.class)
    public void unenclosedRoleThrowsException() {
        validateRoleType(InvalidRole.class);
    }
}

class TestComparment extends Compartment {
    public @Role class TestRole {
    }
}

@Role
class InvalidRole {
}
