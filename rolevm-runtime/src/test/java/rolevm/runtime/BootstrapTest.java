package rolevm.runtime;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;
import static org.junit.Assert.assertEquals;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import org.junit.Before;
import org.junit.Test;

import rolevm.api.DispatchContext;
import rolevm.runtime.TestCompartment.RoleForSomeCore;
import rolevm.runtime.binder.BinderFactory;
import rolevm.runtime.binder.Binder;

public class BootstrapTest {
    private final MethodType type = methodType(int.class, SomeCore.class, int.class);
    private final Binder binder = new BinderFactory().getBindingService();
    private SomeCore core;
    private RoleForSomeCore role;

    @Before
    public void setUp() {
        core = new SomeCore();
        role = new TestCompartment().new RoleForSomeCore();
    }

    @Test
    public void defaultCallSite() throws Throwable {
        CallSite cs = Bootstrap.defaultcall(lookup(), "someMethod", type);
        MethodHandle invoker = cs.dynamicInvoker();
        assertEquals(42, (int) invoker.invokeExact(core, 42));
        binder.bind(core, role);
        assertEquals(-451, (int) invoker.invokeExact(core, 451));
    }

    @Test
    public void proceedCallSite() throws Throwable {
        MethodType proceedType = type.insertParameterTypes(0, MethodHandle.class, DispatchContext.class);
        CallSite cs = Bootstrap.proceedcall(lookup(), "someMethod", proceedType);
        MethodHandle invoker = cs.dynamicInvoker();
        DispatchContext ctx = DispatchContext.ofRoles();
        assertEquals(1337, (int) invoker.invokeExact((MethodHandle) null, ctx, core, 1337));
    }
}
