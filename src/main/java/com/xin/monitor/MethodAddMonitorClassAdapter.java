package com.xin.monitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;

import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.RETURN;

/**
 * @author wanggaoxiang@cvte.com
 * @version 1.0
 * @description
 */
public class MethodAddMonitorClassAdapter {


    public static class AddMonitorClassAdapter extends ClassVisitor {

        public AddMonitorClassAdapter(final ClassVisitor cv) {
            super(Opcodes.ASM6, cv);
        }

        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (!Arrays.asList("<init>", "<clinit>").contains(name)) {
                MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
                return new AsmMethodVisit(mv, desc);
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        static class AsmMethodVisit extends MethodVisitor {
            private String desc;

            private AsmMethodVisit(MethodVisitor mv, String desc) {
                super(Opcodes.ASM6, mv);
                this.desc = desc;
            }

            @Override
            public void visitCode() {
                super.visitCode();
                visitMethodInsn(Opcodes.INVOKESTATIC, Monitor.class.getName().replace(".", "/"), "start", "()V", false);
            }

            @Override
            public void visitInsn(int opcode) {
                if (opcode <= RETURN && opcode >= IRETURN) {
                    visitLdcInsn(desc);
                    visitMethodInsn(Opcodes.INVOKESTATIC, Monitor.class.getName().replace(".", "/"), "end", "(Ljava/lang/String;)V", false);
                }
                super.visitInsn(opcode);
            }

        }
    }
}
