package rolevm.runtime.dynalink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.invoke.SwitchPoint;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import jdk.dynalink.linker.GuardedInvocation;
import rolevm.api.DispatchContext;
import rolevm.runtime.GuardedQuery;
import rolevm.runtime.GuardedValue;
import rolevm.runtime.SomeCore;
import rolevm.runtime.TestCompartment.RoleForSomeCore;

public class StableLinkerTest extends DynalinkTestBase {
    private StableLinker linker;
    private GuardedQuery query;
    private GuardedValue<Optional<DispatchContext>> guardedContext;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        super.setUp();
        query = mock(GuardedQuery.class);
        guardedContext = mock(GuardedValue.class);
        linker = new StableLinker(query);
    }

    @Test
    public void returnsNullWhenUnstable() throws Exception {
        when(request.isCallSiteUnstable()).thenReturn(true);
        assertNull(linker.getGuardedInvocation(request, null));
    }

    @Test(expected = NoSuchMethodException.class)
    public void failsWithNoSuchMethodException() throws Exception {
        when(noSuchMethodRequest.isCallSiteUnstable()).thenReturn(false);
        linker.getGuardedInvocation(noSuchMethodRequest, null);
    }

    @Test
    public void directInvocation() throws Throwable {
        SwitchPoint switchpoint = new SwitchPoint();
        when(request.isCallSiteUnstable()).thenReturn(false);
        when(query.getGuardedDispatchContext(core)).thenReturn(guardedContext);
        when(guardedContext.switchpoint()).thenReturn(switchpoint);
        when(guardedContext.value()).thenReturn(Optional.empty());
        GuardedInvocation invocation = linker.getGuardedInvocation(request, null);
        assertSame(switchpoint, invocation.getSwitchPoints()[0]);
        assertTrue((boolean) invocation.getGuard().invoke(core));
        assertFalse((boolean) invocation.getGuard().invoke(new SomeCore()));
        assertEquals(descriptor.getMethodType(), invocation.getInvocation().type());
        assertEquals(42, (int) invocation.getInvocation().invoke(core, 42));
    }

    @Test
    public void boundInvocation() throws Throwable {
        SwitchPoint switchpoint = new SwitchPoint();
        RoleForSomeCore mockedRole = mock(RoleForSomeCore.class);
        DispatchContext ctx = DispatchContext.ofRoles(mockedRole);
        when(request.isCallSiteUnstable()).thenReturn(false);
        when(query.getGuardedDispatchContext(core)).thenReturn(guardedContext);
        when(guardedContext.switchpoint()).thenReturn(switchpoint);
        when(guardedContext.value()).thenReturn(Optional.of(ctx));
        GuardedInvocation invocation = linker.getGuardedInvocation(request, null);
        assertSame(switchpoint, invocation.getSwitchPoints()[0]);
        assertTrue((boolean) invocation.getGuard().invoke(core));
        assertFalse((boolean) invocation.getGuard().invoke(new SomeCore()));
        assertEquals(descriptor.getMethodType(), invocation.getInvocation().type());
        invocation.getInvocation().invoke(core, 1337);
        verify(mockedRole).someMethod(ctx.next(), core, 1337);
    }
}
