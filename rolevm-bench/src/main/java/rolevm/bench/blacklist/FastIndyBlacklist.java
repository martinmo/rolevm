package rolevm.bench.blacklist;

import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

/**
 * MH-based implementation, using a chain of guarded bound MHs, or specialized
 * bound handles for <code>prefixes.length &lt; 4</code>.
 * <p>
 * In other words, it is supposed to be some kind of JIT for a blacklist.
 * 
 * @author Martin Morgenstern
 */
class FastIndyBlacklist implements Blacklist {
    private static final MethodHandle MH_STARTSWITH;
    private static final MethodHandle MH_ISEXCLUDED_LL;
    private static final MethodHandle MH_ISEXCLUDED_LLL;
    private static final MethodHandle MH_ALWAYSTRUE;

    static {
        final Lookup lookup = MethodHandles.lookup();
        try {
            MH_STARTSWITH = lookup.findVirtual(String.class, "startsWith", methodType(Boolean.TYPE, String.class));
            MH_ISEXCLUDED_LL = lookup.findStatic(FastIndyBlacklist.class, "isExcluded_LL",
                    methodType(Boolean.TYPE, String.class, String.class, String.class));
            MH_ISEXCLUDED_LLL = lookup.findStatic(FastIndyBlacklist.class, "isExcluded_LLL",
                    methodType(Boolean.TYPE, String.class, String.class, String.class, String.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw (AssertionError) new AssertionError().initCause(e);
        }
        MH_ALWAYSTRUE = MethodHandles.dropArguments(MethodHandles.constant(Boolean.TYPE, true), 0, String.class);
    }

    private final MethodHandle mh;

    public FastIndyBlacklist(final String... prefixes) {
        mh = buildHandle(prefixes);
    }

    private static MethodHandle buildHandle(String[] prefixes) {
        if (prefixes.length == 0) {
            throw new IllegalArgumentException();
        }

        // prefixes.length == 1 implicitly handled below

        if (prefixes.length == 2) {
            return MethodHandles.insertArguments(MH_ISEXCLUDED_LL, 0, prefixes[0], prefixes[1]);
        }

        if (prefixes.length == 3) {
            return MethodHandles.insertArguments(MH_ISEXCLUDED_LLL, 0, prefixes[0], prefixes[1], prefixes[2]);
        }

        MethodHandle handle = MethodHandles.insertArguments(MH_STARTSWITH, 1, prefixes[prefixes.length - 1]);

        for (int i = prefixes.length - 2; i >= 0; --i) {
            MethodHandle test = MethodHandles.insertArguments(MH_STARTSWITH, 1, prefixes[i]);
            handle = MethodHandles.guardWithTest(test, MH_ALWAYSTRUE, handle);
        }

        return handle;
    }

    @SuppressWarnings("unused")
    private static boolean isExcluded_LL(final String prefix1, final String prefix2, final String s) {
        return s.startsWith(prefix1) || s.startsWith(prefix2);
    }

    @SuppressWarnings("unused")
    private static boolean isExcluded_LLL(final String prefix1, final String prefix2, final String prefix3,
            final String s) {
        return s.startsWith(prefix1) || s.startsWith(prefix2) || s.startsWith(prefix3);
    }

    @Override
    public boolean isExcluded(String name) {
        try {
            return (boolean) mh.invokeExact(name);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
