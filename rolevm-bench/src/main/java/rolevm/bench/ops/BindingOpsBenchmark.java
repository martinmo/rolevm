package rolevm.bench.ops;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import rolevm.bench.DefaultBenchmark;
import rolevm.examples.noop.BaseType;
import rolevm.examples.noop.NoopCompartment;
import rolevm.examples.noop.NoopCompartment.NoopRole;

@Fork(jvmArgsAppend = { "@rolevm-bench/jvm.options" })
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class BindingOpsBenchmark extends DefaultBenchmark {
    @Param({ "5" })
    int N;

    BaseType base;
    NoopCompartment noop;
    NoopRole[] roles;

    @Setup(Level.Iteration)
    public void setup() {
        noop = new NoopCompartment();
        base = new BaseType();
        roles = new NoopRole[N];
        for (int i = 0; i < N; i++) {
            roles[i] = noop.new NoopRole();
        }
    }

    @Benchmark
    public void bind_unbind_N_roles(Blackhole bh) {
        for (int i = 0; i < N; i++) {
            bh.consume(noop.bind(base, roles[i]));
        }
        for (int i = 0; i < N; i++) {
            bh.consume(noop.unbind(base, roles[i]));
        }
    }
}
