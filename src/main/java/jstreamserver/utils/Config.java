package jstreamserver.utils;

import java.util.Map;
import java.util.TreeMap;

/**
 * POJO which holds config settings for the server.
 *
 * @author Sergey Prilukin
 */
public final class Config {
    private int port = 8888;
    private String host = "0.0.0.0";
    private int maxThreads = 10;
    private String charset = "windows-1251";
    private Map<String, String> rootDirs = new TreeMap<String, String>();

    public Config() {
        rootDirs.put("C", "c:\\");
        rootDirs.put("D", "d:\\");
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public Map<String, String> getRootDirs() {
        return rootDirs;
    }

    public void setRootDirs(Map<String, String> rootDirs) {
        this.rootDirs = rootDirs;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }
}
