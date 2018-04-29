package rolevm.examples.fib;

import java.util.concurrent.ExecutionException;

public class BenchmarkHelper {
    public static int computeFib(final RecursiveFibonacci fib, final int x) {
        return fib.fib(x);
    }

    public static int computeCachedFib(final PureJavaCachedFib fib, final int x) throws ExecutionException {
        return fib.fib(x);
    }
}
