package rolevm.runtime.linker;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

/**
 * Immutable, chained dispatch context for multiple role dispatch.
 * 
 * @author Martin Morgenstern
 */
public class DispatchContext {
    /** End of the chain. */
    private static final DispatchContext END = new DispatchContext();

    /** Direct handle to the {@link DispatchContext#next} field. */
    static final MethodHandle NEXT_HANDLE;

    /** Direct handle to the {@link DispatchContext#target} field. */
    static final MethodHandle TARGET_HANDLE;

    static {
        Lookup lookup = MethodHandles.lookup();
        Class<?> myself = DispatchContext.class;
        try {
            NEXT_HANDLE = lookup.findGetter(myself, "next", myself);
            TARGET_HANDLE = lookup.findGetter(myself, "target", Object.class);
        } catch (ReflectiveOperationException e) {
            throw (AssertionError) new AssertionError().initCause(e);
        }
    }

    /** The next context in the chain (may be <code>null</code>). */
    private final DispatchContext next;

    /** The target of this context. */
    private final Object target;

    /** Builds a chain of {@link DispatchContext}s using the given list of roles. */
    public static DispatchContext of(List<Object> roles) {
        DispatchContext ctx = END;
        ListIterator<Object> iter = roles.listIterator(roles.size());
        while (iter.hasPrevious()) {
            ctx = new DispatchContext(iter.previous(), ctx);
        }
        return ctx;
    }

    /** Convenience factory for {@link DispatchContext#of(List)}. */
    public static DispatchContext ofRoles(Object... roles) {
        return of(List.of(roles));
    }

    /**
     * Private constructor used by {@link DispatchContext#of(List)}. The given
     * target and next must not be <code>null</code>.
     */
    private DispatchContext(Object target, DispatchContext next) {
        this.target = Objects.requireNonNull(target);
        this.next = Objects.requireNonNull(next);
    }

    /**
     * Private constructors solely used to create {@link DispatchContext#END}.
     */
    private DispatchContext() {
        this.target = null;
        this.next = null;
    }

    /**
     * Returns the next {@link DispatchContext} in this chain, or <code>null</code>.
     */
    public DispatchContext next() {
        return next;
    }

    /**
     * Returns the target (i.e., receiver object) of this dispatch context.
     */
    public Object target() {
        return target;
    }

    /**
     * Just a dummy method that allows for the static specification of
     * signature-polymorphic <code>ctx.proceed().invoke(...)</code> calls inside
     * roles using {@link MethodHandle#invoke(Object...)}.
     * 
     * @return <code>null</code>
     */
    public MethodHandle proceed() {
        return null;
    }

    /**
     * Print a human-friendly flat representation of the chain, such as
     * {@code DispatchContext[1 -> 2 -> 3 -> END]}.
     */
    @Override
    public String toString() {
        return "DispatchContext[" + toStringInternal() + "]";
    }

    /** Internal helper for {@link #toString()}. */
    private String toStringInternal() {
        if (this == END) {
            return "END";
        }
        return target + " -> " + next.toStringInternal();
    }
}
