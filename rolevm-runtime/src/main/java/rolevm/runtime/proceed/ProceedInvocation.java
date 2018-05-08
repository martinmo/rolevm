package rolevm.runtime.proceed;

import static java.lang.invoke.MethodHandles.filterArguments;
import static java.lang.invoke.MethodHandles.foldArguments;
import static jdk.dynalink.StandardOperation.CALL;
import static rolevm.api.DispatchContext.NEXT_HANDLE;
import static rolevm.api.DispatchContext.TARGET_HANDLE;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;
import java.util.Objects;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.DynamicLinker;
import jdk.dynalink.support.ChainedCallSite;
import rolevm.api.DispatchContext;

/**
 * Represents a {@code proceed} macro-instruction for a given method name and
 * method type. The method handle returned by {@link #getHandle()} executes the
 * following pseudo code:
 * <p>
 * 
 * <pre>
 * {@code Object role = context.target();
 * if (role != null)
 *   // will be bridged if necessary:
 *   return role.method(context.next(), base, ...);
 * return base.method(...);}
 * </pre>
 * 
 * @author Martin Morgenstern
 */
public class ProceedInvocation {
    private final DynamicLinker linker;
    private final CallSiteDescriptor descriptor;

    public ProceedInvocation(DynamicLinker linker, Lookup lookup, String name, MethodType type) {
        if (type.parameterCount() < 2 || type.parameterType(0) != DispatchContext.class) {
            throw new WrongMethodTypeException(type.toString());
        }
        MethodType callSiteType = type.insertParameterTypes(0, Object.class);
        this.descriptor = new CallSiteDescriptor(lookup, CALL.named(name), callSiteType);
        this.linker = Objects.requireNonNull(linker);
    }

    public MethodHandle getHandle() {
        return combineWithContext(callSiteInvoker());
    }

    MethodHandle callSiteInvoker() {
        return linker.link(new ChainedCallSite(descriptor)).dynamicInvoker();
    }

    static MethodHandle combineWithContext(final MethodHandle invoker) {
        MethodType type = invoker.type();
        if (type.parameterCount() < 3 || type.parameterType(0) != Object.class
                || type.parameterType(1) != DispatchContext.class) {
            throw new WrongMethodTypeException(type.toString());
        }
        return foldArguments(filterArguments(invoker, 1, NEXT_HANDLE), TARGET_HANDLE);
    }
}
