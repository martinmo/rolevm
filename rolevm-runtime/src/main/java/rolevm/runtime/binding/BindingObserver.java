package rolevm.runtime.binding;

/**
 * Interface that must be implemented by classes that wish to observe binding
 * events.
 * 
 * @author Martin Morgenstern
 */
public interface BindingObserver {
    /** Called when a binding was added. */
    void bindingAdded(Object player, Object role);

    /** Called when a binding was removed. */
    void bindingRemoved(Object player, Object role);
}
