package rolevm.runtime.linker;

import static java.lang.invoke.MethodHandles.lookup;
import static org.junit.Assert.assertEquals;

import java.lang.invoke.MethodHandle;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ProceedCallSiteTest extends ProceedTestBase {
    private MethodHandle invoker;

    @Before
    public void setUp() {
        super.setUp();
        invoker = new Proceed().dynamicInvoker(lookup(), "method", genericReceiver(RoleAlike.HANDLE).type());
    }

    @Test
    public void invokeWithRole() throws Throwable {
        DispatchContext ctx = DispatchContext.ofRoles(roleAlike);
        invoker.invokeExact((Object) roleAlike, ctx, core, 42);
        assertEquals(List.of(ctx, core, 42), roleAlike.calledWithArgs);
    }

    @Test
    public void invokeWithAnotherRole() throws Throwable {
        DispatchContext ctx = DispatchContext.ofRoles(anotherRoleAlike);
        invoker.invokeExact((Object) anotherRoleAlike, ctx, core, 451);
        assertEquals(List.of(ctx, core, 451), anotherRoleAlike.calledWithArgs);
    }

    @Test
    public void invokeWithCore() throws Throwable {
        invoker.invokeExact((Object) null, (DispatchContext) null, core, 1337);
        assertEquals(List.of(1337), core.calledWithArgs);
    }

    @Test
    public void invokeMultipleTimes() throws Throwable {
        DispatchContext ctx1 = DispatchContext.ofRoles(roleAlike, anotherRoleAlike);
        invoker.invokeExact((Object) roleAlike, ctx1, core, 42);
        assertEquals(List.of(ctx1, core, 42), roleAlike.calledWithArgs);
        invoker.invokeExact((Object) anotherRoleAlike, ctx1, core, 451);
        assertEquals(List.of(ctx1, core, 451), anotherRoleAlike.calledWithArgs);
        invoker.invokeExact((Object) null, (DispatchContext) null, core, 1337);
        assertEquals(List.of(1337), core.calledWithArgs);
        DispatchContext ctx2 = DispatchContext.ofRoles(roleAlike, anotherRoleAlike);
        invoker.invokeExact((Object) roleAlike, ctx2, core, 43);
        assertEquals(List.of(ctx2, core, 43), roleAlike.calledWithArgs);
        invoker.invokeExact((Object) anotherRoleAlike, ctx2, core, 450);
        assertEquals(List.of(ctx2, core, 450), anotherRoleAlike.calledWithArgs);
        invoker.invokeExact((Object) null, (DispatchContext) null, core, 1336);
        assertEquals(List.of(1336), core.calledWithArgs);
    }

    @Test
    public void dynamicInvokerInvokeAnotherMethod() throws Throwable {
        DispatchContext ctx = DispatchContext.ofRoles(anotherRoleAlike);
        invoker = new Proceed().dynamicInvoker(lookup(), "anotherMethod",
                genericReceiver(RoleAlike.HANDLE).type());
        invoker.invokeExact((Object) anotherRoleAlike, ctx, core, 42);
        assertEquals(List.of(), anotherRoleAlike.calledWithArgs);
    }
}
