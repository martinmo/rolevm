package rolevm.examples.fib;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import rolevm.api.Compartment;
import rolevm.api.DispatchContext;
import rolevm.api.OverrideBase;
import rolevm.api.Role;

public class FibBenchmark extends Compartment {
    private static final int CACHE_SIZE = 30;

    public @Role class CachedFib {
        private final Cache<Integer, Integer> cache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build();

        @OverrideBase
        public int fib(DispatchContext ctx, RecursiveFibonacci base, final int x) throws Throwable {
            Integer value = cache.getIfPresent(x);
            if (value != null) {
                return value;
            }
            value = (int) ctx.proceed().invoke(ctx, base, x);
            cache.put(x, value);
            return value;
        }
    }

    public @Role class NoopFib {
        @OverrideBase
        public int fib(DispatchContext ctx, RecursiveFibonacci base, int x) throws Throwable {
            return (int) ctx.proceed().invoke(ctx, base, x);
        }
    }

    /** Example of a faulty role implementation. */
    public @Role class FaultyCachedFib {
        private final Cache<Integer, Integer> cache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build();

        @OverrideBase
        public int fib(RecursiveFibonacci base, final int x) throws ExecutionException {
            // here, we use an anonymous inner class instead of a lambda
            return cache.get(x, new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    // this call will lead to infinite recursion, because `this`,
                    // aka the message sender, does not refer to the role
                    return base.fib(x);
                }
            });
        }
    }
}
