package com.xin.change;

import com.xin.GeneralClassAdapter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import java.io.IOException;

import static com.xin.Tools.getNewClassBytes;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASM6;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

/**
 * 修改mysql代码, 用于显示具体sql
 */
public class WebappLoaderChangeClass {

    public byte[] writeFile(ClassLoader classLoader, String className) throws IOException, ClassNotFoundException {
        return getNewClassBytes(className, classLoader, new MyGeneralClassAdapter());

    }

    static class MyGeneralClassAdapter extends GeneralClassAdapter {

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            return super.visitField(access, name, desc, signature, value);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);

            // 当是sayName方法是做对应的修改
            if (name.equals("startInternal") && desc.equals("()V")) {
                MethodVisitor newMv = new ChangeMethodAdapter(mv);
                return newMv;
            }
            return mv;
        }
    }

    // 定义一个自己的方法访问类
    static class ChangeMethodAdapter extends MethodVisitor {

        public ChangeMethodAdapter(MethodVisitor mv) {
            super(ASM6, mv);
        }


        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            if (opcode == INVOKEINTERFACE && name.equals("start")) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, "org/apache/catalina/loader/WebappLoader", "classLoader", "Lorg/apache/catalina/loader/WebappClassLoaderBase;");
                mv.visitMethodInsn(INVOKESTATIC, "com/xin/BootClass", "handleClassLoader", "(Ljava/net/URLClassLoader;)V", false);
            }
        }


    }
}
