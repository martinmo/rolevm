package rolevm.bench.fib;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import rolevm.examples.fib.BenchmarkHelper;
import rolevm.examples.fib.FastFib;
import rolevm.examples.fib.FastFib.CachedFibonacci;
import rolevm.examples.fib.RecursiveFibonacci;

/**
 * Measures Fibonacci performance with a loaded RoleVM agent, with and without a
 * bound {@link CachedFibonacci} role.
 * 
 * @author Martin Morgenstern
 */
@Fork(value = 1, jvmArgsAppend = { "@rolevm-bench/jvm.options" })
public class RecursiveFibWithAgent extends RecursiveFibPlainJava {
    @State(Scope.Benchmark)
    public static class WithRole {
        RecursiveFibonacci fib;
        CachedFibonacci cachedFib;
        FastFib fastFib;

        @Setup(Level.Trial)
        public void setupTrial() {
            fastFib = new FastFib();
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            fib = new RecursiveFibonacci();
            cachedFib = fastFib.bind(fib, fastFib.new CachedFibonacci(30));
        }

        @TearDown(Level.Iteration)
        public void teardownIteration() {
            fastFib.unbind(fib, cachedFib);
        }
    }

    @State(Scope.Benchmark)
    public static class WithInvalidatedSwitchPoint {
        RecursiveFibonacci fib;

        @Setup(Level.Trial)
        public void setupTrial() {
            fib = new RecursiveFibonacci();
            FastFib fastFib = new FastFib();
            CachedFibonacci cachedFib = fastFib.bind(fib, fastFib.new CachedFibonacci(30));
            fastFib.unbind(fib, cachedFib);
        }
    }

    @Benchmark
    public int with_role(Shared shared, WithRole state) {
        return BenchmarkHelper.computeFib(state.fib, shared.n);
    }

    @Benchmark
    public int without_role_invalidated_sp(Shared shared, WithInvalidatedSwitchPoint state) {
        return BenchmarkHelper.computeFib(state.fib, shared.n);
    }
}
