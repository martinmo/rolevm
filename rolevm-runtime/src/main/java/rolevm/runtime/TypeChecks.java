package rolevm.runtime;

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

    /**
     * Validates the given role type and throws {@link RoleBindingException} if it
     * is invalid.
     */
    public static void validateRoleType(final Class<?> roleType) {
        final Class<?> compartmentType = roleType.getEnclosingClass();
        if (compartmentType == null || !COMPARTMENT_CLASS.isAssignableFrom(compartmentType)) {
            throw new RoleBindingException("a role type must be an inner class of a compartment");
        }
        if (roleType.getDeclaredAnnotationsByType(ROLE_ANNOTATION).length == 0) {
            throw new RoleBindingException("a role type must be annotated with @Role");
        }
    }

    /**
     * Validates the given player object and throws {@link RoleBindingException} if
     * it is invalid.
     */
    public static void validatePlayer(final Object player) {
        if (player.getClass().isArray()) {
            throw new RoleBindingException("an array cannot be a player");
        }
        if (isRole(player)) {
            throw new RoleBindingException("a role cannot be a player");
        }
    }

    /**
     * Returns {@code true} if the given object has the {@link rolevm.api.Role}
     * annotation.
     */
    public static boolean isRole(final Object object) {
        return object.getClass().getDeclaredAnnotation(ROLE_ANNOTATION) != null;
    }
}
