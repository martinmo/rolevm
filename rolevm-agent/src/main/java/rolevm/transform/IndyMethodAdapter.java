package rolevm.transform;

import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static rolevm.transform.BootstrapConstants.BSM_CLASS;
import static rolevm.transform.BootstrapConstants.BSM_NAME;
import static rolevm.transform.BootstrapConstants.BSM_TYPE;

import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;

/**
 * Adapts <code>invoke{virtual,interface}</code> call sites to equivalent
 * <code>invokedynamic</code> call sites with an injected sender argument. The
 * sender argument is <code>this</code> in bodies of non-static methods,
 * otherwise the {@link Class} object of the surrounding class.
 * 
 * @author Martin Morgenstern
 */
public class IndyMethodAdapter extends MethodVisitor {
    private static final Handle BSM_HANDLE = new Handle(H_INVOKESTATIC, BSM_CLASS, BSM_NAME, BSM_TYPE, false);
    private final MethodInfo info;

    public IndyMethodAdapter(MethodInfo info, MethodVisitor mv) {
        super(ASM5, mv);
        this.info = info;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (opcode == INVOKEVIRTUAL || opcode == INVOKEINTERFACE) {
            visitInvokeDynamicInsn(name, adaptDescriptor(owner, desc), BSM_HANDLE);
        } else {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }

    /**
     * Adapts the method descriptor string to include the receiver type as the first
     * argument, and an additional sender argument at the last position.
     */
    static String adaptDescriptor(final String owner, final String desc) {
        final String ownerTypeDesc = owner.startsWith("[") ? owner : "L" + owner + ";";
        return "(" + ownerTypeDesc + desc.substring(1);
    }
}
