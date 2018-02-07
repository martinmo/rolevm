package rolevm.agent;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import rolevm.transform.DumpingTransformer;
import rolevm.transform.DefaultTransformer;

public class RoleVMAgent {
    private static final Path PATH = Paths.get("target", "rolevm-transformed-classes");

    public static void premain(final String args, final Instrumentation ins) {
        ins.addTransformer(getTransformer("dump".equals(args)));
    }

    private static ClassFileTransformer getTransformer(final boolean dump) {
        if (dump) {
            try {
                Files.createDirectories(PATH);
                return new DumpingTransformer(PATH);
            } catch (final IOException e) {
                System.err.println("Could not create dump directory, dumping disabled.");
            }
        }
        return new DefaultTransformer();
    }
}
