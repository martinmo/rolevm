package rolevm.api;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import jdk.dynalink.linker.support.Lookup;

/**
 * Represents the runtime context for role method calls, which is expected as
 * the first stack argument to role methods, after {@code this}. The dispatch
 * context is an immutable, chained data structure that captures the roles to
 * which a particular method call must be delegated. Inside a role method body,
 * {@link #proceed()} can be used to specify a signature-polymorphic proceed
 * call to the next role, if any, in this chain.
 * 
 * @author Martin Morgenstern
 */
public final class DispatchContext {
    /** Represents the end of a chain. */
    public static final DispatchContext END = new DispatchContext();

    private static final Lookup lookup = new Lookup(MethodHandles.lookup());

    /**
     * Direct handle to the {@link DispatchContext#next} field (internal use only).
     */
    public static final MethodHandle NEXT_HANDLE;

    /**
     * Direct handle to the {@link DispatchContext#target} field (internal use
     * only).
     */
    public static final MethodHandle TARGET_HANDLE;

    static {
        Class<?> myself = DispatchContext.class;
        NEXT_HANDLE = lookup.findGetter(myself, "next", myself);
        TARGET_HANDLE = lookup.findGetter(myself, "target", Object.class);
    }

    /** The next context in the chain (may be <code>null</code>). */
    private final DispatchContext next;

    /** The target of this context (may be <code>null</code>). */
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

    /** Convenience method for {@link DispatchContext#of(List)}. */
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
     * Returns the next {@link DispatchContext} in the chain, or <code>null</code>
     * the end of the chain was reached (internal use only).
     */
    public DispatchContext next() {
        return next;
    }

    /**
     * Returns the next role of this dispatch context, or <code>null</code> if the
     * end of the chain was reached (internal use only).
     */
    public Object target() {
        return target;
    }

    /**
     * A marker method that can be used to specify a signature-polymorphic proceed
     * call to the next role, if any, or to the core object of this dispatch
     * context, using {@link MethodHandle#invoke(Object...)}. A proceed call is only
     * valid inside a role method. The proceed handle expects <em>exactly</em> the
     * same argument list as the surrounding method, i.e., at least the dispatch
     * context and the base argument as the first and second argument, respectively,
     * followed by any remaining arguments.
     * 
     * @see MethodHandle#invoke(Object...)
     * @implNote Calls to this marker method will be replaced by the RoleVM
     *           load-time transformation, so it never gets executed. Its sole
     *           purpose is to take advantage of signature polymorphism, which
     *           avoids boxing of method arguments.
     */
    public MethodHandle proceed() {
        throw new AssertionError();
    }

    /**
     * Returns a human-friendly flat representation of the chain, such as
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
