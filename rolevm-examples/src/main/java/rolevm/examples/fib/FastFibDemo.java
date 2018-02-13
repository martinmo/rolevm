package rolevm.examples.fib;

public class FastFibDemo {
    public static void main(String[] args) {
        RecursiveFibonacci fib = new RecursiveFibonacci();
        demo("Uncached:", fib);

        FastFib fastFib = new FastFib();
        fastFib.bind(fib, fastFib.new CachedFibonacci(20));
        demo("Cached:", fib);
    }

    public static void demo(String title, RecursiveFibonacci fib) {
        System.out.println(title);
        for (int i = 20; i <= 30; ++i) {
            System.out.printf("fib(%2d): %8d%n", i, fib.fib(i));
        }
    }
}
