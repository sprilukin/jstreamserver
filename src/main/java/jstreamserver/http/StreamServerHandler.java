package jstreamserver.http;

import anhttpserver.HttpRequestContext;
import anhttpserver.SimpleHttpHandlerAdapter;
import jstreamserver.utils.Config;
import jstreamserver.utils.EncodingUtil;
import jstreamserver.utils.HtmlRenderer;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
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

    public InputStream getResponseAsStream(HttpRequestContext httpRequestContext) throws IOException {
        String path = URLDecoder.decode(httpRequestContext.getRequestURI().getPath(), EncodingUtil.UTF8_ENCODING);

        if ("/".equals(path)) {
            return renderDirectory(path, config.getRootDirs().keySet().toArray(new String[0]), httpRequestContext);
        }

        File file = getFile(path);
        if (file.exists() && file.isDirectory()) {
            return renderDirectory(path, getDirectoryContent(file), httpRequestContext);
        } else if (file.exists() && file.isFile()) {
            return getResource(file, httpRequestContext);
        } else {
            return rendeResourceNotFound(path, httpRequestContext);
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

    private InputStream renderDirectory(String path, String[] children, HttpRequestContext httpRequestContext) {
        String contentType = "text/html; charset=" + EncodingUtil.UTF8_ENCODING;
        setContentType(contentType, httpRequestContext);
        byte[] bytes = HtmlRenderer.renderDirView(children, path).getBytes();
        setResponseSize(bytes.length, httpRequestContext);
        setResponseCode(HttpURLConnection.HTTP_OK, httpRequestContext);
        return new ByteArrayInputStream(bytes);
    }

    private InputStream rendeResourceNotFound(String path, HttpRequestContext httpRequestContext) {
        String contentType = "text/html; charset=" + EncodingUtil.UTF8_ENCODING;
        setContentType(contentType, httpRequestContext);
        byte[] bytes = HtmlRenderer.renderResourceNotFound(path).getBytes();
        setResponseSize(bytes.length, httpRequestContext);
        setResponseCode(HttpURLConnection.HTTP_NOT_FOUND, httpRequestContext);
        return new ByteArrayInputStream(bytes);
    }

    private void setContentType(String contentType, HttpRequestContext httpRequestContext) {
        setResponseHeader("Content-Type", contentType, httpRequestContext);
    }

    private InputStream getResource(File file, HttpRequestContext httpRequestContext) throws IOException {

        String extension = FilenameUtils.getExtension(file.getName());
        String mimeType = mimeProperties.get(extension.toLowerCase());

        //Set response headers
        setContentType(mimeType != null ? mimeType : "application/octet-stream", httpRequestContext);
        setResponseHeader("Expires", htmlExpiresDateFormat().format(new Date(0)), httpRequestContext);
        setResponseHeader("Pragma", "no-cache", httpRequestContext);
        setResponseHeader("Cache-Control", "no-store,private,no-cache", httpRequestContext);
        setResponseHeader("Accept-Ranges", "bytes", httpRequestContext);
        setResponseHeader("Connection", "close", httpRequestContext);

        long length = file.length();

        String range = null;
        if (httpRequestContext.getRequestHeaders().get("Range") != null) {
            range = httpRequestContext.getRequestHeaders().get("Range").get(0);
        }

        long[] rangeArray = parseRange(range, length);

        InputStream result = null;

        if (range == null) {
            setResponseSize((int) length, httpRequestContext);
            setResponseCode(HttpURLConnection.HTTP_OK, httpRequestContext);
            String contentDisposition = String.format("attachment; filename=\"%s\"", EncodingUtil.encodeStringFromUTF8(file.getName(), "ISO-8859-1"));
            setResponseHeader("Content-Disposition", contentDisposition, httpRequestContext);
            result = new BufferedInputStream(new FileInputStream(file));
        } else {
            String contentRange = String.format("bytes %s-%s/%s", rangeArray[0], rangeArray[1], length);
            setResponseHeader("Content-Range", contentRange, httpRequestContext);
            long rangeLength = rangeArray[1] - rangeArray[0] + 1;
            setResponseSize((int)rangeLength, httpRequestContext);
            setResponseCode(HttpURLConnection.HTTP_PARTIAL, httpRequestContext);

            final RandomAccessFile raf = new RandomAccessFile(file, "r");
            result = new BufferedInputStream(new RandomAccessFileInputStream(raf, rangeArray[0], (int)rangeLength), config.getBufferSize());
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

    /**
     * Helper class which wraps {@link RandomAccessFile} into {@link InputStream}
     */
    class RandomAccessFileInputStream extends InputStream {
        private RandomAccessFile raf;
        private int maxBytesToRead;
        private int currentPos = 0;

        RandomAccessFileInputStream(RandomAccessFile raf) {
            this.raf = raf;
            this.maxBytesToRead = Integer.MAX_VALUE;
        }

        RandomAccessFileInputStream(RandomAccessFile raf, long startPos, int maxBytesToRead) {
            this.raf = raf;
            this.maxBytesToRead = maxBytesToRead;
            try {
                if (startPos > 0) {
                    raf.seek(startPos);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public int read() throws IOException {
            if (maxBytesToRead - currentPos > 0) {
                currentPos++;
                return raf.read();
            } else {
                return -1;
            }
        }

        @Override
        public int read(byte[] b) throws IOException {
            return this.read(b, 0, b.length);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int maxBytesLeftToRead = maxBytesToRead - currentPos;
            if (maxBytesLeftToRead > 0) {
                int readLength = Math.min(maxBytesLeftToRead, len);
                currentPos += readLength;
                return raf.read(b, off, readLength);
            } else {
                return -1;
            }
        }

        @Override
        public void close() throws IOException {
            super.close();
            raf.close();
        }
    }
}
