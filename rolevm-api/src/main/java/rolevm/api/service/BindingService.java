package rolevm.api.service;

/**
 * Minimal service interface for communication with the RoleVM runtime.
 * 
 * @author Martin Morgenstern
 */
public interface BindingService {
    /**
     * Binds {@code role} to {@code player}.
     * 
     * @see rolevm.api.Compartment#bind(Object, Object)
     */
    void bind(Object player, Object role);

    /**
     * Unbinds {@code role} from {@code player}, if such a binding exists.
     * 
     * @see rolevm.api.Compartment#unbind(Object, Object)
     */
    void unbind(Object player, Object role);
}
