package rolevm.runtime.linker;

import static java.lang.invoke.MethodType.methodType;
import static rolevm.runtime.linker.MethodHandleConversions.dropSenderArgument;
import static rolevm.runtime.linker.MethodHandleConversions.lookupType;
import static rolevm.runtime.linker.Utils.unwrapName;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collections;
import java.util.Objects;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.DynamicLinker;
import jdk.dynalink.DynamicLinkerFactory;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.support.Guards;
import jdk.dynalink.support.ChainedCallSite;
import rolevm.api.Role;

public class TrampolineFactory {
    private final DynamicLinker linker;
    private final MethodHandle nextRoleHandle;

    public TrampolineFactory(MethodHandle nextRoleHandle) {
        this.nextRoleHandle = Objects.requireNonNull(nextRoleHandle);
        linker = initLinker();
    }

    /**
     * Initializes the {@link jdk.dynalink} linker which is used to link the nested
     * {@link java.lang.invoke.CallSite}s inside trampolines.
     */
    private DynamicLinker initLinker() {
        DynamicLinkerFactory factory = new DynamicLinkerFactory();
        factory.setPrioritizedLinker(new TrampolineCallSiteLinker());
        factory.setFallbackLinkers(Collections.emptyList());
        return factory.createLinker();
    }

    /**
     * Creates a trampoline method with a floating
     * {@link java.lang.invoke.CallSite}, specialized for the given call site
     * description, and returns a {@link MethodHandle} to it.
     */
    public MethodHandle createTrampoline(final CallSiteDescriptor descriptor) {
        MethodType newType = descriptor.getMethodType().insertParameterTypes(0, Object.class);
        CallSiteDescriptor newDescriptor = descriptor.changeMethodType(newType);
        return MethodHandles.foldArguments(createCallSiteHandle(newDescriptor),
                adaptNextRoleHandle(descriptor.getMethodType()));
    }

    /**
     * Creates a method that can be used to bridge over a role instance that does
     * not implement the method given in the call site description.
     */
    private MethodHandle createBridgeMethod(final CallSiteDescriptor descriptor) {
        MethodType oldType = descriptor.getMethodType().dropParameterTypes(0, 1);
        MethodHandle handle = MethodHandles.foldArguments(createCallSiteHandle(descriptor),
                adaptNextRoleHandle(oldType));
        return MethodHandles.dropArguments(handle, 0, Object.class);
    }

    private MethodHandle adaptNextRoleHandle(final MethodType type) {
        Class<?> baseType = type.parameterType(0);
        MethodType newType = type.changeReturnType(Object.class);
        MethodHandle specificHandle = nextRoleHandle.asType(methodType(Object.class, Object.class, baseType));
        return MethodHandles.permuteArguments(specificHandle, newType, type.parameterCount() - 1, 0);
    }

    /* private */ MethodHandle createCallSiteHandle(final CallSiteDescriptor descriptor) {
        assert isCorrectType(descriptor);
        return linker.link(new ChainedCallSite(descriptor)).dynamicInvoker();
    }

    /**
     * Dynamic linker for the {@link java.lang.invoke.CallSite}s inside trampolines.
     */
    class TrampolineCallSiteLinker implements GuardingDynamicLinker {
        @Override
        public GuardedInvocation getGuardedInvocation(LinkRequest request, LinkerServices services)
                throws NoSuchMethodException, IllegalAccessException {
            CallSiteDescriptor descriptor = request.getCallSiteDescriptor();
            assert isCorrectType(descriptor);
            Class<?> dynamicReceiverType = request.getReceiver().getClass();
            Class<?> staticBaseType = descriptor.getMethodType().parameterType(1);
            if (isRoleType(dynamicReceiverType)) {
                return makeRoleInvocation(descriptor, dynamicReceiverType);
            }
            assert staticBaseType.isAssignableFrom(dynamicReceiverType);
            return makeBaseInvocation(descriptor, staticBaseType);
        }

        private GuardedInvocation makeRoleInvocation(final CallSiteDescriptor descriptor, final Class<?> owner) {
            try {
                MethodType lookupType = lookupType(descriptor.getMethodType());
                MethodHandle handle = descriptor.getLookup().findVirtual(owner, unwrapName(descriptor), lookupType);
                return newGuardedInvocation(handle, owner);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                return new GuardedInvocation(createBridgeMethod(descriptor), Guards.getInstanceOfGuard(owner));
            }
        }

        private GuardedInvocation makeBaseInvocation(final CallSiteDescriptor descriptor, final Class<?> owner)
                throws NoSuchMethodException, IllegalAccessException {
            MethodType lookupType = lookupType(descriptor.getMethodType()).dropParameterTypes(0, 1);
            MethodHandle handle = descriptor.getLookup().findVirtual(owner, unwrapName(descriptor), lookupType);
            return newGuardedInvocation(MethodHandles.dropArguments(handle, 0, owner), owner);
        }

        private GuardedInvocation newGuardedInvocation(final MethodHandle invocation, final Class<?> typeForGuard) {
            MethodHandle genericInvocation = invocation.asType(invocation.type().changeParameterType(0, Object.class));
            return new GuardedInvocation(dropSenderArgument(genericInvocation),
                    Guards.getInstanceOfGuard(typeForGuard));
        }
    }

    // utilities

    /** Returns true if the first argument type is {@link java.lang.Object}. */
    private static boolean isCorrectType(final CallSiteDescriptor descriptor) {
        return descriptor.getMethodType().parameterType(0).equals(Object.class);
    }

    /** Returns true if the given type is a role type. */
    private boolean isRoleType(final Class<?> type) {
        return type.getDeclaredAnnotation(Role.class) != null;
    }
}
