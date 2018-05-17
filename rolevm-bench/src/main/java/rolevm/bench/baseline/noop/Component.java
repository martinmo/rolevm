package rolevm.bench.baseline.noop;

public interface Component {
    Object noArgs();

    Object referenceArgAndReturn(Object o);

    int primitiveArgsAndReturn(int x, int y);

    ComponentRole addRole(String spec);

    ComponentRole getRole(String spec);

    boolean hasRole(String spec);
}
