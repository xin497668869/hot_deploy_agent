package com.xin.change;

import com.xin.GeneralClassAdapter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import java.io.IOException;

import static com.xin.Tools.getNewClassBytes;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASM6;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

/**
 * 修改mysql代码, 用于显示具体sql
 */
public class MySqlConfigChangeClass {


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
            if (name.equals("getInstance") && desc.equals("(Ljava/lang/String;ILjava/util/Properties;Ljava/lang/String;Ljava/lang/String;)Lcom/mysql/jdbc/Connection;")) {
                MethodVisitor newMv = new ChangeMethodAdapter(mv);
                return newMv;
            } else {
                return mv;
            }
        }

        // 定义一个自己的方法访问类
        class ChangeMethodAdapter extends MethodVisitor {

            public ChangeMethodAdapter(MethodVisitor mv) {
                super(ASM6, mv);
            }

            @Override
            public void visitCode() {
                super.visitCode();
                mv.visitVarInsn(ALOAD, 2);
                mv.visitVarInsn(ALOAD, 4);
                mv.visitMethodInsn(INVOKESTATIC, "com/xin/BootClass", "handleMySqlConfigChangeClass", "(Ljava/util/Properties;Ljava/lang/String;)V", false);
            }
        }
    }
}
