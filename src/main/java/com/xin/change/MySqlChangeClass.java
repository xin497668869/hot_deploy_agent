package com.xin.change;

import com.xin.GeneralClassAdapter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import java.io.IOException;

import static com.xin.Tools.getNewClassBytes;
import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARRAYLENGTH;
import static org.objectweb.asm.Opcodes.ASM6;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

/**
 * 修改mysql代码, 用于显示具体sql
 */
public class MySqlChangeClass {


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
            if (name.equals("fillSendPacket") && desc.equals("([[B[Ljava/io/InputStream;[Z[I)Lcom/mysql/jdbc/Buffer;")) {
                MethodVisitor newMv = new ChangeMethodAdapter(mv);
                return newMv;
            } else {
                return mv;
            }
        }

        // 定义一个自己的方法访问类
        class ChangeMethodAdapter extends MethodVisitor {
            public boolean arraylength = false;
            public boolean aaload      = false;

            public ChangeMethodAdapter(MethodVisitor mv) {
                super(ASM6, mv);
            }

            /*
            Lorg/slf4j/Logger; LOGGER
             */
            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                if (name.contains("writeBytesNoNull") && aaload && arraylength) {
                    mv.visitVarInsn(ALOAD, 6);
                    mv.visitVarInsn(ALOAD, 0);
//                    executeInternal(ILcom/mysql/jdbc/Buffer;ZZ[Lcom/mysql/jdbc/Field;Z)Lcom/mysql/jdbc/ResultSetInternalMethods;
                    mv.visitMethodInsn(INVOKESTATIC, "com/xin/BootClass", "logSql", "(Ljava/lang/Object;Ljava/lang/Object;)V", false);
                }

            }

            @Override
            public void visitInsn(int opcode) {

                if (opcode == AALOAD) {
                    aaload = true;
                } else if (aaload && opcode == ARRAYLENGTH) {
                    arraylength = true;
                } else {
                    aaload = arraylength = false;
                }
                super.visitInsn(opcode);

            }
        }
    }
}
