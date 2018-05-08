package rolevm.api;

import java.util.ServiceLoader;

import rolevm.api.service.BindingService;
import rolevm.api.service.BindingServiceFactory;

/**
 * Provides a scope for role types and basic binding operations for role-based
 * programming.
 * 
 * @see Role
 * @author Martin Morgenstern
 */
public class Compartment {
    private static final BindingService BINDER = initBindingService();

    /**
     * Discovers the BindingService, which will be provided by
     * {@link rolevm.runtime} using a {@link ServiceLoader}.
     */
    private static BindingService initBindingService() {
        ServiceLoader<BindingServiceFactory> loader = ServiceLoader.load(BindingServiceFactory.class);
        BindingServiceFactory factory = loader.findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find a BindingService"));
        return factory.getBindingService();
    }

    // TODO: implement the usual compartment stuff (activate, deactivate, ...)

    /**
     * Binds {@code role} to {@code player}, and returns {@code role}. Both
     * arguments must be distinct, non-null objects. Furthermore, the class of
     * {@code role} must be a valid role type, and player must not be a role type
     * (i.e., <em>deep roles</em> are not possible).
     * 
     * @param <T>
     *            the role type
     * @throws NullPointerException
     *             if any of the arguments is <code>null</code>
     * @throws IllegalArgumentException
     *             if {@code role == player}
     * @throws RoleBindingException
     *             if the player is not a valid player type, or if the role is not a
     *             valid role type
     * @return the given {@code role}
     */
    public final <T> T bind(final Object player, final T role) {
        BINDER.bind(player, role);
        return role;
    }

    /**
     * Unbinds {@code role} from {@code player}, if such a binding exists, and
     * returns {@code role}. If {@code role} is bound to {@code player} more than
     * once, only the first binding will be removed.
     * 
     * @param <T>
     *            the role type
     * @throws NullPointerException
     *             if any of the arguments is <code>null</code>
     * @return the given {@code role}
     */
    public final <T> T unbind(final Object player, final T role) {
        BINDER.unbind(player, role);
        return role;
    }
}
