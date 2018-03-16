package rolevm.bench.fib;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import rolevm.examples.fib.BenchmarkHelper;
import rolevm.examples.fib.FibBenchmark;
import rolevm.examples.fib.FibBenchmark.CachedFib;
import rolevm.examples.fib.FibBenchmark.NoopFib;
import rolevm.examples.fib.RecursiveFibonacci;

/**
 * Measures Fibonacci performance with a loaded RoleVM agent, with and without a
 * bound {@link CachedFib} role.
 * 
 * @author Martin Morgenstern
 */
@Fork(jvmArgsAppend = { "@rolevm-bench/jvm.options" })
public class RecursiveFibWithAgent extends RecursiveFibPlainJava {
    @State(Scope.Benchmark)
    public static class WithCachingRole {
        RecursiveFibonacci fib;
        CachedFib cachedFib;
        FibBenchmark fibBench;

        @Setup(Level.Trial)
        public void setupTrial() {
            fibBench = new FibBenchmark();
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            fib = new RecursiveFibonacci();
            cachedFib = fibBench.bind(fib, fibBench.new CachedFib());
        }

        @TearDown(Level.Iteration)
        public void teardownIteration() {
            fibBench.unbind(fib, cachedFib);
        }
    }

    @State(Scope.Benchmark)
    public static class WithNoopRole {
        RecursiveFibonacci fib;
        NoopFib noopFib;
        FibBenchmark fibBench;

        @Setup(Level.Trial)
        public void setupTrial() {
            fibBench = new FibBenchmark();
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            fib = new RecursiveFibonacci();
            noopFib = fibBench.bind(fib, fibBench.new NoopFib());
        }

        @TearDown(Level.Iteration)
        public void teardownIteration() {
            fibBench.unbind(fib, noopFib);
        }
    }

    @State(Scope.Benchmark)
    public static class WithInvalidatedSwitchPoint {
        RecursiveFibonacci fib;

        @Setup(Level.Trial)
        public void setupTrial() {
            fib = new RecursiveFibonacci();
            FibBenchmark fibBench = new FibBenchmark();
            CachedFib cachedFib = fibBench.bind(fib, fibBench.new CachedFib());
            fibBench.unbind(fib, cachedFib);
        }
    }

    @Benchmark
    public int with_caching_role(Shared shared, WithCachingRole state) {
        return BenchmarkHelper.computeFib(state.fib, shared.n);
    }

    @Benchmark
    public int with_noop_role(Shared shared, WithNoopRole state) {
        return BenchmarkHelper.computeFib(state.fib, shared.n);
    }

    @Benchmark
    public int without_role_invalidated_sp(Shared shared, WithInvalidatedSwitchPoint state) {
        return BenchmarkHelper.computeFib(state.fib, shared.n);
    }
}
