package jstreamserver.http;

import anhttpserver.HttpRequestContext;
import anhttpserver.SimpleHttpHandlerAdapter;
import jstreamserver.utils.Config;
import jstreamserver.utils.EncodingUtil;
import jstreamserver.utils.HtmlRenderer;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;

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

    private String[] getDirectoryContent(File dir) {
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !pathname.isHidden();
            }
        });

        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            names[i] = files[i].getName();
        }

        return names;
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
        String uriDecodedPath = URLDecoder.decode(httpRequestContext.getRequestURI().getPath(), EncodingUtil.UTF8_ENCODING);
        String path = EncodingUtil.encodeStringFromUTF8(uriDecodedPath, config.getCharset());

        if ("/".equals(path)) {
            return renderDirectory(path, config.getRootDirs().keySet().toArray(new String[0]));
        }

        File file = getFile(path);
        if (file.exists() && file.isDirectory()) {
            return renderDirectory(path, getDirectoryContent(file));
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
