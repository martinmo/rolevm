package rolevm.runtime.proceed;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;
import static org.junit.Assert.assertEquals;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import rolevm.api.DispatchContext;

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

    @Test
    public void proceedMissingMethod() throws Throwable {
        DispatchContext ctx = DispatchContext.ofRoles(new RoleAlikeEmpty());
        proceed.invokeExact(ctx, core, 6);
        assertEquals(List.of(6), core.calledWithArgs);
    }

    @Test
    public void proceedMultipleMissingMethod() throws Throwable {
        DispatchContext ctx = DispatchContext.ofRoles(new RoleAlikeEmpty(), new RoleAlikeEmpty());
        proceed.invokeExact(ctx, core, 7);
        assertEquals(List.of(7), core.calledWithArgs);
    }

    @Test
    public void proceedMultipleMissingNonVoidMethods() throws Throwable {
        DispatchContext ctx = DispatchContext.ofRoles(new RoleAlikeEmpty(), new RoleAlikeEmpty());
        MethodType type = methodType(int.class, DispatchContext.class, Integer.class);
        proceed = factory.getInvocation(lookup(), "intValue", type).getHandle();
        int result = (int) proceed.invokeExact(ctx, Integer.valueOf(451));
        assertEquals(451, result);
    }

    @Test(expected = NoSuchMethodException.class)
    public void missingCoreMethod() throws Throwable {
        DispatchContext ctx = DispatchContext.ofRoles();
        proceed = factory.getInvocation(lookup(), "noSuchMethod", TYPE).getHandle();
        proceed.invokeExact(ctx, core, 8);
    }

    @Test(expected = NoSuchMethodException.class)
    public void eventuallyMissingCoreMethod() throws Throwable {
        DispatchContext ctx = DispatchContext.ofRoles(new RoleAlikeEmpty(), new RoleAlikeEmpty());
        proceed = factory.getInvocation(lookup(), "noSuchMethod", TYPE).getHandle();
        proceed.invokeExact(ctx, core, 9);
    }

    static class RoleAlikeEmpty {
        // no method named "method" -> will be bridged
    }
}
