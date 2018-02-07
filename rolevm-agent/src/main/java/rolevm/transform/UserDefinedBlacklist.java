package rolevm.transform;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.util.List;

/**
 * Allows to specify additional prefixes that should be excluded in addition to
 * the ones defined in {@link StandardBlacklist}. Currently unused.
 * 
 * @author Martin Morgenstern
 */
public class UserDefinedBlacklist extends StandardBlacklist {
    private final String[] blacklist;

    public UserDefinedBlacklist(final String blacklist) {
        this(stream(blacklist.split(",")).map(String::trim).collect(toList()));
    }

    public UserDefinedBlacklist(final List<String> blacklist) {
        this.blacklist = blacklist.toArray(new String[0]);
    }

    @Override
    public boolean isExcluded(final String name) {
        if (super.isExcluded(name)) {
            return true;
        }
        for (String prefix : blacklist) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
