package rolevm.runtime.linker;

import static java.lang.invoke.MethodHandles.filterReturnValue;
import static java.lang.invoke.MethodHandles.foldArguments;
import static java.lang.invoke.MethodHandles.lookup;
import static rolevm.runtime.linker.Utils.JLO_METHODS;
import static rolevm.runtime.linker.Utils.unwrapName;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.SwitchPoint;
import java.util.Objects;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.support.Lookup;
import rolevm.runtime.binder.BinderNG;
import rolevm.runtime.binder.BindingObserver;

/**
 * State based linker that uses a "fast path" linking mechanism initially, when
 * no object/role mappings are established, and turns to a slower role-based
 * linking mechanism after the first role binding operation succeeded.
 * Furthermore, the initial linker installs {@link SwitchPoint}s that are
 * invalidated as lazily as possible in order to minimize the number of call
 * sites that have to relink using the slower linker.
 * <p>
 * The call sites linked using the initial linker are equivalent in speed to
 * ordinary <code>invoke{virtual,interface}</code> call sites.
 * 
 * @author Martin Morgenstern
 */
public class StateBasedLinkerNG implements BindingObserver, GuardingDynamicLinker {
    private final SwitchPointManager switchpoints = new SwitchPointManager();
    private final ProceedInvocations factory = new ProceedInvocations();
    private final MethodHandle isPureHandle;
    private final MethodHandle isNotPureHandle;
    private final MethodHandle getContextHandle;
    private final BinderNG binder;
    private LinkerState currentLinker = new InitialLinker();

    public StateBasedLinkerNG(final BinderNG binder) {
        this.binder = Objects.requireNonNull(binder);
        binder.addObserver(this);
        binder.addObserver(switchpoints);
        isNotPureHandle = binder.createContainsKeyHandle();
        isPureHandle = filterReturnValue(isNotPureHandle, NOT);
        getContextHandle = binder.createGetContextHandle();
    }

    public MethodHandle getIsPureHandle() {
        return isPureHandle;
    }

    public MethodHandle getIsNotPureHandle() {
        return isNotPureHandle;
    }

    private static final MethodHandle NOT = Lookup.findOwnStatic(lookup(), "not", boolean.class, boolean.class);

    @SuppressWarnings("unused") // used through a MethodHandle
    private static final boolean not(final boolean value) {
        return !value;
    }

    @Override
    public void bindingAdded(final Object player, final Object role) {
        currentLinker.bindingAdded(player, role);
    }

    @Override
    public void bindingRemoved(final Object player, final Object role) {
        currentLinker.bindingRemoved(player, role);
    }

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest request, LinkerServices services) throws Exception {
        return currentLinker.getGuardedInvocation(request, services);
    }

    abstract class LinkerState implements BindingObserver, GuardingDynamicLinker {
        @Override
        public void bindingAdded(Object player, Object role) {
            // optional
        }

        @Override
        public void bindingRemoved(Object player, Object role) {
            // optional
        }
    }

    /**
     * Looks up target methods using ordinary Java <code>invokevirtual</code> or
     * <code>invokeinterface</code> semantics and returns a {@link SwitchPoint}
     * guarded invocation that will be invalidated as soon as a role binding
     * affecting the call site is added.
     * <p>
     * This linker will be used initially, when no role binding is active.
     * 
     * @see SwitchPointManager
     */
    class InitialLinker extends LinkerState {
        @Override
        public GuardedInvocation getGuardedInvocation(LinkRequest request, LinkerServices unused) throws Exception {
            CallSiteDescriptor descriptor = request.getCallSiteDescriptor();
            MethodType type = descriptor.getMethodType();
            Class<?> receiverType = type.parameterType(0);
            MethodHandle handle = descriptor.getLookup().findVirtual(receiverType, unwrapName(descriptor), type);
            return new GuardedInvocation(handle, switchpoints.getSwitchPointForType(receiverType));
        }

        @Override
        public void bindingAdded(final Object player, final Object role) {
            currentLinker = new MainLinker();
        }
    }

    /**
     * Looks up target methods using role dispatch semantics.
     * <p>
     * This linker will be used as soon as the first binding is active.
     */
    class MainLinker extends LinkerState {
        @Override
        public GuardedInvocation getGuardedInvocation(LinkRequest request, LinkerServices unused) throws Exception {
            CallSiteDescriptor descriptor = request.getCallSiteDescriptor();
            Object receiver = request.getReceiver();
            String name = unwrapName(descriptor);
            MethodType type = descriptor.getMethodType();
            MethodType lookupType = type.dropParameterTypes(0, 1);
            Class<?> receiverType = type.parameterType(0);
            MethodHandle handle = descriptor.getLookup().findVirtual(receiverType, name, lookupType); // always execute
            if (binder.isPureObject(receiver)) {
                return new GuardedInvocation(handle, isPureHandle);
            }
            if (lookupType.equals(JLO_METHODS.get(name))) {
                return new GuardedInvocation(handle);
            }
            MethodHandle proceed = factory
                    .getInvocation(descriptor.getLookup(), name, type.insertParameterTypes(0, DispatchContext.class))
                    .getHandle();
            MethodHandle lifted = foldArguments(proceed, getContextHandle);
            return new GuardedInvocation(lifted.asType(type), isNotPureHandle);
        }
    }
}
