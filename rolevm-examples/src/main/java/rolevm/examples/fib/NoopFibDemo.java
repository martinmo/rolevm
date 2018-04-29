package rolevm.examples.fib;

public class NoopFibDemo {
    public static void main(String[] args) {
        RecursiveFibonacci fib = new RecursiveFibonacci();
        FibBenchmark fibBench = new FibBenchmark();
        fibBench.bind(fib, fibBench.new NoopFib());
        System.out.println(fib.fib(5));
    }
}
