package jstreamserver.http;

import anhttpserver.HttpRequestContext;
import anhttpserver.SimpleHttpHandlerAdapter;
import jstreamserver.utils.Config;

import java.io.IOException;

/**
 * Main handler for the server.
 * All server logic put here.
 *
 * @author Sergey Prilukin
 */
public final class StreamServerHandler extends SimpleHttpHandlerAdapter {

    private Config config;

    public StreamServerHandler() {
        config = new Config();
    }

    public StreamServerHandler(Config config) {
        this.config = config;
    }

    @Override
    public byte[] getResponse(HttpRequestContext httpRequestContext) throws IOException {
        return new byte[0];
    }

    @Override
    public int getResponseCode(HttpRequestContext httpRequestContext) {
        return super.getResponseCode(httpRequestContext);
    }
}
