package rolevm.runtime;

import static jdk.dynalink.StandardOperation.CALL;
import static rolevm.runtime.linker.Utils.createDynamicLinker;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.DynamicLinker;
import jdk.dynalink.support.ChainedCallSite;
import rolevm.runtime.binder.Binder;
import rolevm.runtime.binder.BinderFactory;
import rolevm.runtime.binder.BinderNG;
import rolevm.runtime.linker.ProceedInvocations;
import rolevm.runtime.linker.StateBasedLinker;
import rolevm.runtime.linker.StateBasedLinkerNG;

/**
 * Sets up the RoleVM linker and provides the bootstrap methods for the
 * {@code invokedynamic} instructions.
 * 
 * @author Martin Morgenstern
 */
public class Bootstrap {
    /** The top level Dynalink linker used for default call sites. */
    private static final DynamicLinker dynamicLinker = createDynamicLinker(newStateBasedLinker());

    /** Factory for {@code proceed()} invocations. */
    private static final ProceedInvocations proceedFactory = new ProceedInvocations();

    /** Create a {@link StateBasedLinker} using the global {@link Binder}. */
    private static StateBasedLinkerNG newStateBasedLinker() {
        BinderNG binder = new BinderFactory().getBindingService();
        return new StateBasedLinkerNG(binder);
    }

    /** Initializes default call sites. */
    public static CallSite defaultcall(Lookup lookup, String name, MethodType type) {
        return dynamicLinker.link(new ChainedCallSite(new CallSiteDescriptor(lookup, CALL.named(name), type)));
    }

    /** Initializes proceed call sites. */
    public static CallSite proceedcall(Lookup lookup, String name, MethodType type) {
        return new ConstantCallSite(proceedFactory.getAdaptedInvocation(lookup, name, type).getHandle());
    }
}
