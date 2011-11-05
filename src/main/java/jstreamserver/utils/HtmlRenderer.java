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
