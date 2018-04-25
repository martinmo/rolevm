package rolevm.runtime.linker;

import static java.lang.invoke.MethodType.methodType;
import static org.junit.Assert.assertEquals;
import static rolevm.runtime.linker.ProceedFactory.proceedHandle;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ProceedFactoryTest {
    private Core core;
    private RoleAlike roleAlike;

    @Before
    public void setUp() {
        core = new Core();
        roleAlike = new RoleAlike();
    }

    @Test
    public void proceedCombinator() throws Throwable {
        DispatchContext ctx = DispatchContext.ofRoles(roleAlike);
        MethodHandle proceed = proceedHandle(genericReceiver(RoleAlike.HANDLE));
        proceed.invoke(ctx, core, 3);
        assertEquals(List.of(ctx.next(), core, 3), roleAlike.calledWithArgs);
    }

    // utils

    /** Change the first parameter type of the given handle to {@link Object}. */
    static MethodHandle genericReceiver(MethodHandle mh) {
        return mh.asType(mh.type().changeParameterType(0, Object.class));
    }
}

// test classes

class Core {
}

class RoleAlike {
    List<Object> calledWithArgs = Collections.emptyList();

    void roleMethod(DispatchContext ctx, Core core, int arg) {
        calledWithArgs = List.of(ctx, core, arg);
    }

    public static final MethodHandle HANDLE;
    static {
        Lookup lookup = MethodHandles.lookup();
        try {
            HANDLE = lookup.findVirtual(RoleAlike.class, "roleMethod",
                    methodType(void.class, DispatchContext.class, Core.class, int.class));
        } catch (ReflectiveOperationException e) {
            throw (AssertionError) new AssertionError().initCause(e);
        }
    }
}
