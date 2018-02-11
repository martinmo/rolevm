package rolevm.runtime.binder;

import java.lang.reflect.Field;

import rolevm.api.RoleBindingException;
import rolevm.runtime.RoleTypeConstants;

/**
 * Static utility methods to validate role types.
 * 
 * @author Martin Morgenstern
 */
public class TypeChecks implements RoleTypeConstants {
    private TypeChecks() {
    }

    public static Class<?> validateRoleType(final Class<?> roleType) {
        final Class<?> compartmentType = roleType.getEnclosingClass();
        if (compartmentType == null || !COMPARTMENT_CLASS.isAssignableFrom(compartmentType)) {
            throw new RoleBindingException("a role type must be an inner class of a compartment");
        }
        if (roleType.getDeclaredAnnotationsByType(ROLE_ANNOTATION).length == 0) {
            throw new RoleBindingException("a role type must be annotated with @Role");
        }
        return roleType;
    }

    public static void validatePlayer(final Object player) {
        if (player.getClass().getDeclaredAnnotationsByType(ROLE_ANNOTATION).length != 0) {
            throw new RoleBindingException("a role cannot be a player");
        }
    }

    public static void checkFieldAssignment(final Field field, final Object obj) {
        if (!field.getType().isAssignableFrom(obj.getClass())) {
            throw new RoleBindingException("incompatible assignment of player to role base");
        }
        field.setAccessible(true);
    }
}
