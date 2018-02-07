package rolevm.examples.simple;

public class BaseType {
    public int calculate(int x) {
        return x + 3;
    }

    public int delegation() {
        // should return 10 if role is bound, 6 otherwise
        return calculate(3);
    }
}
