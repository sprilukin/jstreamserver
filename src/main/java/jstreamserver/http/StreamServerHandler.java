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
import jstreamserver.utils.EncodingUtil;
import jstreamserver.utils.HtmlRenderer;
import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Main handler for the server.
 * All server logic put here.
 *
 * @author Sergey Prilukin
 */
public final class StreamServerHandler extends BaseHandler {

    public static final String HANDLE_PATH = "/";

    public StreamServerHandler() {
        super();
    }

    public StreamServerHandler(Config config) {
        super(config);
    }

    public InputStream getResponseAsStream(HttpRequestContext httpRequestContext) throws IOException {
        String path = URLDecoder.decode(httpRequestContext.getRequestURI().getPath(), EncodingUtil.UTF8_ENCODING);

        if ("/".equals(path)) {
            return renderDirectory(path, getConfig().getRootDirs().keySet().toArray(new String[0]), httpRequestContext);
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

    private Map<String, String> getHrefs(String[] fileNames, String parentPath) {
        Map<String, String> hrefs = new TreeMap<String, String>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return "/".equals(o1) ? -1 : ("/".equals(o2) ? 1 : o1.compareTo(o2));
            }
        });

        if (!"/".equals(parentPath)) {
            String parentDir = parentPath.replaceAll("\\/$", "").replaceAll("\\/[^\\/]+$", "");
            hrefs.put(parentDir.isEmpty() ? "/" : parentDir, "..");
        }

        for (String name : fileNames) {
            String parentDir = "/".equals(parentPath) ? "" : parentPath + "/";
            File file = getFile(parentDir + name);

            String href = null;
            try {
                String encodedName = URLEncoder.encode(name, EncodingUtil.UTF8_ENCODING);

                if (file.isFile()) {
                    String extension = FilenameUtils.getExtension(file.getName());
                    String mimeType = getMimeProperties().get(extension.toLowerCase());
                    if (getConfig().httpLiveStreamingSupported(extension, mimeType)) {
                        href = LiveStreamHandler.HANDLE_PATH + "?file=" + parentDir + encodedName;
                    } else {
                        href = parentDir + encodedName;
                    }
                } else {
                    href = parentDir + encodedName;
                }

                hrefs.put(href, name);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        return hrefs;
    }

    private InputStream renderDirectory(String path, String[] children, HttpRequestContext httpRequestContext) {
        setContentType(DEFAULT_HTML_CONTENT_TYPE, httpRequestContext);
        byte[] response = HtmlRenderer.renderDirView(getHrefs(children, path)).getBytes();
        setResponseSize(response.length, httpRequestContext);
        setResponseCode(HttpURLConnection.HTTP_OK, httpRequestContext);
        return new ByteArrayInputStream(response);
    }
}
