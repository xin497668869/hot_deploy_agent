package com.xin.monitor;

import com.xin.vo.TreeVo;

import java.util.Stack;

/**
 * @author wanggaoxiang@cvte.com
 * @version 1.0
 * @description
 */
public class Monitor {

    static ThreadLocal<Stack<String>> stack = new ThreadLocal<>();

    public static void start() {
        if (stack.get() == null) {
            stack.set(new Stack<>());
        }
        stack.get().push(System.currentTimeMillis() + "###" + Thread.currentThread().getStackTrace().length);
    }

    public static void end(String paramClassNames) {
        String pop = stack.get().pop();
        System.out.println("pop" + pop);
        String[] popSplit = pop.split("###");
        while (Integer.valueOf(popSplit[1]) > Thread.currentThread().getStackTrace().length) {
            pop = stack.get().pop();
            popSplit = pop.split("###");
        }
        long runTime = System.currentTimeMillis() - Long.valueOf(popSplit[0]);
        System.out.println(Thread.currentThread().getStackTrace()[2].toString() + "花费的时间:" + runTime);
        TreeVo vo = new TreeVo();
        vo.setClassName(Thread.currentThread().getStackTrace()[2].getClassName());
        vo.setMethodName(Thread.currentThread().getStackTrace()[2].getMethodName());
        vo.setParamClassNames(paramClassNames);
        vo.setTimeConsuming(runTime);
        System.out.println(vo.toString());


        //todo 将数据放入一个树形对象中
    }
}
