package rolevm.examples.simple;

public class BaseType {
    public int calculate(int x) {
        return x + 3;
    }

    public int delegation() {
        return calculate(3);
    }
}
