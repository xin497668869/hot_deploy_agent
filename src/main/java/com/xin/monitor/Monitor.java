package com.xin.monitor;

import com.xin.monitor.tree.TreeNode;
import com.xin.vo.TreeVo;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author wanggaoxiang@cvte.com
 * @version 1.0
 * @description
 */
public class Monitor {

    static ThreadLocal<Stack<String>> stack = new ThreadLocal<>();
    static List<TreeNode>             tree  = new ArrayList<>();


    public static void start() {
        if (stack.get() == null) {
            stack.set(new Stack<>());
        }

        stack.get().push(System.currentTimeMillis() + "&$##$&" + Thread.currentThread().getStackTrace().length);
        //每次进入方法就将构造树节点并放到list中
        tree.add(new TreeNode(Thread.currentThread().getStackTrace().length, Thread.currentThread().getStackTrace().length - 1));
    }

    public static void end(String paramClassNames) {
        String pop = stack.get().pop();
        while (Integer.valueOf(pop.split("&$##$&")[1]) > Thread.currentThread().getStackTrace().length) {
            pop = stack.get().pop();
        }
        long runTime = System.currentTimeMillis() - Long.valueOf(pop.split("&$##$&")[0]);
        System.out.println(Thread.currentThread().getStackTrace()[2].toString() + "花费的时间:" + runTime);
        TreeVo vo = new TreeVo();
        vo.setClassName(Thread.currentThread().getStackTrace()[2].getClassName());
        vo.setMethodName(Thread.currentThread().getStackTrace()[2].getMethodName());
        vo.setParamClassNames(paramClassNames);
        vo.setTimeConsuming(runTime);
        System.out.println(vo.toString());
        //跳出节点之前先查看list中看是对应哪个方法,将耗时TreeVo更新到list中
        for (int i = 0; i < tree.size(); i++) {
            if (tree.get(i).getNodeId() == Thread.currentThread().getStackTrace().length) {
                tree.get(i).setVo(vo);
            }
        }

    }
}
