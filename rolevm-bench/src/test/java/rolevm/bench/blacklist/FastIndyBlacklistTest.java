package rolevm.bench.blacklist;

import org.junit.Test;

public class FastIndyBlacklistTest extends BlacklistTestBase {
    @Override
    Blacklist createBlacklist(String... prefixes) {
        return new FastIndyBlacklist(prefixes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmpty() {
        new FastIndyBlacklist();
    }
}
