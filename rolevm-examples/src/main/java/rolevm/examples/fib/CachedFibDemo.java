package rolevm.examples.fib;

public class CachedFibDemo {
    public static void main(String[] args) {
        RecursiveFibonacci fib = new RecursiveFibonacci();
        demo("Uncached:", fib);

        FibBenchmark fibBench = new FibBenchmark();
        fibBench.bind(fib, fibBench.new CachedFib());
        demo("Cached:", fib);
    }

    public static void demo(String title, RecursiveFibonacci fib) {
        System.out.println(title);
        for (int i = 20; i <= 30; ++i) {
            System.out.printf("fib(%2d): %8d%n", i, fib.fib(i));
        }
    }
}
