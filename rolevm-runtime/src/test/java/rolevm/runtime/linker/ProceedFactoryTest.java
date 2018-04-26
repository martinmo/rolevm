package rolevm.runtime.linker;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;
import static org.junit.Assert.assertEquals;
import static rolevm.runtime.linker.ProceedFactory.proceedHandle;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ProceedFactoryTest {
    private Core core;
    private RoleAlike roleAlike;
    private MethodHandle invoker;
    private AnotherRoleAlike anotherRoleAlike;

    @Before
    public void setUp() {
        core = new Core();
        roleAlike = new RoleAlike();
        anotherRoleAlike = new AnotherRoleAlike();
        invoker = new ProceedFactory().dynamicInvoker(lookup(), "method", genericReceiver(RoleAlike.HANDLE).type());
    }

    @Test
    public void proceedCombinator() throws Throwable {
        DispatchContext ctx = DispatchContext.ofRoles(roleAlike);
        MethodHandle proceed = proceedHandle(genericReceiver(RoleAlike.HANDLE));
        proceed.invokeExact(ctx, core, 3);
        assertEquals(List.of(ctx.next(), core, 3), roleAlike.calledWithArgs);
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
        invoker = new ProceedFactory().dynamicInvoker(lookup(), "anotherMethod",
                genericReceiver(RoleAlike.HANDLE).type());
        invoker.invokeExact((Object) anotherRoleAlike, ctx, core, 42);
        assertEquals(List.of(), anotherRoleAlike.calledWithArgs);
    }

    // utils

    /** Change the first parameter type of the given handle to {@link Object}. */
    static MethodHandle genericReceiver(MethodHandle mh) {
        return mh.asType(mh.type().changeParameterType(0, Object.class));
    }
}

// test classes

class Core {
    List<Object> calledWithArgs = List.of();

    void method(int arg) {
        calledWithArgs = List.of(arg);
    }
}

class RoleAlike {
    List<Object> calledWithArgs = List.of();

    void method(DispatchContext ctx, Core core, int arg) {
        calledWithArgs = List.of(ctx, core, arg);
    }

    public static final MethodHandle HANDLE;
    static {
        Lookup lookup = MethodHandles.lookup();
        try {
            HANDLE = lookup.findVirtual(RoleAlike.class, "method",
                    methodType(void.class, DispatchContext.class, Core.class, int.class));
        } catch (ReflectiveOperationException e) {
            throw (AssertionError) new AssertionError().initCause(e);
        }
    }
}

class AnotherRoleAlike {
    List<Object> calledWithArgs = List.of();

    void method(DispatchContext ctx, Core core, int arg) {
        calledWithArgs = List.of(ctx, core, arg);
    }

    void anotherMethod(DispatchContext ctx, Core core, int arg) {
    }
}
