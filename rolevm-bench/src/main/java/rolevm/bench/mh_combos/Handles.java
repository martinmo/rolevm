package rolevm.bench.mh_combos;

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
    public static final MethodHandle MH_STRLENGTH_PROXY;

    static {
        final Lookup lookup = MethodHandles.publicLookup();
        try {
            MH_STRLENGTH = lookup.findVirtual(String.class, "length", methodType(int.class));
            MH_STRLENGTH_PROXY = lookup.findVirtual(StringProxy.class, "length", methodType(int.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw (AssertionError) new AssertionError().initCause(e);
        }
    }
}
