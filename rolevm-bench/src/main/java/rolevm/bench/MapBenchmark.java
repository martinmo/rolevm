package rolevm.bench;

import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class MapBenchmark extends DefaultBenchmark {
    private static final String CUSTOM_MAP_CLASSNAME = "rolevm.runtime.binder.ConcurrentWeakHashMap";
    private static final MethodHandle CUSTOM_MAP_CONSTRUCTOR = customMapConstructor();

    // the Maven project setup currently requires this, will be fixed soon
    private static MethodHandle customMapConstructor() {
        try {
            Class<?> customMapClass = Class.forName(CUSTOM_MAP_CLASSNAME);
            return MethodHandles.lookup().findConstructor(customMapClass, methodType(void.class));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError();
        }
    }

    @State(Scope.Benchmark)
    public static class IHM {
        Map<Object, Object> map;
        Object defaultValue, key;

        @Setup(Level.Iteration)
        public void setUp() {
            map = new IdentityHashMap<>();
            defaultValue = new Object();
            key = new Object();
        }
    }

    @State(Scope.Benchmark)
    public static class CWHM {
        Map<Object, Object> map;
        Object defaultValue, key;

        @Setup(Level.Iteration)
        public void setUp() throws Throwable {
            map = (Map<Object, Object>) CUSTOM_MAP_CONSTRUCTOR.invoke();
            defaultValue = new Object();
            key = new Object();
        }
    }

    @Benchmark
    public Object identityhashmap_get(IHM state) {
        return state.map.get(state.key);
    }

    @Benchmark
    public Object concurrentweakhashmap_get(CWHM state) {
        return state.map.get(state.key);
    }

    @Benchmark
    public Object identityhashmap_getdefault(IHM state) {
        return state.map.getOrDefault(state.key, state.defaultValue);
    }

    @Benchmark
    public Object concurrentweakhashmap_getdefault(CWHM state) {
        return state.map.getOrDefault(state.key, state.defaultValue);
    }
}
