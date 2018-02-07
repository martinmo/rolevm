package rolevm.runtime;

import rolevm.api.Base;
import rolevm.api.Compartment;
import rolevm.api.Role;

/**
 * Contains constants that reference the {@link rolevm.api} types and
 * annotations.
 * 
 * @author Martin Morgenstern
 */
public interface RoleTypeConstants {
    final Class<Base> BASE_ANNOTATION = Base.class;
    final Class<Role> ROLE_ANNOTATION = Role.class;
    final Class<Compartment> COMPARTMENT_CLASS = Compartment.class;
}
