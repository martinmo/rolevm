package rolevm.bench.ops;

import static java.lang.ClassLoader.getSystemResourceAsStream;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.IllegalClassFormatException;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import rolevm.bench.DefaultBenchmark;
import rolevm.examples.bank.Bank;
import rolevm.transform.DefaultTransformer;
import rolevm.transform.StandardBlacklist;

@Fork(jvmArgsAppend = { "@rolevm-bench/jvm.options" })
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class WeavingBenchmark extends DefaultBenchmark {
    DefaultTransformer transformer = new DefaultTransformer(new StandardBlacklist());
    Class<?> clazz;
    String className;
    byte[] classFile;

    @Setup(Level.Iteration)
    public void setup() throws IOException {
        clazz = Bank.class;
        className = clazz.getName().replace('.', '/');
        try (InputStream b = getSystemResourceAsStream(className + ".class")) {
            classFile = b.readAllBytes();
        }
    }

    @Benchmark
    public byte[] transform_class() throws IllegalClassFormatException {
        return transformer.transform(null, className, clazz, null, classFile);
    }
}
