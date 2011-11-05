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
    private String mimeProperties = null;
    private int bufferSize = 1024 * 1024; //1MB

    public Config() {
        rootDirs.put("C", "c:\\");
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

    public String getMimeProperties() {
        return mimeProperties;
    }

    public void setMimeProperties(String mimeProperties) {
        this.mimeProperties = mimeProperties;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Jstreamserver config:\r\n");
        sb.append("listening on: ").append(host).append(":").append(port).append("\r\n");
        sb.append("max threads count: ").append(maxThreads).append("\r\n");
        sb.append("response buffer size: ").append(bufferSize).append("\r\n");
        if (mimeProperties != null) {
            sb.append("mimeProperties: ").append(mimeProperties).append("\r\n");
        }

        sb.append("rootDirs:\r\n");
        for (Map.Entry<String, String> entry: rootDirs.entrySet()) {
            sb.append("\"").append(entry.getValue()).append("\"").append(" with label: ").append(entry.getKey()).append("\r\n");
        }

        return sb.toString();
    }
}
