package rolevm.examples.fib;

import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Functionally equivalent memoization of {@code fib()} calls without roles.
 * 
 * @author Martin Morgenstern
 */
public class PureJavaCachedFib {
    private static final int CACHE_SIZE = 30;
    private final RecursiveFibonacci fib = new RecursiveFibonacci();
    private final Cache<Integer, Integer> cache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build();

    public int fib(int x) throws ExecutionException {
        return cache.get(x, () -> fib.fib(x));
    }
}
