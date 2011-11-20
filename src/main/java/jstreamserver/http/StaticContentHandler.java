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
import jstreamserver.utils.Config;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * This handler serves static content like CSS, JavaScript and images
 *
 * @author Sergey Prilukin
 */
public final class StaticContentHandler extends BaseHandler {
    public static final String HANDLE_PATH = "/static";
    public static String RESOURCES_PATH_PREFIX = "jstreamserver/staticcontent";

    public static final List<String> mimeTypesToCompress = new ArrayList<String>();
    static {
        mimeTypesToCompress.add("text/css");
        mimeTypesToCompress.add("application/x-javascript");
    }

    public StaticContentHandler() {
        super();
    }

    public StaticContentHandler(Config config) {
        super(config);
    }

    @Override
    public InputStream getResponseAsStream(HttpRequestContext httpRequestContext) throws IOException {
        String path = RESOURCES_PATH_PREFIX + httpRequestContext.getRequestURI().getPath().substring(HANDLE_PATH.length());

        String extension = FilenameUtils.getExtension(path);
        String type = getMimeProperties().getProperty(extension);

        setContentType(type, httpRequestContext);
        InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (mimeTypesToCompress.contains(type)) {
            setResponseHeader("Content-Encoding", "gzip", httpRequestContext);
            resourceAsStream = compressInputStream(resourceAsStream);
        }

        setResponseSize(resourceAsStream.available(), httpRequestContext);
        return resourceAsStream;
    }

    private InputStream compressInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        OutputStream os = new GZIPOutputStream(byteArrayOutputStream);
        IOUtils.copyLarge(inputStream, os);
        os.flush();
        os.close();

        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }
}
