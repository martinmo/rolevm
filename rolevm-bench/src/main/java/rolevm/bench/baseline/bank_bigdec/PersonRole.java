package rolevm.bench.baseline.bank_bigdec;

public abstract class PersonRole implements Person {
    private final PersonCore core;

    protected PersonRole(final PersonCore core) {
        this.core = core;
    }

    public static PersonRole createFor(final String spec, final PersonCore personCore) {
        if ("Customer".equals(spec)) {
            return new Customer(personCore);
        }
        throw new IllegalArgumentException(spec);
    }

    @Override
    public PersonRole addRole(final String spec) {
        return core.addRole(spec);
    }

    @Override
    public PersonRole getRole(final String spec) {
        return core.getRole(spec);
    }

    @Override
    public boolean hasRole(final String spec) {
        return core.hasRole(spec);
    }
}
