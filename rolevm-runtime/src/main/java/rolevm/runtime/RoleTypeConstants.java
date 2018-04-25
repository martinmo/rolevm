package rolevm.runtime;

import rolevm.api.Compartment;
import rolevm.api.Role;

/**
 * Contains constants that reference the {@link rolevm.api} types and
 * annotations.
 * 
 * @author Martin Morgenstern
 */
public class RoleTypeConstants {
    /** Should not be instantiated. */
    private RoleTypeConstants() {
    }

    public static final Class<Role> ROLE_ANNOTATION = Role.class;
    public static final Class<Compartment> COMPARTMENT_CLASS = Compartment.class;
}
