package rolevm.examples.noop;

/**
 * Another level of indirection is needed because we exempt package rolevm.bench
 * from transformation.
 * 
 * @author Martin Morgenstern
 */
public class BenchmarkHelper {
    public static Object performTest1(BaseType b) {
        return b.noArgs();
    }

    public static Object performTest2(BaseType b, Object o) {
        return b.referenceArgAndReturn(o);
    }

    public static int performTest3(BaseType b, int x, int y) {
        return b.primitiveArgsAndReturn(x, y);
    }
}
