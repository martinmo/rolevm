package rolevm.transform;

import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.V1_7;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * Adapts classes using the {@link IndyMethodAdapter}.
 * 
 * @author Martin Morgenstern
 */
public class IndyClassAdapter extends ClassVisitor {
    private String className;

    public IndyClassAdapter(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (version < V1_7) {
            throw new OutdatedClassFormatError(version);
        }
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        return new IndyMethodAdapter(new MethodInfo(className, access, name, desc), mv);
    }
}
