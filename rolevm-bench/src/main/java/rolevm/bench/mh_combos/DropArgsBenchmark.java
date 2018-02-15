package rolevm.bench.mh_combos;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import rolevm.bench.DefaultBenchmark;

/**
 * Benchmark the overhead of a sender argument inserted to a stack. Especially,
 * whether beginning or end of stack is faster. The sender argument would be
 * used in a special dispatch routine and discarded for the actual method
 * invocation.
 * 
 * @author Martin Morgenstern
 */
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class DropArgsBenchmark extends DefaultBenchmark {
    @State(Scope.Benchmark)
    public static class DropFirstArgHandleState {
        Object sender;
        MethodHandle target;

        @Setup(Level.Trial)
        public void setup() {
            target = MethodHandles.dropArguments(Handles.MH_STRLENGTH, 0, Object.class);
            sender = new Object();
        }
    }

    @State(Scope.Benchmark)
    public static class DropLastArgHandleState {
        Object sender;
        MethodHandle target;

        @Setup(Level.Trial)
        public void setup() {
            target = MethodHandles.dropArguments(Handles.MH_STRLENGTH, 1, Object.class);
            sender = new Object();
        }
    }

    @State(Scope.Benchmark)
    public static class StringState {
        String str;

        @Setup(Level.Iteration)
        public void setup() {
            str = new String("Luke, I am your father.");
        }
    }

    @Benchmark
    public int baseline() {
        return 0;
    }

    @Benchmark
    public int invoke_dropfirstarg(DropFirstArgHandleState hs, StringState ss) throws Throwable {
        return (int) hs.target.invokeExact(hs.sender, ss.str);
    }

    @Benchmark
    public int invoke_droplastarg(DropLastArgHandleState hs, StringState ss) throws Throwable {
        return (int) hs.target.invokeExact(ss.str, hs.sender);
    }
}
