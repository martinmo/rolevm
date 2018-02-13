package rolevm.bench.fib;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import rolevm.examples.fib.BenchmarkHelper;
import rolevm.examples.fib.FastFib;
import rolevm.examples.fib.FastFib.CachedFibonacci;
import rolevm.examples.fib.RecursiveFibonacci;

@Fork(value = 1, jvmArgsAppend = { "@rolevm-bench/jvm.options" })
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class RecursiveFibonacciBenchmark {
    @State(Scope.Benchmark)
    public static class Shared {
        @Param({ "20" })
        int n;
    }

    @State(Scope.Benchmark)
    public static class WithoutCaching {
        RecursiveFibonacci fib;

        @Setup(Level.Iteration)
        public void setupIteration() {
            fib = new RecursiveFibonacci();
        }
    }

    @State(Scope.Benchmark)
    public static class WithCaching {
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

    @Benchmark
    public void baseline() {
        // empty
    }

    @Benchmark
    public int with_caching(Shared shared, WithCaching state) {
        return BenchmarkHelper.computeFib(state.fib, shared.n);
    }

    @Benchmark
    public int without_caching(Shared shared, WithoutCaching state) {
        return BenchmarkHelper.computeFib(state.fib, shared.n);
    }
}
