package rolevm.examples.noop;

import rolevm.api.Compartment;
import rolevm.api.DispatchContext;
import rolevm.api.OverrideBase;
import rolevm.api.Role;

public class NoopCompartment extends Compartment {
    public @Role class NoopRole {
        @OverrideBase
        public Object noArgs(DispatchContext ctx, BaseType base) throws Throwable {
            return ctx.proceed().invoke(ctx, base);
        }

        @OverrideBase
        public Object referenceArgAndReturn(DispatchContext ctx, BaseType base, Object o) throws Throwable {
            return ctx.proceed().invoke(ctx, base, o);
        }

        @OverrideBase
        public int primitiveArgsAndReturn(DispatchContext ctx, BaseType base, int x, int y) throws Throwable {
            return (int) ctx.proceed().invoke(ctx, base, x, y);
        }
    }
}
