package rolevm.bench.noop;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import rolevm.bench.DefaultBenchmark;
import rolevm.examples.noop.BaseType;
import rolevm.examples.noop.BenchmarkHelper;
import rolevm.examples.noop.NoopCompartment;
import rolevm.examples.noop.NoopCompartment.NoopRole;

@Fork(jvmArgsAppend = { "@rolevm-bench/jvm.options" })
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class NoopCompartmentBenchmark extends DefaultBenchmark {
    @State(Scope.Benchmark)
    public static class BenchState {
        int x, y;
        BaseType b;
        NoopRole r;
        NoopCompartment c;

        @Setup(Level.Trial)
        public void setupCompartment() {
            c = new NoopCompartment();
        }

        @Setup(Level.Iteration)
        public void setup() {
            b = new BaseType();
            r = c.bind(b, c.new NoopRole());
            Random random = new Random();
            x = random.nextInt();
            y = random.nextInt();
        }

        @TearDown(Level.Iteration)
        public void teardown() {
            c.unbind(b, r);
        }
    }

    @Benchmark
    public void baseline() {
        // empty
    }

    @Benchmark
    public Object basecall_noargs(BenchState s) {
        return BenchmarkHelper.performTest1(s.b);
    }

    @Benchmark
    public Object basecall_withargs(BenchState s) {
        return BenchmarkHelper.performTest2(s.b, s.b);
    }

    @Benchmark
    public int basecall_primitiveargs(BenchState s) {
        return BenchmarkHelper.performTest3(s.b, s.x, s.y);
    }
}
