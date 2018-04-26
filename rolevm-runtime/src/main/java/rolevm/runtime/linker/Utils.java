package rolevm.runtime.linker;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.NamedOperation;
import jdk.dynalink.Operation;

public class Utils {
    private Utils() {
    }

    /**
     * Unwraps the method name from the {@link Operation} referenced by the
     * {@link CallSiteDescriptor}.
     */
    public static String unwrapName(final CallSiteDescriptor descriptor) {
        Operation op = descriptor.getOperation();
        if (op instanceof NamedOperation) {
            return ((NamedOperation) op).getName().toString();
        }
        throw new AssertionError();
    }
}
