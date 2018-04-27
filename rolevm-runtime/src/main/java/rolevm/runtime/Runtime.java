package rolevm.runtime;

import static jdk.dynalink.StandardOperation.CALL;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.Collections;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.DynamicLinker;
import jdk.dynalink.DynamicLinkerFactory;
import jdk.dynalink.support.ChainedCallSite;
import rolevm.runtime.linker.StateBasedLinker;

/**
 * Sets up the RoleVM linker and provides the bootstrap method for the
 * {@code invokedynamic} instructions. The bootstrap method installs a call site
 * that implements polymorphic inline caching (PIC).
 * 
 * @see ChainedCallSite
 * @author Martin Morgenstern
 */
public class Runtime {
    private static final DynamicLinker dynamicLinker = createDynamicLinker();

    private static StateBasedLinker createStateBasedLinker() {
        return new StateBasedLinker(null);
    }

    private static DynamicLinker createDynamicLinker() {
        DynamicLinkerFactory factory = new DynamicLinkerFactory();
        factory.setPrioritizedLinker(createStateBasedLinker());
        factory.setFallbackLinkers(Collections.emptyList());
        return factory.createLinker();
    }

    public static CallSite bootstrap(Lookup lookup, String name, MethodType type) {
        return dynamicLinker.link(new ChainedCallSite(new CallSiteDescriptor(lookup, CALL.named(name), type)));
    }
}
