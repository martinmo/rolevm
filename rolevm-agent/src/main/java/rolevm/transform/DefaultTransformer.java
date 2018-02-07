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
    final static Logger logger = LoggerFactory.getLogger(DefaultTransformer.class);
    private final StandardBlacklist blacklist = new StandardBlacklist();

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (blacklist.isExcluded(className)) {
            return null;
        }
        logger.trace("Transforming class {}", className);
        final ClassReader reader = new ClassReader(classfileBuffer);
        final ClassWriter writer = new ClassWriter(reader, 0);
        final ClassVisitor visitor = new IndyClassAdapter(writer);
        try {
            reader.accept(visitor, 0);
            return writer.toByteArray();
        } catch (OutdatedClassFormatError e) {
            logger.warn("Class format of {} is too old, and thus not transformed");
        } catch (Throwable t) {
            logger.error("Transformation of {} failed", className);
            logger.error("Stacktrace:", t);
        }
        return null;
    }
}
