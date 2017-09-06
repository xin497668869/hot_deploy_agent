package com.xin.vo;

/**
 * @author wanggaoxiang@cvte.com
 * @version 1.0
 * @description
 */
public class TreeVo {

    private String className;
    private String methodName;
    private String paramClassNames;
    private Long   timeConsuming;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getParamClassNames() {
        return paramClassNames;
    }

    public void setParamClassNames(String paramClassName) {
        this.paramClassNames = paramClassName;
    }

    public Long getTimeConsuming() {
        return timeConsuming;
    }

    public void setTimeConsuming(Long timeConsuming) {
        this.timeConsuming = timeConsuming;
    }

    @Override
    public String toString() {
        return "TreeVo{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", paramClassNames=" + paramClassNames +
                ", timeConsuming=" + timeConsuming +
                '}';
    }
}
