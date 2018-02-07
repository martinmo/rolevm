package rolevm.api;

/**
 * Thrown when a binding operation cannot be executed, e.g., when reflective
 * operations fail, or if the {@link Base} and {@link Role} annotations have not
 * been used correctly. Note that this exception is unchecked.
 * 
 * @author Martin Morgenstern
 */
public class RoleBindingException extends RuntimeException {
    public RoleBindingException(String message) {
        super(message);
    }

    public RoleBindingException(String message, Throwable cause) {
        super(message, cause);
    }

    private static final long serialVersionUID = 0;
}
