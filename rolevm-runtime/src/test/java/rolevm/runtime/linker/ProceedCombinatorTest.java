package rolevm.runtime.linker;

import static org.junit.Assert.assertEquals;

import java.lang.invoke.MethodHandle;
import java.util.List;

import org.junit.Test;

public class ProceedCombinatorTest extends ProceedTestBase {
    @Test
    public void proceedCombinator() throws Throwable {
        DispatchContext ctx = DispatchContext.ofRoles(roleAlike);
        MethodHandle proceed = ProceedInvocation.combineWithContext(genericReceiver(RoleAlike.HANDLE));
        proceed.invokeExact(ctx, core, 3);
        assertEquals(List.of(ctx.next(), core, 3), roleAlike.calledWithArgs);
    }
}
