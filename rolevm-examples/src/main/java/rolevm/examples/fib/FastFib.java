package rolevm.examples.fib;

import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import rolevm.api.Base;
import rolevm.api.Compartment;
import rolevm.api.OverrideBase;
import rolevm.api.Role;

public class FastFib extends Compartment {
    public @Role class CachedFibonacci {
        private final Cache<Integer, Integer> cache;
        private @Base RecursiveFibonacci base;

        public CachedFibonacci(final int cacheSize) {
            cache = CacheBuilder.newBuilder().maximumSize(cacheSize).build();
        }

        @OverrideBase
        public int fib(final int x) throws ExecutionException {
            return cache.get(x, () -> base.fib(x));
        }
    }
}
