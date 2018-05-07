package rolevm.runtime.dynalink;

import static java.lang.invoke.MethodHandles.foldArguments;
import static java.lang.invoke.MethodType.methodType;
import static rolevm.runtime.Bootstrap.unwrapMethodName;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import rolevm.api.DispatchContext;
import rolevm.runtime.proceed.ProceedInvocations;

public class UnstableLinker implements GuardingDynamicLinker {
    private static final MethodType COMBINER_TYPE = methodType(DispatchContext.class, Object.class);
    private final ProceedInvocations factory = new ProceedInvocations();
    private final MethodHandle getContext;

    public UnstableLinker(final MethodHandle getContext) {
        if (!COMBINER_TYPE.equals(getContext.type())) { // forces NPE
            throw new WrongMethodTypeException(getContext + " should be of type " + COMBINER_TYPE);
        }
        this.getContext = getContext;
    }

    @Override
    public GuardedInvocation getGuardedInvocation(final LinkRequest request, final LinkerServices unused)
            throws Exception {
        CallSiteDescriptor descriptor = request.getCallSiteDescriptor();
        String name = unwrapMethodName(descriptor);
        MethodType callsiteType = descriptor.getMethodType();
        MethodType lookupType = callsiteType.dropParameterTypes(0, 1);
        Class<?> receiverType = callsiteType.parameterType(0);
        Lookup lookup = descriptor.getLookup();
        lookup.findVirtual(receiverType, name, lookupType); // fail early if core type has no such method
        MethodHandle proceed = factory
                .getInvocation(lookup, name, callsiteType.insertParameterTypes(0, DispatchContext.class)).getHandle();
        MethodHandle foldedProceed = foldArguments(proceed,
                getContext.asType(methodType(DispatchContext.class, receiverType)));
        return new GuardedInvocation(foldedProceed.asType(callsiteType));
    }
}
