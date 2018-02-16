package rolevm.runtime.linker;

import static java.lang.invoke.MethodType.methodType;
import static rolevm.runtime.linker.MethodHandleConversions.dropSenderArgument;
import static rolevm.runtime.linker.MethodHandleConversions.lookupType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.SwitchPoint;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.NamedOperation;
import jdk.dynalink.Operation;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import rolevm.runtime.binder.Binder;
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
public class StateBasedLinker implements BindingObserver, GuardingDynamicLinker {
    private static final Map<String, MethodType> alwaysBaseMethods = objectMethods();
    private final SwitchPointManager switchpoints = new SwitchPointManager();
    private final Lookup lookup = MethodHandles.publicLookup();
    private final MethodHandle isPureObjectGuard;
    private final MethodHandle getRoleHandle;
    private final Binder binder;
    private LinkerState currentLinker = new InitialLinker();

    public StateBasedLinker(final Binder binder) {
        this.binder = Objects.requireNonNull(binder);
        binder.addObserver(this);
        binder.addObserver(switchpoints);
        isPureObjectGuard = Guards.createPureObjectGuard(binder);
        getRoleHandle = Handles.createGetRoleHandle(binder);
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
    public GuardedInvocation getGuardedInvocation(final LinkRequest request, final LinkerServices services)
            throws Exception {
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
     * Looks up target methods using ordinary Java
     * <code>invoke{virtual,interface}</code> semantics and returns a
     * {@link SwitchPoint}-guarded invocation that will be invalidated as soon as a
     * role binding affecting the call site is added.
     * <p>
     * This linker will be used initially, when no role binding is active.
     * 
     * @see SwitchPointManager
     */
    class InitialLinker extends LinkerState {
        @Override
        public GuardedInvocation getGuardedInvocation(final LinkRequest request, final LinkerServices services)
                throws Exception {
            CallSiteDescriptor desc = request.getCallSiteDescriptor();
            MethodType type = desc.getMethodType();
            String name = getOperationName(desc);
            MethodHandle mh = desc.getLookup().findVirtual(type.parameterType(0), name, lookupType(type));
            return new GuardedInvocation(dropSenderArgument(mh), switchpoints.get(type.parameterType(0)));
        }

        @Override
        public void bindingAdded(final Object player, final Object role) {
            currentLinker = new MainLinker();
        }
    }

    /**
     * Looks up target methods using role semantics, based on sender and receiver
     * identity, and returns reusable guarded invocations to take advantage of
     * inline caching.
     * 
     * @see Guards
     */
    class MainLinker extends LinkerState {
        @Override
        public GuardedInvocation getGuardedInvocation(final LinkRequest request, final LinkerServices services)
                throws Exception {
            CallSiteDescriptor desc = request.getCallSiteDescriptor();
            String name = getOperationName(desc);
            MethodType type = desc.getMethodType();
            Lookup lookup = desc.getLookup();
            Object role = binder.getRole(request.getReceiver());

            MethodType lookupType = lookupType(type);
            MethodHandle mh = lookup.findVirtual(type.parameterType(0), name, lookupType);

            if (role == null) {
                // not bound
                return new GuardedInvocation(dropSenderArgument(mh), isPureObjectGuard);
            }

            if (lookupType.equals(alwaysBaseMethods.get(name))) {
                // cannot be overridden by a role
                return new GuardedInvocation(dropSenderArgument(mh));
            }

            Object sender = request.getArguments()[type.parameterCount() - 1];
            if (role == sender) {
                // base call
                // TODO: handle multiple roles per player
                return new GuardedInvocation(dropSenderArgument(mh));
            }

            Class<?> roleType = role.getClass();
            return new GuardedInvocation(dropSenderArgument(maybeRoleHandle(roleType, name, lookupType, mh)),
                    Guards.createRoleTypePlayedByGuard(binder, roleType));
        }
    }

    /**
     * Tries to find an overriding method in the role type, matching the given name
     * and method type, and if found, returns a method handle that dynamically
     * replaces the original receiver with its bound role. Otherwise, if no
     * overriding method is found in the role type, the fallback method handle is
     * returned as-is.
     * 
     * @see MethodHandles#collectArguments(MethodHandle, int, MethodHandle)
     */
    private MethodHandle maybeRoleHandle(final Class<?> roleType, final String name, final MethodType lookupType,
            final MethodHandle fallback) {
        try {
            MethodHandle handle = lookup.findVirtual(roleType, name, lookupType);
            return MethodHandles.collectArguments(handle, 0, makeGetRoleHandle(roleType));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return fallback;
        }
    }

    /**
     * Returns the object-to-role mapping method handle, with its return type
     * adapted to the given role type. The resulting handle is intended to be used
     * as the filter method handle in
     * {@link MethodHandles#collectArguments(MethodHandle, int, MethodHandle)}.
     */
    private MethodHandle makeGetRoleHandle(final Class<?> roleType) {
        return getRoleHandle.asType(getRoleHandle.type().changeReturnType(roleType));
    }

    /**
     * Unwraps the method name from the {@link Operation} referenced by the
     * {@link CallSiteDescriptor}.
     */
    private static String getOperationName(final CallSiteDescriptor descriptor) {
        Operation op = descriptor.getOperation();
        if (op instanceof NamedOperation) {
            return ((NamedOperation) op).getName().toString();
        }
        throw new AssertionError();
    }

    /**
     * Return the names and method types of the methods in {@link Object}.
     */
    private static Map<String, MethodType> objectMethods() {
        Map<String, MethodType> namesAndType = new HashMap<>();
        for (Method method : Object.class.getMethods()) {
            namesAndType.put(method.getName(), methodType(method.getReturnType(), method.getParameterTypes()));
        }
        return namesAndType;
    }
}
