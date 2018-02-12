package rolevm.examples.noop;

public class BaseType {
    public Object noArgs() {
        return this;
    }

    public Object referenceArgAndReturn(Object o) {
        return o;
    }

    public int primitiveArgsAndReturn(int x, int y) {
        return x + y;
    }
}
