package rolevm.runtime.linker;

import static java.lang.invoke.MethodHandles.lookup;
import static org.junit.Assert.assertEquals;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ProceedHandleTest extends ProceedTestBase {
    private static final MethodType TYPE = RoleAlike.HANDLE.type().dropParameterTypes(0, 1);
    private static final MethodType ADAPTED_TYPE = TYPE.insertParameterTypes(0, MethodHandle.class);
    private ProceedInvocations factory;
    private MethodHandle proceed;
    private MethodHandle adaptedProceed;

    @Before
    public void setUp() {
        super.setUp();
        factory = new ProceedInvocations();
        proceed = factory.getInvocation(lookup(), "method", TYPE).getHandle();
        adaptedProceed = factory.getAdaptedInvocation(lookup(), "method", ADAPTED_TYPE).getHandle();
    }

    @Test
    public void proceedWithNoRole() throws Throwable {
        DispatchContext ctx = DispatchContext.ofRoles();
        proceed.invokeExact(ctx, core, 3);
        assertEquals(List.of(3), core.calledWithArgs);
    }

    @Test
    public void adaptedProceedWithNoRole() throws Throwable {
        DispatchContext ctx = DispatchContext.ofRoles();
        adaptedProceed.invokeExact((MethodHandle) null, ctx, core, 4);
        assertEquals(List.of(4), core.calledWithArgs);
    }

    @Test
    public void proceedWithRoles() throws Throwable {
        DispatchContext ctx = DispatchContext.ofRoles(roleAlike, anotherRoleAlike);
        proceed.invokeExact(ctx, core, 5);
        assertEquals(List.of(ctx.next(), core, 5), roleAlike.calledWithArgs);
        assertEquals(List.of(), anotherRoleAlike.calledWithArgs);
        assertEquals(List.of(), core.calledWithArgs);
    }
}
