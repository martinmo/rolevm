package rolevm.runtime.dynalink;

import static rolevm.runtime.Bootstrap.unwrapMethodName;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.Objects;
import java.util.Optional;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.support.Guards;
import rolevm.api.DispatchContext;
import rolevm.runtime.binder.GuardedQuery;
import rolevm.runtime.binder.GuardedValue;
import rolevm.runtime.linker.ProceedInvocations;

public class StableLinker implements GuardingDynamicLinker {
    private final ProceedInvocations factory = new ProceedInvocations();
    private final GuardedQuery query;

    public StableLinker(final GuardedQuery query) {
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
        MethodType lookupType = callsiteType.dropParameterTypes(0, 1);
        String name = unwrapMethodName(descriptor);
        Lookup lookup = descriptor.getLookup();
        MethodHandle handle = lookup.findVirtual(callsiteType.parameterType(0), name, lookupType);
        Object receiver = request.getReceiver();
        GuardedValue<Optional<DispatchContext>> guardedContext = query.getGuardedDispatchContext(receiver);
        if (guardedContext.value().isPresent()) {
            MethodHandle proceed = factory
                    .getInvocation(lookup, name, callsiteType.insertParameterTypes(0, DispatchContext.class))
                    .getHandle();
            MethodHandle boundProceed = proceed.bindTo(guardedContext.value().get());
            return new GuardedInvocation(boundProceed, Guards.getIdentityGuard(receiver), guardedContext.switchpoint());
        }
        return new GuardedInvocation(handle, Guards.getIdentityGuard(receiver), guardedContext.switchpoint());
    }
}
