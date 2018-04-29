package rolevm.examples.fib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RecursiveFibonacciTest {
    @Test
    public void withoutCaching() {
        checkValues(new RecursiveFibonacci());
    }

    @Test
    public void withCachedFib() {
        RecursiveFibonacci fib = new RecursiveFibonacci();
        FibBenchmark fibBench = new FibBenchmark();
        fibBench.bind(fib, fibBench.new CachedFib());
        checkValues(fib);
    }

    @Test
    public void withNoopFib() {
        RecursiveFibonacci fib = new RecursiveFibonacci();
        FibBenchmark fibBench = new FibBenchmark();
        fibBench.bind(fib, fibBench.new NoopFib());
        checkValues(fib);
    }

    public void checkValues(RecursiveFibonacci fib) {
        assertEquals(1, fib.fib(0));
        assertEquals(1, fib.fib(1));
        assertEquals(2, fib.fib(2));
        assertEquals(3, fib.fib(3));
        assertEquals(5, fib.fib(4));
        assertEquals(8, fib.fib(5));
        assertEquals(89, fib.fib(10));
    }
}
