package rolevm.runtime.linker;

import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.DynamicLinker;
import jdk.dynalink.DynamicLinkerFactory;
import jdk.dynalink.NamedOperation;
import jdk.dynalink.Operation;
import jdk.dynalink.linker.GuardingDynamicLinker;

public class Utils {
    /** Map of names and method types of methods in {@link java.lang.Object}. */
    public static final Map<String, MethodType> JLO_METHODS = objectMethods();

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

    /**
     * Creates a Dynalink {@link DynamicLinker} linker using the given
     * {@code linker} and no fallback linkers.
     */
    public static DynamicLinker createDynamicLinker(GuardingDynamicLinker linker) {
        DynamicLinkerFactory factory = new DynamicLinkerFactory();
        factory.setPrioritizedLinker(linker);
        factory.setFallbackLinkers(Collections.emptyList());
        return factory.createLinker();
    }

    /**
     * Computes the names and method types of the methods in
     * {@link java.lang.Object}.
     */
    private static Map<String, MethodType> objectMethods() {
        Map<String, MethodType> objectMethods = new HashMap<>();
        for (Method m : Object.class.getMethods()) {
            objectMethods.put(m.getName(), methodType(m.getReturnType(), m.getParameterTypes()));
        }
        return Collections.unmodifiableMap(objectMethods);
    }
}
