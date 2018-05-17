package rolevm.bench.baseline;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import rolevm.bench.DefaultBenchmark;
import rolevm.bench.baseline.noop.Component;
import rolevm.bench.baseline.noop.ComponentCore;
import rolevm.bench.baseline.noop.NoopRole;

/**
 * Baseline for the noop method dispatch benchmarks, implemented with the Role
 * Object Pattern. The benchmark includes the code that is generally needed to
 * call role-specific behavior with that pattern, i.e., before the role method
 * is called, the the role object is retrieved with getRole() and a downcast is
 * performed.
 *
 * @author Martin Morgenstern
 */
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class NoopBaseline extends DefaultBenchmark {
    private int x, y;
    private Component b;

    @Setup(Level.Iteration)
    public void setup() {
        Random random = new Random();
        b = new ComponentCore();
        b.addRole("NoopRole");
        x = random.nextInt();
        y = random.nextInt();
    }

    @Benchmark
    public Object basecall_noargs() {
        NoopRole r = (NoopRole) b.getRole("NoopRole");
        return r.noArgs();
    }

    @Benchmark
    public Object basecall_withargs() {
        NoopRole r = (NoopRole) b.getRole("NoopRole");
        return r.referenceArgAndReturn(r);
    }

    @Benchmark
    public int basecall_primitiveargs() {
        NoopRole r = (NoopRole) b.getRole("NoopRole");
        return r.primitiveArgsAndReturn(x, y);
    }
}
