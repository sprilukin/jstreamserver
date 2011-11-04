package jstreamserver.utils;

/**
 * POJO which holds config settings for the server.
 *
 * @author Sergey Prilukin
 */
public final class Config {
    private int port = 8888;
    private String host = "0.0.0.0";

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
}
