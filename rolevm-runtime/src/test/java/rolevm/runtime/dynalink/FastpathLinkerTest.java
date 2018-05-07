package rolevm.runtime.dynalink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.invoke.SwitchPoint;

import org.junit.Before;
import org.junit.Test;

import jdk.dynalink.linker.GuardedInvocation;
import rolevm.runtime.GuardedQuery;
import rolevm.runtime.GuardedValue;
import rolevm.runtime.SomeCore;

public class FastpathLinkerTest extends DynalinkTestBase {
    private FastpathLinker linker;
    private GuardedQuery query;
    private GuardedValue<Boolean> isPure;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        super.setUp();
        query = mock(GuardedQuery.class);
        isPure = mock(GuardedValue.class);
        linker = new FastpathLinker(query);
    }

    @Test
    public void returnsNullWhenUnstable() throws Exception {
        when(request.isCallSiteUnstable()).thenReturn(true);
        assertNull(linker.getGuardedInvocation(request, null));
    }

    @Test
    public void returnsNullWhenNotPure() throws Exception {
        when(request.isCallSiteUnstable()).thenReturn(false);
        when(isPure.value()).thenReturn(false);
        when(query.getGuardedIsPureType(SomeCore.class)).thenReturn(isPure);
        assertNull(linker.getGuardedInvocation(request, null));
    }

    @Test
    public void successCase() throws Throwable {
        when(request.isCallSiteUnstable()).thenReturn(false);
        SwitchPoint switchpoint = new SwitchPoint();
        when(isPure.value()).thenReturn(true);
        when(isPure.switchpoint()).thenReturn(switchpoint);
        when(query.getGuardedIsPureType(SomeCore.class)).thenReturn(isPure);
        GuardedInvocation invocation = linker.getGuardedInvocation(request, null);
        assertNotNull(invocation);
        assertSame(switchpoint, invocation.getSwitchPoints()[0]);
        assertNull(invocation.getGuard());
        assertEquals(descriptor.getMethodType(), invocation.getInvocation().type());
        assertEquals(42, (int) invocation.getInvocation().invoke(core, 42));
    }

    @Test(expected = NoSuchMethodException.class)
    public void failureCase() throws Exception {
        when(noSuchMethodRequest.isCallSiteUnstable()).thenReturn(false);
        when(isPure.value()).thenReturn(true);
        when(query.getGuardedIsPureType(SomeCore.class)).thenReturn(isPure);
        linker.getGuardedInvocation(noSuchMethodRequest, null);
    }
}
