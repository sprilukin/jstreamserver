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

package jstreamserver.ftp;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utils to convert path from native to visible etc.
 *
 * @author Sergey Prilukin
 */
public class FtpUtils {
    public static final String PATH_REGEXP = "^/([^/]+)(.*)$";

    public static String normalizePath(String path, String parentPath) throws Exception {

        if (parentPath.charAt(parentPath.length() - 1) == '/') {
            parentPath = parentPath.substring(0, parentPath.length() - 1);
        }

        if (path.charAt(0) != '/') {
            path = parentPath + "/" + path;
        }


        String normalizedPath = new URI(encodePath(path)).normalize().toString();
        if (normalizedPath.length() > 1 && normalizedPath.endsWith("/")) {
            normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 1);
        }

        return URLDecoder.decode(normalizedPath, "UTF-8");
    }

    private static String encodePath(String path) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '/') {
                sb.append(path.charAt(i));
            } else {
                char[] chars = new char[path.length() - i];
                int offset = i;

                while (i < path.length() && path.charAt(i) != '/') {
                    chars[i - offset] = path.charAt(i);
                    i++;
                }

                sb.append(URLEncoder.encode(new String(chars, 0, i - offset), "UTF-8"));
                i--;
            }
        }

        return sb.toString();
    }

    public static String getNativePath(String path, Map<String, String> rootDirs) {
        String rootDirLabel = null;
        String relativePath = null;

        Matcher matcher = Pattern.compile(PATH_REGEXP).matcher(path);
        if (matcher.find()) {
            rootDirLabel = matcher.group(1);
            relativePath = matcher.group(2);
        }

        if (relativePath.length() > 0 && relativePath.charAt(relativePath.length() - 1) == '/') {
            relativePath = relativePath.substring(0, relativePath.length() - 1);
        }

        relativePath = relativePath.replaceAll("\\/", "\\" + System.getProperty("file.separator"));

        return rootDirs.get(rootDirLabel) + relativePath;
    }
}
