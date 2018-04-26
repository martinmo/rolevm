package rolevm.runtime.binder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static rolevm.runtime.binder.TypeChecks.isRole;
import static rolevm.runtime.binder.TypeChecks.validatePlayer;
import static rolevm.runtime.binder.TypeChecks.validateRoleType;

import org.junit.Test;

import rolevm.api.RoleBindingException;
import rolevm.runtime.binder.TestCompartment.ValidRole;

public class TypeCheckTests {
    @Test
    public void basicChecks() {
        assertFalse(isRole("this is a string, not a role"));
        assertTrue(isRole(new TestCompartment().new ValidRole()));
    }

    @Test
    public void validityChecks() {
        validatePlayer(new Object());
        validateRoleType(ValidRole.class);
    }

    @Test(expected = RoleBindingException.class)
    public void roleAsPlayerThrowsException() {
        validatePlayer(new TestCompartment().new ValidRole());
    }

    @Test(expected = RoleBindingException.class)
    public void unenclosedRoleThrowsException() {
        validateRoleType(InvalidRole.class);
    }
}
