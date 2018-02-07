package rolevm.bench.mh_combos;

import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * Benchmark "proxy" method handles created with
 * {@link MethodHandles#collectArguments(MethodHandle, int, MethodHandle)},
 * which transparently replace the receiver argument with a proxy.
 * 
 * @author Martin Morgenstern
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class CollectArgsBenchmark {
    @State(Scope.Benchmark)
    public static class BenchState {
        ProxyRegistry registry;
        String[] strings;
        MethodHandle collectTarget, plainTarget;
        int i;

        @Setup(Level.Trial)
        public void setup() {
            registry = new ProxyRegistry(1000);
            MethodHandle getProxy = registry.getGetProxyHandle();
            getProxy = getProxy.asType(methodType(StringProxy.class, String.class));
            collectTarget = MethodHandles.collectArguments(Handles.MH_STRLENGTH_PROXY, 0, getProxy);
            plainTarget = Handles.MH_STRLENGTH_PROXY;
            strings = registry.getKeys();
        }

        @Setup(Level.Iteration)
        public void initCounter() {
            i = 0;
        }
    }

    @Benchmark
    public int baseline() {
        return 0;
    }

    @Benchmark
    public int invoke_collecthandle(BenchState s) throws Throwable {
        return (int) s.collectTarget.invokeExact(s.strings[s.i++ % 1000]);
    }

    @Benchmark
    public int invoke_plainhandle(BenchState s) throws Throwable {
        StringProxy p = (StringProxy) s.registry.get(s.strings[s.i++ % 1000]);
        return (int) s.plainTarget.invokeExact(p);
    }

    @Benchmark
    public int invoke_plainjava(BenchState s) throws Throwable {
        StringProxy p = (StringProxy) s.registry.get(s.strings[s.i++ % 1000]);
        return p.length();
    }

    @Benchmark
    public int invoke_noproxy(BenchState s) {
        return s.strings[s.i++ % 1000].length();
    }
}
