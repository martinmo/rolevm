package rolevm.bench.noop;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import rolevm.examples.person.BenchmarkHelper;
import rolevm.examples.person.NoopCompartment;
import rolevm.examples.person.NoopCompartment.AdvancedPerson;
import rolevm.examples.person.Person;

@Fork(value = 1, jvmArgsAppend = "-javaagent:rolevm-agent/target/rolevm-agent-1.0-SNAPSHOT.jar")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class NoopCompartmentBenchmark {
    @State(Scope.Benchmark)
    public static class BenchState {
        Person p1, p2;
        AdvancedPerson ap;
        NoopCompartment c;

        @Setup(Level.Trial)
        public void setupCompartment() {
            c = new NoopCompartment();
        }

        @Setup(Level.Iteration)
        public void setup() {
            p1 = new Person("Martin");
            p2 = new Person("Max");
            ap = c.bind(p1, c.new AdvancedPerson());
        }

        @TearDown(Level.Iteration)
        public void teardown() {
            c.unbind(p1, ap);
        }
    }

    @Benchmark
    public void baseline() {
        // empty
    }

    @Benchmark
    public String basecall_noargs(BenchState s) {
        return BenchmarkHelper.performTest1(s.p1);
    }

    @Benchmark
    public String basecall_withargs(BenchState s) {
        return BenchmarkHelper.performTest2(s.p1, s.p2);
    }
}
