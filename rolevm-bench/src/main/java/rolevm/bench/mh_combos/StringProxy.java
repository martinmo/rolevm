package rolevm.bench.mh_combos;

/**
 * Minimal no-op proxy for {@link String}.
 * 
 * @author Martin Morgenstern
 */
public class StringProxy {
    private final String base;

    public StringProxy(final String base) {
        this.base = base;
    }

    public int length() {
        return base.length();
    }
}
