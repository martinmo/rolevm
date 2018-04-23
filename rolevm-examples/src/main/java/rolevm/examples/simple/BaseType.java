package rolevm.examples.simple;

public class BaseType {
    public int calculate(int x) {
        System.out.printf("BaseType(%s)::calculate()%n", this);
        return x + 3;
    }

    public int delegation() {
        System.out.printf("BaseType(%s)::delegation()%n", this);
        return calculate(3);
    }
}
