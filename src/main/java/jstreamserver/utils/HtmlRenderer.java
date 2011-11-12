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

package jstreamserver.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Helper class which encapsulates HTML rendering
 *
 * @author Sergey Prilukin
 */
public final class HtmlRenderer {

    public static void renderHeader(StringBuilder sb) {
        sb.append("<!DOCTYPE html><html><head>");
        sb.append(renderMetaContentType()).append("\r\n");
        sb.append(renderCSS()).append("</head><body>\r\n");
    }

    public static void renderFooter(StringBuilder sb) {
        sb.append("</body></html>\r\n");
    }

    public static String renderMetaContentType() {
        return (new StringBuilder("<meta http-equiv=\"Content-Type\" content=\""))
                .append("text/html; charset=").append(EncodingUtil.UTF8_ENCODING).append("\">").toString();
    }

    public static String renderCSS() {
        return "<style>ul, p {font-size: 32px;}</style>";
    }

    public static String renderDirView(String[] files, String parentPath) {
        try {
            StringBuilder sb = new StringBuilder();
            renderHeader(sb);
            sb.append("<ul>\r\n");

            if (!"/".equals(parentPath)) {
                String parentDir = parentPath.replaceAll("\\/$", "").replaceAll("\\/[^\\/]+$", "");
                sb.append("<li><a href=\"")
                        .append(parentDir.isEmpty() ? "/" : parentDir)
                        .append("\">").append("..").append("</a>").append("</li>\r\n");
            }

            for (String file: files) {
                sb.append("<li><a href=\"");
                if (!"/".equals(parentPath)) {
                    sb.append(parentPath);
                }
                sb.append("/").append(URLEncoder.encode(file, EncodingUtil.UTF8_ENCODING)).append("\">")
                        .append(file).append("</a>").append("</li>\r\n");
            }
            sb.append("</ul>");
            renderFooter(sb);

            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String renderResourceNotFound(String path) {
        StringBuilder sb = new StringBuilder();
        renderHeader(sb);
        sb.append("<h1>Resource not found</h1>\r\n");
        sb.append("<p>").append(path).append("</p>\r\n");
        renderFooter(sb);

        return sb.toString();
    }
}
