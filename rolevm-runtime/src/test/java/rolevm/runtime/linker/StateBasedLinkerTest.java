package rolevm.runtime.linker;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;
import static jdk.dynalink.StandardOperation.CALL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.invoke.MethodHandle;

import org.junit.Before;
import org.junit.Test;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import rolevm.runtime.SomeCore;
import rolevm.runtime.TestCompartment;
import rolevm.runtime.TestCompartment.RoleForSomeCore;
import rolevm.runtime.binder.Binder;

public class StateBasedLinkerTest {
    private final CallSiteDescriptor descriptor = new CallSiteDescriptor(lookup(), CALL.named("someMethod"),
            methodType(int.class, SomeCore.class, int.class));
    private Binder binder;
    private StateBasedLinker linker;
    private SomeCore core;
    private RoleForSomeCore role;
    private LinkRequest request;

    @Before
    public void setUp() {
        binder = new Binder();
        linker = new StateBasedLinker(binder);
        core = new SomeCore();
        role = new TestCompartment().new RoleForSomeCore();
        request = mock(LinkRequest.class);
        when(request.getCallSiteDescriptor()).thenReturn(descriptor);
        when(request.getReceiver()).thenReturn(core);
    }

    @Test
    public void initiallyWithSwitchPoint() throws Exception {
        GuardedInvocation invocation = linker.getGuardedInvocation(request, null);
        assertNotNull(invocation.getSwitchPoints());
        assertFalse(invocation.hasBeenInvalidated());
        binder.bind(core, role);
        assertTrue(invocation.hasBeenInvalidated());
    }

    @Test
    public void withBoundRole() throws Throwable {
        binder.bind(core, role);
        GuardedInvocation invocation = linker.getGuardedInvocation(request, null);
        MethodHandle invocationHandle = invocation.getInvocation();
        MethodHandle guardHandle = invocation.getGuard();
        assertNull(invocation.getSwitchPoints());
        assertEquals(-3, (int) invocationHandle.invokeExact(core, 3));
        assertTrue((boolean) guardHandle.invoke(core));
    }

    @Test
    public void withUnboundRole() throws Throwable {
        binder.bind(core, role);
        binder.unbind(core, role);
        GuardedInvocation invocation = linker.getGuardedInvocation(request, null);
        MethodHandle invocationHandle = invocation.getInvocation();
        MethodHandle guardHandle = invocation.getGuard();
        assertNull(invocation.getSwitchPoints());
        assertEquals(4, (int) invocationHandle.invokeExact(core, 4));
        assertTrue((boolean) guardHandle.invoke(core));
    }

    @Test
    public void withMultipleRoles() throws Throwable {
        TestCompartment compartment = new TestCompartment();
        binder.bind(core, compartment.new ValidRole());
        binder.bind(core, compartment.new ValidRole());
        binder.bind(core, role);
        GuardedInvocation invocation = linker.getGuardedInvocation(request, null);
        MethodHandle invocationHandle = invocation.getInvocation();
        MethodHandle guardHandle = invocation.getGuard();
        assertNull(invocation.getSwitchPoints());
        assertEquals(-5, (int) invocationHandle.invokeExact(core, 5));
        assertTrue((boolean) guardHandle.invoke(core));
    }
}
