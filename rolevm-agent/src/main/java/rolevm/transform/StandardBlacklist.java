package rolevm.transform;

public class StandardBlacklist {
    public boolean isExcluded(final String n) {
        return n.startsWith("java/") || n.startsWith("jdk/") || n.startsWith("sun/") || n.startsWith("rolevm/runtime/")
                || n.startsWith("rolevm/api/") || n.startsWith("rolevm/bench/") || n.startsWith("com/sun/")
                || n.startsWith("org/junit/") || n.startsWith("org/openjdk/jmh/");
    }
}
