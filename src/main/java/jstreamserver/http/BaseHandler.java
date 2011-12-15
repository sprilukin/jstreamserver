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
import anhttpserver.ResponseSizeNeedlessHandlerAdapter;
import jstreamserver.utils.Config;
import jstreamserver.utils.HttpUtils;
import jstreamserver.utils.RandomAccessFileInputStream;
import jstreamserver.utils.velocity.VelocityModel;
import jstreamserver.utils.velocity.VelocityRenderer;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.zip.GZIPOutputStream;

/**
 * Base handler which holds common methods for other handlers
 *
 * @author Sergey Prilukin
 */
public abstract class BaseHandler extends ResponseSizeNeedlessHandlerAdapter implements ConfigAwareHttpHandler {

    public static final String DIRECTORY_SEPARATOR = "/";
    public static final String ROOT_DIRECTORY = DIRECTORY_SEPARATOR;

    public static final String DEFAULT_MIME_PROPERTIES = "mime.properties";

    public static final ThreadLocal<DateFormat> HTTP_HEADER_DATE_FORMAT =
            new ThreadLocal<DateFormat> () {
                @Override
                protected synchronized DateFormat initialValue() {
                    DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
                    df.setTimeZone(TimeZone.getTimeZone("GMT"));
                    return df;
                }
            };

    private Config config;
    private static Properties mimeProperties = null;

    public BaseHandler() {
        //Should be synchronized.
        //In our case it is true since instances of BaseHandler are created sequentially by DispatcherHandler
        if (BaseHandler.mimeProperties == null) {
            loadMimeProperties();
        }
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public void loadMimeProperties() {
        try {
            InputStream is = null;

            File mimePropertiesFile = new File(DEFAULT_MIME_PROPERTIES);
            if (mimePropertiesFile.exists() && mimePropertiesFile.isFile()) {
                is = new FileInputStream(mimePropertiesFile);
            } else {
                is = Thread.currentThread().getContextClassLoader().getResourceAsStream(DEFAULT_MIME_PROPERTIES);
            }

            mimeProperties = new Properties();
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

    protected Properties getMimeProperties() {
        return mimeProperties;
    }

    protected void setContentType(String contentType, HttpRequestContext httpRequestContext) {
        setResponseHeader(HttpUtils.CONTENT_TYPE_HEADER, contentType, httpRequestContext);
    }

    protected void setCommonResourceHeaders(HttpRequestContext httpRequestContext, String mimeType) {
        //Set response headers
        setContentType(mimeType != null ? mimeType : "application/octet-stream", httpRequestContext);
        setResponseHeader("Expires", HTTP_HEADER_DATE_FORMAT.get().format(new Date(0)), httpRequestContext);
        setResponseHeader("Pragma", "no-cache", httpRequestContext);
        setResponseHeader("Cache-Control", "no-store,private,no-cache", httpRequestContext);
        setResponseHeader("Accept-Ranges", "bytes", httpRequestContext);
        setResponseHeader("Connection", "keep-alive", httpRequestContext);
    }

    protected InputStream rendeResourceNotFound(String path, HttpRequestContext httpRequestContext) throws IOException {
        InputStream result = VelocityRenderer.renderTemplate("templates/notfound.vm", new VelocityModel("path", path));
        setResponseCode(HttpURLConnection.HTTP_NOT_FOUND, httpRequestContext);
        return renderCompressedView(result, httpRequestContext);
    }

    protected InputStream renderCompressedView(InputStream view, HttpRequestContext httpRequestContext) throws IOException {
        setResponseHeader(HttpUtils.CONTENT_ENCODING_HEADER, HttpUtils.GZIP_ENCODING, httpRequestContext);
        setContentType(HttpUtils.DEFAULT_TEXT_CONTENT_TYPE, httpRequestContext);
        return compressInputStream(view);
    }

    protected boolean isAjaxRequest(HttpRequestContext httpRequestContext) throws IOException {
        List<String> ajaxHeader = httpRequestContext.getRequestHeaders().get("X-Requested-With");
        return ajaxHeader != null && ajaxHeader.size() > 0 && "XMLHttpRequest".equals(ajaxHeader.get(0));
    }

    protected InputStream compressInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        OutputStream os = new GZIPOutputStream(byteArrayOutputStream);
        IOUtils.copyLarge(inputStream, os);
        os.flush();
        os.close();

        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    protected InputStream getResource(File file, HttpRequestContext httpRequestContext) throws IOException {
        String extension = FilenameUtils.getExtension(file.getName());
        String mimeType = getMimeProperties().getProperty(extension.toLowerCase());

        setCommonResourceHeaders(httpRequestContext, mimeType);

        String range = "bytes=0-";
        if (httpRequestContext.getRequestHeaders().get("Range") != null) {
            range = httpRequestContext.getRequestHeaders().get("Range").get(0);
        }

        return getResourceRange(file, range, httpRequestContext);
    }

    protected File getFile(String path) {
        String rootDir = path.replaceFirst("\\/", "").replaceAll("\\/.*$", "");
        String fsPath = path.replaceFirst("\\/[^\\/]+", "");
        return new File(getConfig().getRootDirs().get(rootDir) + fsPath);
    }

    protected InputStream getResourceRange(File file, String range, HttpRequestContext httpRequestContext) throws IOException {
        long[] rangeArray = parseRange(range, file.length());

        String contentRange = String.format("bytes %s-%s/%s", rangeArray[0], rangeArray[1], file.length());
        setResponseHeader("Content-Range", contentRange, httpRequestContext);

        //Range should be an integer
        int rangeLength = (int)(rangeArray[1] - rangeArray[0] + 1);

        setResponseCode(HttpURLConnection.HTTP_PARTIAL, httpRequestContext);

        return new BufferedInputStream(new RandomAccessFileInputStream(file, rangeArray[0], rangeLength));
    }

    private long[] parseRange(String range, long fileLength) {
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
}
