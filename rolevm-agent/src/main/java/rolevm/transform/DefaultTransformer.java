package rolevm.transform;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rolevm.agent.RoleVMAgent;

/**
 * The default transformer that is installed by the {@link RoleVMAgent}.
 * 
 * @author Martin Morgenstern
 */
public class DefaultTransformer implements ClassFileTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultTransformer.class);
    private final StandardBlacklist blacklist;

    public DefaultTransformer(StandardBlacklist blacklist) {
        this.blacklist = blacklist;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (blacklist.isExcluded(className)) {
            return null;
        }
        LOG.trace("Transforming class {}", className);
        final ClassReader reader = new ClassReader(classfileBuffer);
        final ClassWriter writer = new ClassWriter(reader, 0);
        final ClassVisitor visitor = new IndyClassAdapter(writer);
        try {
            reader.accept(visitor, 0);
            return writer.toByteArray();
        } catch (OutdatedClassFormatError e) {
            LOG.warn("Skipping class with outdated format: {}", className);
        } catch (Throwable t) {
            LOG.error("Transformation failed: {}", className);
            LOG.error("Stacktrace:", t);
        }
        return null;
    }
}
