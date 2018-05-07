package rolevm.runtime.dynalink;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.NamedOperation;
import jdk.dynalink.Operation;
import jdk.dynalink.linker.GuardingDynamicLinker;

public abstract class BaseLinker implements GuardingDynamicLinker {
    /**
     * Unwraps the method name from the {@link Operation} referenced by the given
     * {@link CallSiteDescriptor}.
     */
    protected static String unwrapMethodName(final CallSiteDescriptor descriptor) {
        final Operation operation = descriptor.getOperation();
        if (operation instanceof NamedOperation) {
            return ((NamedOperation) operation).getName().toString();
        }
        throw new AssertionError();
    }
}
