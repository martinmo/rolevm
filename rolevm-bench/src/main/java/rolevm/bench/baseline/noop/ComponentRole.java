package rolevm.bench.baseline.noop;

public abstract class ComponentRole implements Component {
    private final ComponentCore core;

    protected ComponentRole(ComponentCore core) {
        this.core = core;
    }

    public static ComponentRole createFor(final String spec, final ComponentCore componentCore) {
        if ("NoopRole".equals(spec)) {
            return new NoopRole(componentCore);
        }
        throw new IllegalArgumentException(spec);
    }

    @Override
    public Object noArgs() {
        return core.noArgs();
    }

    @Override
    public Object referenceArgAndReturn(Object o) {
        return core.referenceArgAndReturn(o);
    }

    @Override
    public int primitiveArgsAndReturn(int x, int y) {
        return core.primitiveArgsAndReturn(x, y);
    }

    @Override
    public ComponentRole addRole(final String spec) {
        return core.addRole(spec);
    }

    @Override
    public ComponentRole getRole(final String spec) {
        return core.getRole(spec);
    }

    @Override
    public boolean hasRole(final String spec) {
        return core.hasRole(spec);
    }
}
