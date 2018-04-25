package rolevm.runtime.binder;

import static rolevm.runtime.RoleTypeConstants.COMPARTMENT_CLASS;
import static rolevm.runtime.RoleTypeConstants.ROLE_ANNOTATION;

import rolevm.api.RoleBindingException;

/**
 * Static utility methods to validate role types.
 * 
 * @author Martin Morgenstern
 */
public class TypeChecks {
    /** Should not be instantiated. */
    private TypeChecks() {
    }

    public static void validateRoleType(final Class<?> roleType) {
        final Class<?> compartmentType = roleType.getEnclosingClass();
        if (compartmentType == null || !COMPARTMENT_CLASS.isAssignableFrom(compartmentType)) {
            throw new RoleBindingException("a role type must be an inner class of a compartment");
        }
        if (roleType.getDeclaredAnnotationsByType(ROLE_ANNOTATION).length == 0) {
            throw new RoleBindingException("a role type must be annotated with @Role");
        }
    }

    public static void validatePlayer(final Object player) {
        if (isRole(player)) {
            throw new RoleBindingException("a role cannot be a player");
        }
    }

    public static boolean isRole(final Object object) {
        return object.getClass().getDeclaredAnnotation(ROLE_ANNOTATION) != null;
    }
}
