package rolevm.bench.blacklist;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

abstract class BlacklistTestBase {
    abstract Blacklist createBlacklist(String... prefixes);

    @Test
    public void testSingle() {
        Blacklist blacklist = createBlacklist("java/");
        assertTrue(blacklist.isExcluded("java/lang/Runnable"));
        assertFalse(blacklist.isExcluded("rolevm/bench/blacklist/Blacklist"));
    }

    @Test
    public void testTwo() {
        Blacklist blacklist = createBlacklist("java/", "org/junit/");
        assertTrue(blacklist.isExcluded("java/lang/Runnable"));
        assertTrue(blacklist.isExcluded("org/junit/Test"));
        assertFalse(blacklist.isExcluded("rolevm/bench/blacklist/Blacklist"));
    }

    @Test
    public void testThree() {
        Blacklist blacklist = createBlacklist("java/", "org/junit/", "jdk/dynalink/");
        assertTrue(blacklist.isExcluded("java/lang/Runnable"));
        assertTrue(blacklist.isExcluded("org/junit/Test"));
        assertTrue(blacklist.isExcluded("jdk/dynalink/DynamicLinker"));
        assertFalse(blacklist.isExcluded("rolevm/bench/blacklist/Blacklist"));
    }

    @Test
    public void testMultiple() {
        Blacklist blacklist = createBlacklist("java/", "org/junit/", "jdk/dynalink/", "rolevm/bench/");
        assertTrue(blacklist.isExcluded("java/lang/Runnable"));
        assertTrue(blacklist.isExcluded("org/junit/Test"));
        assertTrue(blacklist.isExcluded("jdk/dynalink/DynamicLinker"));
        assertTrue(blacklist.isExcluded("rolevm/bench/blacklist/Blacklist"));
        assertFalse(blacklist.isExcluded("rolevm/examples/simple/SimpleCompartment"));
    }
}
