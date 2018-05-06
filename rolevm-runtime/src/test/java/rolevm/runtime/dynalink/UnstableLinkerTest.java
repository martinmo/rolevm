package rolevm.runtime.dynalink;

import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.empty;
import static java.lang.invoke.MethodType.methodType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.invoke.WrongMethodTypeException;

import org.junit.Test;

import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import rolevm.api.DispatchContext;
import rolevm.runtime.TestCompartment.RoleForSomeCore;

public class UnstableLinkerTest extends DynalinkTestBase {
    @Test(expected = WrongMethodTypeException.class)
    public void failsWithWrongMethodTypeException() {
        new UnstableLinker(empty(methodType(DispatchContext.class)));
    }

    @Test(expected = NoSuchMethodException.class)
    public void failsWithNoSuchMethodException() throws Exception {
        GuardingDynamicLinker linker = new UnstableLinker(empty(methodType(DispatchContext.class, Object.class)));
        linker.getGuardedInvocation(noSuchMethodRequest, null);
    }

    @Test
    public void directInvocation() throws Throwable {
        GuardingDynamicLinker linker = new UnstableLinker(
                dropArguments(constant(DispatchContext.class, DispatchContext.END), 0, Object.class));
        GuardedInvocation invocation = linker.getGuardedInvocation(request, null);
        assertNull(invocation.getGuard());
        assertNull(invocation.getSwitchPoints());
        assertEquals(descriptor.getMethodType(), invocation.getInvocation().type());
        assertEquals(42, (int) invocation.getInvocation().invoke(core, 42));
    }

    @Test
    public void foldedInvocation() throws Throwable {
        RoleForSomeCore mockedRole = mock(RoleForSomeCore.class);
        DispatchContext ctx = DispatchContext.ofRoles(mockedRole);
        GuardingDynamicLinker linker = new UnstableLinker(
                dropArguments(constant(DispatchContext.class, ctx), 0, Object.class));
        GuardedInvocation invocation = linker.getGuardedInvocation(request, null);
        assertNull(invocation.getGuard());
        assertNull(invocation.getSwitchPoints());
        assertEquals(descriptor.getMethodType(), invocation.getInvocation().type());
        invocation.getInvocation().invoke(core, 1337);
        verify(mockedRole).someMethod(ctx.next(), core, 1337);
    }
}
