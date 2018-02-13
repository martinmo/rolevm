package rolevm.examples.fib;

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
        public int fib(final int x) {
            Integer cached = cache.getIfPresent(x);
            if (cached != null)
                return cached;
            Integer result = base.fib(x);
            cache.put(x, result);
            return result;
        }
    }
}
