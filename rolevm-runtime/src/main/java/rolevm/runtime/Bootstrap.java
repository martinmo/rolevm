package rolevm.runtime;

import static jdk.dynalink.StandardOperation.CALL;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.DynamicLinker;
import jdk.dynalink.NamedOperation;
import jdk.dynalink.Operation;
import jdk.dynalink.support.ChainedCallSite;
import rolevm.runtime.binder.BinderFactory;
import rolevm.runtime.binder.CacheAwareBinder;
import rolevm.runtime.dynalink.DynalinkLinkerBuilder;
import rolevm.runtime.proceed.ProceedInvocations;

/**
 * Sets up the RoleVM linker and provides the bootstrap methods for the
 * {@code invokedynamic} instructions.
 * 
 * @author Martin Morgenstern
 */
public class Bootstrap {
    /** Global {@link Binder} instance. */
    static final CacheAwareBinder THE_BINDER = new BinderFactory().getBindingService();

    /** The top level Dynalink linker used for default call sites. */
    private static final DynamicLinker dynamicLinker = initCompositeLinker();

    /** Factory for {@code proceed()} invocations. */
    private static final ProceedInvocations proceedFactory = new ProceedInvocations();

    private static DynamicLinker initCompositeLinker() {
        return new DynalinkLinkerBuilder()//
                .fromSystemProperties().withBinder(THE_BINDER).withGuardedQuery(THE_BINDER).build();
    }

    /** Initializes default call sites. */
    public static CallSite defaultcall(Lookup lookup, String name, MethodType type) {
        return dynamicLinker.link(new ChainedCallSite(new CallSiteDescriptor(lookup, CALL.named(name), type)));
    }

    /** Initializes proceed call sites. */
    public static CallSite proceedcall(Lookup lookup, String name, MethodType type) {
        return new ConstantCallSite(proceedFactory.getAdaptedInvocation(lookup, name, type).getHandle());
    }

    /** Unwraps the method name from the given {@link CallSiteDescriptor}. */
    public static String unwrapMethodName(final CallSiteDescriptor descriptor) {
        final Operation operation = descriptor.getOperation();
        if (operation instanceof NamedOperation) {
            return ((NamedOperation) operation).getName().toString();
        }
        throw new AssertionError();
    }
}
