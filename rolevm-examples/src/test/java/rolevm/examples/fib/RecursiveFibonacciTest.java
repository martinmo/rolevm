package rolevm.examples.fib;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class RecursiveFibonacciTest {
    @Test
    public void withoutCaching() {
        checkValues(new RecursiveFibonacci());
    }

    @Test
    public void withCaching() {
        RecursiveFibonacci fib = new RecursiveFibonacci();
        FastFib fastFib = new FastFib();
        fastFib.bind(fib, fastFib.new CachedFibonacci(20));
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
