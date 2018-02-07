package rolevm.bench.mh_guards;

import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

/**
 * Common method handles used in the benchmarks.
 * 
 * @author Martin Morgenstern
 */
public class Handles {
    public static final MethodHandle MH_STRLENGTH;
    public static final MethodHandle MH_FALLBACK;
    public static final MethodHandle MH_ALWAYSTRUE;

    static {
        final Lookup lookup = MethodHandles.publicLookup();
        try {
            MH_STRLENGTH = lookup.findVirtual(String.class, "length", methodType(int.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw (AssertionError) new AssertionError().initCause(e);
        }

        MethodHandle fallback = MethodHandles.throwException(int.class, AssertionError.class)
                .bindTo(new AssertionError("should not be invoked"));
        MethodHandle alwaysTrue = MethodHandles.constant(boolean.class, true);

        MH_FALLBACK = MethodHandles.dropArguments(fallback, 0, String.class);
        MH_ALWAYSTRUE = MethodHandles.dropArguments(alwaysTrue, 0, String.class);
    }
}
