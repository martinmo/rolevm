package rolevm.examples.fib;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import rolevm.api.Base;
import rolevm.api.Compartment;
import rolevm.api.OverrideBase;
import rolevm.api.Role;

public class FibBenchmark extends Compartment {
    private static final int CACHE_SIZE = 30;

    public @Role class CachedFib {
        private final Cache<Integer, Integer> cache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build();
        private @Base RecursiveFibonacci base;

        @OverrideBase
        public int fib(final int x) throws ExecutionException {
            return cache.get(x, () -> base.fib(x));
        }
    }

    public @Role class NoopFib {
        private @Base RecursiveFibonacci base;

        @OverrideBase
        public int fib(int x) {
            return base.fib(x);
        }
    }

    /** Example of a faulty role implementation. */
    public @Role class FaultyCachedFib {
        private final Cache<Integer, Integer> cache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build();
        private @Base RecursiveFibonacci base;

        @OverrideBase
        public int fib(final int x) throws ExecutionException {
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
