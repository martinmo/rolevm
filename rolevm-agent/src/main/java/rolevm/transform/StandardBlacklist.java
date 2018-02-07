package rolevm.transform;

/**
 * Contains the standard list of package prefixes that are excluded from
 * bytecode transformation.
 * 
 * @author Martin Morgenstern
 */
public class StandardBlacklist {
    public boolean isExcluded(final String n) {
        return n.startsWith("java/") || n.startsWith("jdk/") || n.startsWith("sun/") || n.startsWith("rolevm/runtime/")
                || n.startsWith("rolevm/api/") || n.startsWith("rolevm/bench/") || n.startsWith("com/sun/")
                || n.startsWith("org/junit/") || n.startsWith("org/openjdk/jmh/");
    }
}
