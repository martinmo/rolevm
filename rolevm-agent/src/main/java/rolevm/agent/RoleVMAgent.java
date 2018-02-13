package rolevm.agent;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import rolevm.transform.DefaultTransformer;
import rolevm.transform.DumpingTransformer;
import rolevm.transform.StandardBlacklist;
import rolevm.transform.UserDefinedBlacklist;

public class RoleVMAgent {
    private static final Path PATH = Paths.get("target", "rolevm-transformed-classes");

    public static void premain(final String args, final Instrumentation ins) {
        ins.addTransformer(createTransformer(System.getProperty("rolevm.exclude"), "dump".equals(args)));
    }

    private static ClassFileTransformer createTransformer(final String exclude, final boolean dump) {
        StandardBlacklist blacklist = createBlacklist(exclude);
        if (dump) {
            try {
                Files.createDirectories(PATH);
                return new DumpingTransformer(blacklist, PATH);
            } catch (final IOException e) {
                System.err.println("Could not create dump directory, dumping disabled.");
            }
        }
        return new DefaultTransformer(blacklist);
    }

    private static StandardBlacklist createBlacklist(final String exclude) {
        if (exclude != null && !exclude.trim().isEmpty()) {
            return new UserDefinedBlacklist(exclude);
        }
        return new StandardBlacklist();
    }
}
