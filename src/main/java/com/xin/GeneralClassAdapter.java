package com.xin;

import org.objectweb.asm.ClassVisitor;

import static org.objectweb.asm.Opcodes.ASM6;

/**
 * @author linxixin@cvte.com
 * @version 1.0
 * @description
 */
public class GeneralClassAdapter extends ClassVisitor {

    public GeneralClassAdapter() {
        super(ASM6);
    }

    public void setCp(ClassVisitor cv) {
        this.cv = cv;
    }
}
