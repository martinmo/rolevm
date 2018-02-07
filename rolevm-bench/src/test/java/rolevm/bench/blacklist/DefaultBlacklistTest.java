package rolevm.bench.blacklist;

public class DefaultBlacklistTest extends BlacklistTestBase {
    @Override
    Blacklist createBlacklist(String... prefixes) {
        return new DefaultBlacklist(prefixes);
    }
}
