package jstreamserver.http;

import anhttpserver.HttpRequestContext;
import anhttpserver.SimpleHttpHandlerAdapter;
import jstreamserver.utils.Config;
import jstreamserver.utils.HtmlRenderer;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

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

    private File getFile(String path) {
        String rootDir = path.replaceFirst("\\/", "").replaceAll("\\/.*$", "");
        String fsPath = path.replaceFirst("\\/[^\\/]+", "");
        return new File(config.getRootDirs().get(rootDir) + fsPath);
    }

    private byte[] renderDirectory(String path, String[] children) {
        String contentType = "text/html; charset=UTF-8";
        setContentType(contentType);
        return HtmlRenderer.renderDirView(children, path, config.getCharset()).getBytes();
    }

    private byte[] rendeResourceNotFound(String path) {
        String contentType = "text/html; charset=UTF-8";
        setContentType(contentType);
        return HtmlRenderer.renderResourceNotFound(path, config.getCharset()).getBytes();
    }

    private byte[] getResource(File file, HttpRequestContext httpRequestContext) {
        return new byte[0];
    }

    private void setContentType(String contentType) {
        setResponseHeader("Content-Type", contentType);
    }

    @Override
    public byte[] getResponse(HttpRequestContext httpRequestContext) throws IOException {
        String path = httpRequestContext.getRequestURI().getPath();
        if ("/".equals(path)) {
            return renderDirectory(path, config.getRootDirs().keySet().toArray(new String[0]));
        }

        File file = getFile(path);
        if (file.exists() && file.isDirectory()) {
            return renderDirectory(path, file.list());
        } else if (file.exists() && file.isFile()) {
            return getResource(file, httpRequestContext);
        } else {
            return rendeResourceNotFound(path);
        }
    }

    @Override
    public int getResponseCode(HttpRequestContext httpRequestContext) {
        String path = httpRequestContext.getRequestURI().getPath();
        if ("/".equals(path)) {
            return HttpURLConnection.HTTP_OK;
        }

        File file = getFile(path);

        if (file.exists()) {
            return file.isDirectory() ? HttpURLConnection.HTTP_OK : HttpURLConnection.HTTP_PARTIAL;
        } else {
            //Resource not found
            return HttpURLConnection.HTTP_NOT_FOUND;
        }
    }
}
