package rolevm.bench.blacklist;

public class FastBlacklistTest extends BlacklistTestBase {
    @Override
    Blacklist createBlacklist(String... prefixes) {
        return Blacklists.of(prefixes);
    }
}
