package rolevm.bench;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Warmup;

@Fork(2)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 20)
@Measurement(iterations = 10)
public abstract class DefaultBenchmark {
}
