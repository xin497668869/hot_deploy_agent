package com.xin.vo;

/**
 * @author linxixin@cvte.com
 * @version 1.0
 * @description
 */

public class DbDetailInfoVo {

    private String  url;
    private String  host;
    private Integer port;
    private String  username;
    private String  password;
    private String  dbName;

    public DbDetailInfoVo() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    @Override
    public String toString() {
        return "DbDetailInfoVo{" +
                "url='" + url + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", passwordd='" + password + '\'' +
                ", dbName='" + dbName + '\'' +
                '}';
    }
}
