package rolevm.runtime.linker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.lang.invoke.MethodHandle;

import org.junit.Test;

public class DispatchContextTest {
    @Test
    public void empty() {
        DispatchContext ctx = DispatchContext.ofRoles();
        assertEquals(ctx.target(), null);
        assertEquals(ctx.next(), null);
    }

    @Test
    public void correctOrder() {
        DispatchContext ctx = DispatchContext.ofRoles(1, 2, 3);
        assertEquals(ctx.target(), 1);
        assertEquals(ctx.next().target(), 2);
        assertEquals(ctx.next().next().target(), 3);
        assertEquals(ctx.next().next().next().target(), null);
        assertEquals(ctx.next().next().next().next(), null);
    }

    @Test
    public void methodHandles() throws Throwable {
        DispatchContext ctx = DispatchContext.ofRoles(1, 2, 3);
        MethodHandle next = DispatchContext.NEXT_HANDLE;
        MethodHandle target = DispatchContext.TARGET_HANDLE;
        assertSame(target.invoke(ctx), ctx.target());
        assertSame(next.invoke(ctx), ctx.next());
    }

    @Test
    public void stringRepresentationEmpty() {
        DispatchContext ctx = DispatchContext.ofRoles();
        assertEquals(ctx.toString(), "DispatchContext[END]");
    }

    @Test
    public void stringRepresentation() {
        DispatchContext ctx = DispatchContext.ofRoles(1, 2, 3);
        assertEquals(ctx.toString(), "DispatchContext[1 -> 2 -> 3 -> END]");
    }
}
