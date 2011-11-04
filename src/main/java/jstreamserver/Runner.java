package jstreamserver;


import anhttpserver.DefaultSimpleHttpServer;
import anhttpserver.SimpleHttpServer;
import jstreamserver.http.StreamServerHandler;
import jstreamserver.utils.Config;

/**
 * entry point for <b>jstreamserver</b>
 *
 * @author Sergey Prilukin
 */
public final class Runner {

    private static final Object monitor = new Object();

    private void start(Config config) {
        SimpleHttpServer server = new DefaultSimpleHttpServer();
        server.setMaxThreads(config.getMaxThreads());
        server.setHost(config.getHost());
        server.setPort(config.getPort());

        server.addHandler("/", new StreamServerHandler(config));
        server.start();
    }

    /**
     * Create {@link Config} instance from passed params
     *
     * @param args command-line parameters
     * @return instance of {@link Config}
     */
    private static Config getConfig(String[] args) {
        return new Config();
    }

    public static void main(String[] args) throws Exception {
        Runner runner = new Runner();
        runner.start(getConfig(args));

        synchronized (monitor) {
            monitor.wait();
        }
    }
}
