package rolevm.runtime.linker;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;
import static jdk.dynalink.StandardOperation.CALL;
import static org.junit.Assert.assertEquals;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import org.junit.Before;
import org.junit.Test;

import jdk.dynalink.CallSiteDescriptor;
import rolevm.api.Role;

public class TrampolineCallSiteTest {
    private static final CallSiteDescriptor DESC = new CallSiteDescriptor(publicLookup(), CALL.named("m"),
            methodType(int.class, Object.class, B.class, String.class, Object.class));
    private final MethodHandle nextRole = MethodHandles.dropArguments(MethodHandles.identity(Object.class), 0,
            Object.class);

    private B b1, b2;
    private R1 r1_1, r1_2;
    private R2 r2;
    private R3 r3;

    private MethodHandle handle;

    @Before
    public void init() {
        handle = new TrampolineFactory(nextRole).createCallSiteHandle(DESC);
        b1 = new B();
        b2 = new B();
        r1_1 = new R1(3);
        r1_2 = new R1(6);
        r2 = new R2();
        r3 = new R3();
    }

    @Test
    public void baseInvocation() throws Throwable {
        assertEquals(3, (int) handle.invokeExact((Object) b1, b1, "foo", (Object) this));
        assertEquals(3, (int) handle.invokeExact((Object) b2, b2, "foo", (Object) this));
    }

    @Test
    public void roleInvocation() throws Throwable {
        assertEquals(6, (int) handle.invokeExact((Object) r1_1, b1, "foo", (Object) this));
        assertEquals(9, (int) handle.invokeExact((Object) r1_2, b1, "foo", (Object) this));
        assertEquals(2, (int) handle.invokeExact((Object) r2, b1, "foo", (Object) this));
    }

    @Test(expected = RuntimeException.class)
    public void roleInvocationMissingMethod() throws Throwable {
        assertEquals(3, (int) handle.invokeExact((Object) r3, b1, "foo", (Object) this));
    }

    @Test(expected = AssertionError.class)
    public void notfound() throws Throwable {
        @SuppressWarnings("unused")
        int i = (int) handle.invokeExact(new Object(), b1, "foo", (Object) this);
    }

    public static class B {
        public int m(String x) {
            return x.length();
        }
    }

    public static @Role class R1 {
        private final int c;

        public R1(final int c) {
            this.c = c;
        }

        public int m(B base, String x) {
            return base.m(x) + c;
        }
    }

    public static @Role class R2 {
        public int m(B base, String x) {
            return 2;
        }
    }

    public static @Role class R3 {
    }
}
