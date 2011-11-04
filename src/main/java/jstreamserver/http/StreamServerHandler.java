package jstreamserver.http;

import anhttpserver.HttpRequestContext;
import anhttpserver.SimpleHttpHandlerAdapter;
import jstreamserver.utils.Config;
import jstreamserver.utils.EncodingUtil;
import jstreamserver.utils.HtmlRenderer;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Main handler for the server.
 * All server logic put here.
 *
 * @author Sergey Prilukin
 */
public final class StreamServerHandler extends SimpleHttpHandlerAdapter {
    public static final String DEFAULT_MIME_PROPERTIES = "jstreamserver/http/mime.properties";

    private Config config;
    private Map<String, String> mimeProperties = new HashMap<String, String>();

    public StreamServerHandler() {
        config = new Config();
        loadMimeProperties();
    }

    public StreamServerHandler(Config config) {
        this.config = config;
        loadMimeProperties();
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

        if (file.exists() && file.isDirectory()) {
            return HttpURLConnection.HTTP_OK;
        } else if (file.exists() && file.isFile()) {
            List<String> range = httpRequestContext.getRequestHeaders().get("Range");
            return range != null ? HttpURLConnection.HTTP_PARTIAL : HttpURLConnection.HTTP_OK;
        } else {
            //Resource not found
            return HttpURLConnection.HTTP_NOT_FOUND;
        }
    }

    private void loadMimeProperties() {
        try {
            InputStream is = null;

            String mimePropsPath = config.getMimeProperties();
            if (mimePropsPath != null) {
                is = new FileInputStream(mimePropsPath);
            } else {
                is = Thread.currentThread().getContextClassLoader().getResourceAsStream(DEFAULT_MIME_PROPERTIES);
            }

            Properties properties = new Properties();
            properties.load(is);
            is.close();

            for (Map.Entry entry: properties.entrySet()) {
                String mimeType = (String)entry.getKey();
                String[] extensions = ((String)entry.getValue()).split("[\\s]+");
                for (String extension: extensions) {
                    mimeProperties.put(extension, mimeType);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    private void setContentType(String contentType) {
        setResponseHeader("Content-Type", contentType);
    }

    private byte[] getResource(File file, HttpRequestContext httpRequestContext) throws IOException {

        String extension = FilenameUtils.getExtension(file.getName());
        String mimeType = mimeProperties.get(extension);

        //Set response headers
        setContentType(mimeType != null ? mimeType : "application/octet-stream");
        setResponseHeader("Expires", htmlExpiresDateFormat().format(new Date(0)));
        setResponseHeader("Pragma", "no-cache");
        setResponseHeader("Cache-Control", "no-store,private,no-cache");
        setResponseHeader("Accept-Ranges", "bytes");

        long length = file.length();

        String range = null;
        if (httpRequestContext.getRequestHeaders().get("Range") != null) {
            range = httpRequestContext.getRequestHeaders().get("Range").get(0);
        }

        long[] rangeArray = parseRange(range, length);

        byte[] result = null;

        if (range == null) {
            //setResponseHeader("Content-Length", String.valueOf(length));
            result = IOUtils.toByteArray(new FileInputStream(file));
        } else {
            setResponseHeader("Content-Range", String.format("bytes %s-%s/%s", rangeArray[0], rangeArray[1], length));
            long rangeLength = rangeArray[1] - rangeArray[0] + 1;
            //setResponseHeader("Content-Length", String.valueOf(rangeLength));
            RandomAccessFile raf = new RandomAccessFile(file, "r");

            result = new byte[(int)rangeLength];

            raf.read(result, (int)rangeArray[0], (int)rangeLength);
        }

        return result;
    }

    public long[] parseRange(String range, long fileLength) {
        if (range == null) {
            return null;
        }

        String[] string = range.split("=")[1].split("-");
        long start = Math.min(Long.parseLong(string[0]), fileLength);
        long end = string.length == 1 ? fileLength - 1 : Math.min(Long.parseLong(string[1]), fileLength);

        long[] rangeArray = new long[2];
        rangeArray[0] = Math.min(start, end);
        rangeArray[1] = Math.max(start, end);

        return rangeArray;
    }

    private DateFormat htmlExpiresDateFormat() {
        DateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return httpDateFormat;
    }
}
