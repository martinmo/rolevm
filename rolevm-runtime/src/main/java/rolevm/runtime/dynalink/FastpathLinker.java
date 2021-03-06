package rolevm.runtime.dynalink;

import static rolevm.runtime.Bootstrap.LOG;
import static rolevm.runtime.Bootstrap.unwrapMethodName;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.Objects;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import rolevm.runtime.GuardedQuery;
import rolevm.runtime.GuardedValue;

/**
 * Component linker that that exclusively links stable call sites with a pure
 * receiver type, and guards the invocation with a type-specific switchpoint.
 * 
 * @author Martin Morgenstern
 */
public class FastpathLinker implements GuardingDynamicLinker {
    private final GuardedQuery query;

    public FastpathLinker(final GuardedQuery query) {
        this.query = Objects.requireNonNull(query);
    }

    @Override
    public GuardedInvocation getGuardedInvocation(final LinkRequest request, final LinkerServices unused)
            throws Exception {
        if (request.isCallSiteUnstable()) {
            return null;
        }
        CallSiteDescriptor descriptor = request.getCallSiteDescriptor();
        MethodType callsiteType = descriptor.getMethodType();
        Class<?> staticReceiverType = callsiteType.parameterType(0);
        GuardedValue<Boolean> isPure = query.getGuardedIsPureType(staticReceiverType);
        if (!isPure.value()) {
            return null;
        }
        LOG.trace("fastpath link for {}", descriptor);
        MethodHandle handle = descriptor.getLookup().findVirtual(staticReceiverType, unwrapMethodName(descriptor),
                callsiteType.dropParameterTypes(0, 1));
        return new GuardedInvocation(handle, isPure.switchpoint());
    }
}
