package rolevm.examples.fib;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import rolevm.api.Compartment;
import rolevm.api.DispatchContext;
import rolevm.api.OverrideBase;
import rolevm.api.Role;

public class FastFib extends Compartment {
    public @Role class CachedFibonacci {
        private final Cache<Integer, Integer> cache;

        public CachedFibonacci(final int cacheSize) {
            cache = CacheBuilder.newBuilder().maximumSize(cacheSize).build();
        }

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

    /** Example of a faulty role implementation. */
    public @Role class FaultyCachedFibonacci {
        private final Cache<Integer, Integer> cache;

        public FaultyCachedFibonacci(final int cacheSize) {
            cache = CacheBuilder.newBuilder().maximumSize(cacheSize).build();
        }

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
