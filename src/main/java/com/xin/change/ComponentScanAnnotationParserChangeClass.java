package com.xin.change;

import com.xin.GeneralClassAdapter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import java.io.IOException;

import static com.xin.Tools.getNewClassBytes;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASM6;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.POP;

/**
 * @author linxixin@cvte.com
 * @version 1.0
 * @description
 */
public class ComponentScanAnnotationParserChangeClass {

    public byte[] writeFile(ClassLoader classLoader, String className) throws IOException, ClassNotFoundException {
        return getNewClassBytes(className, classLoader, new ComponentScanAnnotationParserChangeClass.Adapter());
    }


    static class Adapter extends GeneralClassAdapter {

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            return super.visitField(access, name, desc, signature, value);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);

            // 当是sayName方法是做对应的修改
            if (name.equals("parse") && desc.equals("(Lorg/springframework/core/annotation/AnnotationAttributes;Ljava/lang/String;)Ljava/util/Set;")) {
                MethodVisitor newMv = new ComponentScanAnnotationParserChangeClass.ChangeMethodAdapter(mv);
                return newMv;
            } else {
                return mv;
            }
        }
    }

    // 定义一个自己的方法访问类
    public static class ChangeMethodAdapter extends MethodVisitor {


        public ChangeMethodAdapter(MethodVisitor mv) {
            super(ASM6, mv);
        }

        public int time = 0;

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
//            System.out.println("  "+owner+"  "+name+"  "+desc+"  "+itf);
            if (owner.equals("org/springframework/context/annotation/ClassPathBeanDefinitionScanner") && name.equals("addExcludeFilter")) {
                time++;
            }
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            super.visitVarInsn(opcode, var);
//            System.out.println(opcode+"  "+var);
            if (time > 1 && opcode == ALOAD && var == 3) {
//                System.out.println("准备替换 scan NEWWW");
                super.visitVarInsn(ALOAD, 8);
                super.visitLdcInsn("com.xin");
                super.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "add", "(Ljava/lang/Object;)Z", true);
                super.visitInsn(POP);
            }
        }
    }

}
