package rolevm.examples.fib;

public class RecursiveFibonacci {
    public int fib(final int x) {
        if (x == 0 || x == 1) {
            return 1;
        }
        return fib(x - 1) + fib(x - 2);
    }
}
