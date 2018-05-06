package rolevm.runtime.dynalink;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;
import static jdk.dynalink.StandardOperation.CALL;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.linker.LinkRequest;
import rolevm.runtime.SomeCore;
import rolevm.runtime.TestCompartment;
import rolevm.runtime.TestCompartment.RoleForSomeCore;
import rolevm.runtime.TestCompartment.ValidRole;

public abstract class DynalinkTestBase {
    protected final CallSiteDescriptor descriptor = new CallSiteDescriptor(lookup(), CALL.named("someMethod"),
            methodType(int.class, SomeCore.class, int.class));
    protected SomeCore core;
    protected RoleForSomeCore role1;
    protected ValidRole role2;
    protected LinkRequest request;
    protected LinkRequest noSuchMethodRequest;

    @Before
    public void setUp() {
        core = new SomeCore();
        role1 = new TestCompartment().new RoleForSomeCore();
        role2 = new TestCompartment().new ValidRole();
        request = mock(LinkRequest.class);
        when(request.getCallSiteDescriptor()).thenReturn(descriptor);
        when(request.getReceiver()).thenReturn(core);
        noSuchMethodRequest = mock(LinkRequest.class);
        when(noSuchMethodRequest.getCallSiteDescriptor())
                .thenReturn(descriptor.changeOperation(CALL.named("noSuchMethod")));
        when(noSuchMethodRequest.getReceiver()).thenReturn(core);
    }
}
