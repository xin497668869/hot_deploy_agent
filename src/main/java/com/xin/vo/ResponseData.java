package com.xin.vo;

import java.util.List;

/**
 * @author linxixin@cvte.com
 * @version 1.0
 * @description
 */
public class ResponseData {
    private DbDetailInfoVo      dbDetailInfoVo;
    private List<String>        sql;
    private StackTraceElement[] stackTraceElement;

    public DbDetailInfoVo getDbDetailInfoVo() {
        return dbDetailInfoVo;
    }

    public void setDbDetailInfoVo(DbDetailInfoVo dbDetailInfoVo) {
        this.dbDetailInfoVo = dbDetailInfoVo;
    }

    public List<String> getSql() {
        return sql;
    }

    public void setSql(List<String> sql) {
        this.sql = sql;
    }

    public StackTraceElement[] getStackTraceElement() {
        return stackTraceElement;
    }

    public void setStackTraceElement(StackTraceElement[] stackTraceElement) {
        this.stackTraceElement = stackTraceElement;
    }

    @Override
    public String toString() {
        return "ResponseData{" +
                "dbDetailInfoVo=" + dbDetailInfoVo +
                ", sql=" + sql +
                '}';
    }
}
