package rolevm.bench.blacklist;

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
 * Benchmark whether dynamic blacklists built with MethodHandles or "unrolled"
 * loops are worth the hassle, and also compare with a static one.
 * <p>
 * It is expected that the looping over a given string of prefixes is fast
 * enough.
 * <p>
 * In the end, the blacklist will be used to exclude classes from transformation
 * at load time.
 * 
 * @author Martin Morgenstern
 */
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class BlacklistBenchmark extends DefaultBenchmark {
    @State(Scope.Benchmark)
    public static class BlacklistsWithThreeEntries {
        Blacklist staticBlacklist, defaultBlacklist, fastBlacklist, indyBlacklist;

        @Setup(Level.Iteration)
        public void setup() {
            staticBlacklist = new Blacklist() {
                @Override
                public boolean isExcluded(final String name) {
                    return (name.startsWith("java/") || name.startsWith("org/openjdk/jmh/")
                            || name.startsWith("org/junit/"));
                }
            };
            defaultBlacklist = new DefaultBlacklist("java/", "org/openjdk/jmh/", "org/junit/");
            fastBlacklist = new DefaultBlacklist.Three("java/", "org/openjdk/jmh/", "org/junit/");
            indyBlacklist = new FastIndyBlacklist("java/", "org/openjdk/jmh/", "org/junit/");
        }
    }

    @State(Scope.Benchmark)
    public static class Data {
        @Param(value = { "java/lang/Runnable", "org/junit/Test", "com.github.martinmo" })
        String name;
    }

    @Benchmark
    public boolean baseline() {
        return true;
    }

    @Benchmark
    public boolean static_blacklist(Data d, BlacklistsWithThreeEntries l) {
        return l.staticBlacklist.isExcluded(d.name);
    }

    @Benchmark
    public boolean default_blacklist(Data d, BlacklistsWithThreeEntries l) {
        return l.defaultBlacklist.isExcluded(d.name);
    }

    @Benchmark
    public boolean fast_blacklist(Data d, BlacklistsWithThreeEntries l) {
        return l.fastBlacklist.isExcluded(d.name);
    }

    @Benchmark
    public boolean indy_blacklist(Data d, BlacklistsWithThreeEntries l) {
        return l.indyBlacklist.isExcluded(d.name);
    }
}
