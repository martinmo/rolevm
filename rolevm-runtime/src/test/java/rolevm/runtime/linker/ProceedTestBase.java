package rolevm.runtime.linker;

import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;

import org.junit.Before;

import jdk.dynalink.linker.support.Lookup;
import rolevm.api.DispatchContext;

public abstract class ProceedTestBase {
    protected Core core;
    protected RoleAlike roleAlike;
    protected AnotherRoleAlike anotherRoleAlike;

    @Before
    public void setUp() {
        core = new Core();
        roleAlike = new RoleAlike();
        anotherRoleAlike = new AnotherRoleAlike();
    }

    // utils

    /** Change the first parameter type of the given handle to {@link Object}. */
    static MethodHandle genericReceiver(MethodHandle mh) {
        return mh.asType(mh.type().changeParameterType(0, Object.class));
    }

    // test classes

    static class Core {
        List<Object> calledWithArgs = List.of();

        void method(int arg) {
            calledWithArgs = List.of(arg);
        }
    }

    static class RoleAlike {
        List<Object> calledWithArgs = List.of();

        void method(DispatchContext ctx, Core core, int arg) {
            calledWithArgs = List.of(ctx, core, arg);
        }

        public static final MethodHandle HANDLE;
        static {
            Lookup lookup = new Lookup(MethodHandles.lookup());
            HANDLE = lookup.findVirtual(RoleAlike.class, "method",
                    methodType(void.class, DispatchContext.class, Core.class, int.class));
        }
    }

    static class AnotherRoleAlike {
        List<Object> calledWithArgs = List.of();

        void method(DispatchContext ctx, Core core, int arg) {
            calledWithArgs = List.of(ctx, core, arg);
        }

        void anotherMethod(DispatchContext ctx, Core core, int arg) {
        }
    }
}
