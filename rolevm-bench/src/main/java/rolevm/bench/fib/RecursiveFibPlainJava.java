package rolevm.bench.fib;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import rolevm.bench.DefaultBenchmark;
import rolevm.examples.fib.BenchmarkHelper;
import rolevm.examples.fib.RecursiveFibonacci;

/**
 * Measures plain-Java Fibonacci performance, i.e., when the RoleVM agent wasn't
 * loaded.
 */
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class RecursiveFibPlainJava extends DefaultBenchmark {
    @State(Scope.Benchmark)
    public static class Shared {
        @Param({ "20" })
        int n;
    }

    @State(Scope.Benchmark)
    public static class WithoutRole {
        RecursiveFibonacci fib;

        @Setup(Level.Iteration)
        public void setupIteration() {
            fib = new RecursiveFibonacci();
        }
    }

    @Benchmark
    public int without_role(Shared shared, WithoutRole state) {
        return BenchmarkHelper.computeFib(state.fib, shared.n);
    }
}
