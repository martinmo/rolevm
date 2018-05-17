package rolevm.examples.simple;

public class BaseType {
    private final String id;

    public BaseType(String id) {
        this.id = id;
    }

    public int calculate(int x) {
        System.out.println(this + "::calculate(" + x + ")");
        return x + 3;
    }

    public int delegation() {
        System.out.println(this + "::delegation()");
        return calculate(3);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + id + ")";
    }
}
