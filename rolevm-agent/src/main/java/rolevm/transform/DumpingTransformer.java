package rolevm.transform;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.Objects;

/**
 * Transformer that dumps modified classes to a specified path.
 * 
 * @author Martin Morgenstern
 */
public class DumpingTransformer extends DefaultTransformer {
    private final Path directory;

    public DumpingTransformer(StandardBlacklist blacklist, final Path directory) {
        super(blacklist);
        this.directory = Objects.requireNonNull(directory);
    }

    @Override
    public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined,
            final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {
        final byte[] buffer = super.transform(loader, className, classBeingRedefined, protectionDomain,
                classfileBuffer);
        if (buffer != null) {
            write(className, buffer);
        }
        return buffer;
    }

    private void write(final String className, final byte[] contents) {
        final Path path = directory.resolve(className + ".class");
        try {
            Files.createDirectories(path.getParent());
            try (OutputStream s = new BufferedOutputStream(Files.newOutputStream(path))) {
                s.write(contents);
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
