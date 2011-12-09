/*
 * Copyright (c) 2011 Sergey Prilukin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package jstreamserver.http;

import anhttpserver.HttpRequestContext;
import anhttpserver.SimpleHttpHandlerAdapter;
import jstreamserver.utils.Config;
import jstreamserver.utils.HttpUtils;
import jstreamserver.utils.velocity.VelocityModel;
import jstreamserver.utils.velocity.VelocityRenderer;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Handler intended to dispatch requests to proper controllers
 *
 * @author Sergey Prilukin
 */
public final class DispatcherHandler extends SimpleHttpHandlerAdapter {
    public static final String MAPPING = "handler.properties";
    private Config config;
    private Properties mapping;
    private Map<String, ConfigAwareHttpHandler> handlers = new HashMap<String, ConfigAwareHttpHandler>();

    public void setMapping(Properties mapping) {
        this.mapping = mapping;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    @Override
    public InputStream getResponse(HttpRequestContext httpRequestContext) throws IOException {
        try {
            String path = httpRequestContext.getRequestURI().getPath();
            if (path.length() > 1) {
                path = path.substring(1).split("\\/")[0];
            }

            if (handlers.get(path) == null) {
                synchronized (this) {
                    if (handlers.get(path) == null) {
                        String handlerClass = mapping.getProperty(path);
                        if (handlerClass == null) {
                            return rendeResourceNotFound(httpRequestContext.getRequestURI().getPath(), httpRequestContext);
                        }

                        Class<ConfigAwareHttpHandler> clazz = (Class<ConfigAwareHttpHandler>)Class.forName(handlerClass);
                        ConfigAwareHttpHandler handler = clazz.newInstance();
                        handler.setConfig(config);
                        handlers.put(path, handler);
                    }
                }
            }

            return handlers.get(path).getResponse(httpRequestContext);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            e.printStackTrace();

            return renderException(sw.toString(), httpRequestContext);
        }
    }

    protected InputStream renderException(String exception, HttpRequestContext httpRequestContext) throws IOException {
        setResponseHeader(HttpUtils.CONTENT_TYPE_HEADER, HttpUtils.DEFAULT_TEXT_CONTENT_TYPE, httpRequestContext);
        InputStream result = VelocityRenderer.renderTemplate("jstreamserver/templates/exception.vm", new VelocityModel("exception", exception));
        setResponseSize(result.available(), httpRequestContext);
        setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR, httpRequestContext);
        return result;
    }

    protected InputStream rendeResourceNotFound(String path, HttpRequestContext httpRequestContext) throws IOException {
        setResponseHeader(HttpUtils.CONTENT_TYPE_HEADER, HttpUtils.DEFAULT_TEXT_CONTENT_TYPE, httpRequestContext);
        InputStream result = VelocityRenderer.renderTemplate("jstreamserver/templates/notmapped.vm", new VelocityModel("path", path));
        setResponseSize(result.available(), httpRequestContext);
        setResponseCode(HttpURLConnection.HTTP_NOT_FOUND, httpRequestContext);
        return result;
    }
}
