package rolevm.bench.mh_guards;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.SwitchPoint;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import rolevm.bench.DefaultBenchmark;

/**
 * Benchmarks comparing the overheads of different kind of MH guards and switch
 * points.
 * 
 * Especially interesting is the penalty of a map-based guard (as implemented in
 * the {@link DummyRegistry}), because such a guard could be used in the runtime
 * of a role-based programming language.
 * 
 * The target method is always {@link String#length()}.
 * 
 * @author Martin Morgenstern
 */
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class GuardsBenchmark extends DefaultBenchmark {
    @State(Scope.Benchmark)
    public static class NormalHandleState {
        MethodHandle target;

        @Setup(Level.Trial)
        public void setup() {
            target = Handles.MH_STRLENGTH;
        }
    }

    @State(Scope.Benchmark)
    public static class MapGuardedHandleState {
        /** Size of the generated dummy hash map. */
        @Param({ "1000", "10000" })
        int size;

        MethodHandle target;

        @Setup(Level.Trial)
        public void setup() {
            MethodHandle test = new DummyRegistry(size).getGuardHandle();
            target = MethodHandles.guardWithTest(test, Handles.MH_STRLENGTH, Handles.MH_FALLBACK);
        }
    }

    @State(Scope.Benchmark)
    public static class TrueGuardedHandleState {
        MethodHandle target;

        @Setup(Level.Trial)
        public void setup() {
            target = MethodHandles.guardWithTest(Handles.MH_ALWAYSTRUE, Handles.MH_STRLENGTH, Handles.MH_FALLBACK);
        }
    }

    @State(Scope.Benchmark)
    public static class SwitchPointGuardedHandleState {
        MethodHandle target;

        @Setup(Level.Trial)
        public void setup() {
            SwitchPoint switchPoint = new SwitchPoint();
            target = switchPoint.guardWithTest(Handles.MH_STRLENGTH, Handles.MH_FALLBACK);
        }
    }

    @State(Scope.Benchmark)
    public static class StringState {
        String str;

        @Setup(Level.Iteration)
        public void setup() {
            str = new String("How much is the fish?");
        }
    }

    @Benchmark
    public int baseline() {
        return 0;
    }

    @Benchmark
    public int invoke_direct(StringState ss) {
        return ss.str.length();
    }

    @Benchmark
    public int invoke_normal_handle(StringState ss, NormalHandleState hs) throws Throwable {
        return (int) hs.target.invokeExact(ss.str);
    }

    @Benchmark
    public int invoke_mapguarded_handle(StringState ss, MapGuardedHandleState hs) throws Throwable {
        return (int) hs.target.invokeExact(ss.str);
    }

    @Benchmark
    public int invoke_noopguarded_handle(StringState ss, TrueGuardedHandleState hs) throws Throwable {
        return (int) hs.target.invokeExact(ss.str);
    }

    @Benchmark
    public int invoke_switchpointguarded_handle(StringState ss, SwitchPointGuardedHandleState hs) throws Throwable {
        return (int) hs.target.invokeExact(ss.str);
    }
}
