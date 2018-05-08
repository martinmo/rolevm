package rolevm.transform;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.POP;
import static rolevm.transform.BootstrapConstants.BSM_CLASS;
import static rolevm.transform.BootstrapConstants.BSM_DEFAULT_NAME;
import static rolevm.transform.BootstrapConstants.BSM_PROCEED_NAME;
import static rolevm.transform.BootstrapConstants.BSM_TYPE;

import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;

/**
 * Rewrites {@code invokevirtual} and {@code invokeinterface} call sites to
 * equivalent {@code invokedynamic} call sites. Call sites representing
 * {@code proceed} calls are assigned a special bootstrap method, and calls to
 * the {@link rolevm.api.DispatchContext#proceed()} marker method are replaced
 * by no-ops.
 * 
 * @author Martin Morgenstern
 */
public class IndyMethodAdapter extends MethodVisitor {
    private static final Handle DEFAULT_BSM = new Handle(H_INVOKESTATIC, BSM_CLASS, BSM_DEFAULT_NAME, BSM_TYPE, false);
    private static final Handle PROCEED_BSM = new Handle(H_INVOKESTATIC, BSM_CLASS, BSM_PROCEED_NAME, BSM_TYPE, false);
    private final MethodInfo info;

    public IndyMethodAdapter(MethodInfo info, MethodVisitor mv) {
        super(ASM5, mv);
        this.info = info;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (opcode == INVOKEVIRTUAL || opcode == INVOKEINTERFACE) {
            rewriteInvokeInstruction(owner, name, desc);
        } else {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }

    private void rewriteInvokeInstruction(String owner, String name, String descriptor) {
        // System.err.printf("rewriteInvoke(%s, %s, %s)%n", owner, name, descriptor);
        if ("java/lang/invoke/MethodHandle".equals(owner) && "invoke".equals(name)
                && descriptor.startsWith("(Lrolevm/api/DispatchContext;")) {
            visitInvokeDynamicInsn(info.name, adaptDescriptor(owner, descriptor), PROCEED_BSM);
        } else if ("rolevm/api/DispatchContext".equals(owner) && "proceed".equals(name)
                && "()Ljava/lang/invoke/MethodHandle;".equals(descriptor)) {
            visitInsn(POP);
            visitInsn(ACONST_NULL);
        } else {
            visitInvokeDynamicInsn(name, adaptDescriptor(owner, descriptor), DEFAULT_BSM);
        }
    }

    /**
     * Adapts the method descriptor string to include the receiver type as the first
     * argument.
     */
    static String adaptDescriptor(final String owner, final String desc) {
        final String ownerTypeDesc = owner.startsWith("[") ? owner : "L" + owner + ";";
        return "(" + ownerTypeDesc + desc.substring(1);
    }
}
