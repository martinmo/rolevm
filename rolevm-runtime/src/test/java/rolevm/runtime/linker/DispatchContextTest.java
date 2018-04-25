package rolevm.runtime.linker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.lang.invoke.MethodHandle;

import org.junit.Test;

public class DispatchContextTest {
    @Test
    public void empty() {
        DispatchContext ctx = DispatchContext.ofRoles();
        assertEquals(null, ctx.target());
        assertEquals(null, ctx.next());
    }

    @Test
    public void correctOrder() {
        DispatchContext ctx = DispatchContext.ofRoles(1, 2, 3);
        assertEquals(1, ctx.target());
        assertEquals(2, ctx.next().target());
        assertEquals(3, ctx.next().next().target());
        assertEquals(null, ctx.next().next().next().target());
        assertEquals(null, ctx.next().next().next().next());
    }

    @Test
    public void methodHandles() throws Throwable {
        DispatchContext ctx = DispatchContext.ofRoles(1, 2, 3);
        MethodHandle next = DispatchContext.NEXT_HANDLE;
        MethodHandle target = DispatchContext.TARGET_HANDLE;
        assertSame(ctx.target(), target.invoke(ctx));
        assertSame(ctx.next(), next.invoke(ctx));
    }

    @Test
    public void stringRepresentationEmpty() {
        DispatchContext ctx = DispatchContext.ofRoles();
        assertEquals("DispatchContext[END]", ctx.toString());
    }

    @Test
    public void stringRepresentation() {
        DispatchContext ctx = DispatchContext.ofRoles(1, 2, 3);
        assertEquals("DispatchContext[1 -> 2 -> 3 -> END]", ctx.toString());
    }
}
