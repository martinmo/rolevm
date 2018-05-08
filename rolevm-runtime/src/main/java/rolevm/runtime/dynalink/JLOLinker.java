package rolevm.runtime.dynalink;

import static java.lang.invoke.MethodType.methodType;
import static rolevm.runtime.Bootstrap.LOG;
import static rolevm.runtime.Bootstrap.unwrapMethodName;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;

public class JLOLinker implements GuardingDynamicLinker {
    /** Map of names and method types of methods in {@link java.lang.Object}. */
    private static final Map<String, MethodType> JLO_METHODS = objectMethods();

    @Override
    public GuardedInvocation getGuardedInvocation(final LinkRequest request, final LinkerServices unused)
            throws Exception {
        CallSiteDescriptor descriptor = request.getCallSiteDescriptor();
        MethodType callsiteType = descriptor.getMethodType();
        MethodType lookupType = callsiteType.dropParameterTypes(0, 1);
        String name = unwrapMethodName(descriptor);
        if (!lookupType.equals(JLO_METHODS.get(name))) {
            // we cannot link this request, try the next linker
            return null;
        }
        LOG.trace("JLO link for {}", descriptor);
        Lookup lookup = descriptor.getLookup();
        return new GuardedInvocation(lookup.findVirtual(callsiteType.parameterType(0), name, lookupType));
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
