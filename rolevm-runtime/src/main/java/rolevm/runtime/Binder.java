package rolevm.runtime;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Optional;

import rolevm.api.DispatchContext;
import rolevm.api.service.BindingService;

/**
 * The basic interface that a {@link DispatchContext}-based object/role manager
 * must conform to.
 * 
 * @author Martin Morgenstern
 */
public interface Binder extends BindingService {
    /** Returns a (possibly empty) list with the bound roles of {@code player}. */
    List<Object> getRoles(Object player);

    /**
     * Returns an {@link Optional} containing the {@link DispatchContext} for
     * {@code player}, or an empty Optional if {@code player} has no bound roles.
     */
    Optional<DispatchContext> getDispatchContext(Object player);

    /**
     * Returns true if and only if {@code player} has no bound roles.
     */
    boolean isPureObject(Object player);

    /**
     * Returns true if and only if {@code type} is a pure type, i.e., if there is
     * currently no registered player that is a subtype of {@code type}.
     */
    boolean isPureType(Class<?> type);

    /**
     * Returns a bound method handle of type {@code (Object)DispatchContext}. When
     * invoked, it returns a {@link DispatchContext} for the given object (the
     * returned value is never <code>null</code> for objects without roles, but
     * {@link DispatchContext#END} instead}.
     */
    MethodHandle createGetContextHandle();
}
