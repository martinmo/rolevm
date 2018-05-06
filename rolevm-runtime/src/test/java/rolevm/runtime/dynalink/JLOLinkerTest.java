package rolevm.runtime.dynalink;

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

import java.lang.invoke.MethodType;

import org.junit.Before;
import org.junit.Test;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;

public class JLOLinkerTest {
    private final CallSiteDescriptor objectEquals = new CallSiteDescriptor(lookup(), CALL.named("equals"),
            methodType(boolean.class, Object.class, Object.class));
    private LinkRequest request;
    private JLOLinker linker;

    @Before
    public void setUp() {
        request = mock(LinkRequest.class);
        linker = new JLOLinker();
    }

    @Test
    public void returnsUnconditionalInvocation() throws Throwable {
        when(request.getCallSiteDescriptor()).thenReturn(objectEquals);
        GuardedInvocation invocation = linker.getGuardedInvocation(request, null);
        assertNotNull(invocation);
        assertNull(invocation.getSwitchPoints());
        assertNull(invocation.getGuard());
        assertEquals(objectEquals.getMethodType(), invocation.getInvocation().type());
        assertTrue((boolean) invocation.getInvocation().invoke("foo", "foo"));
        assertFalse((boolean) invocation.getInvocation().invoke("foo", "bar"));
    }

    @Test
    public void whenReceiverTypeIsNotObject() throws Throwable {
        MethodType invocationType = methodType(boolean.class, CallSiteDescriptor.class, Object.class);
        when(request.getCallSiteDescriptor()).thenReturn(objectEquals.changeMethodType(invocationType));
        GuardedInvocation invocation = linker.getGuardedInvocation(request, null);
        assertNotNull(invocation);
        assertNull(invocation.getSwitchPoints());
        assertNull(invocation.getGuard());
        assertEquals(invocationType, invocation.getInvocation().type());
        assertTrue((boolean) invocation.getInvocation().invoke(objectEquals, objectEquals));
    }

    @Test
    public void returnsNullOtherwise() throws Exception {
        when(request.getCallSiteDescriptor()).thenReturn(objectEquals.changeOperation(CALL.named("nonJLOmethod")));
        GuardedInvocation invocation = linker.getGuardedInvocation(request, null);
        assertNull(invocation);
    }
}
