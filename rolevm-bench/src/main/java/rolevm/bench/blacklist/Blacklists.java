package rolevm.bench.blacklist;

public final class Blacklists {
    private Blacklists() {
    }

    public static Blacklist of(String... prefixes) {
        if (prefixes.length == 0) {
            throw new IllegalArgumentException();
        }
        if (prefixes.length == 1) {
            return new DefaultBlacklist.One(prefixes[0]);
        }
        if (prefixes.length == 2) {
            return new DefaultBlacklist.Two(prefixes[0], prefixes[1]);
        }
        if (prefixes.length == 3) {
            return new DefaultBlacklist.Three(prefixes[0], prefixes[1], prefixes[2]);
        }
        if (prefixes.length < 15) {
            return new FastIndyBlacklist(prefixes);
        }
        return new DefaultBlacklist(prefixes);
    }
}
