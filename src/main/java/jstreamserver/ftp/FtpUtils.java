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

import java.io.File;
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
    public static final String PARENT_PATH_REGEXP = "^(.*)(/[^/]+)$";
    public static final String FIRST_LEVEL_PATH_REGEXP = "^/([^/]+)$";

    public static String getNativePath(String path, String parentPath, Map<String, String> rootDirs) {
        if (path.startsWith("./")) {
            path = path.substring(2);
        }

        if (!path.startsWith("/")) {
            path = (parentPath + "/" + path).replaceAll("//", "/");
        }

        String rootDirLabel = null;
        String relativePath = null;

        Matcher matcher = Pattern.compile(PATH_REGEXP).matcher(path);
        if (matcher.find()) {
            rootDirLabel = matcher.group(1);
            relativePath = matcher.group(2);
        }

        if (relativePath.endsWith("/")) {
            relativePath = relativePath.substring(0, relativePath.length() - 1);
        }

        File file = new File(rootDirs.get(rootDirLabel) + relativePath);
        return file.getPath();
    }

    public static final String convertNativeToFtpPath(String absolutePath, Map<String, String> rootDirs) {
        for (Map.Entry<String, String> entry: rootDirs.entrySet()) {
            if (absolutePath.startsWith(entry.getValue())) {
                return "/" + entry.getKey() + "/" + absolutePath.substring(entry.getValue().length()).replaceAll("\\\\", "/");
            }
        }

        return null;
    }
    
    public static String getName(String path, String parentPath) {        
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        if (!path.startsWith("/")) {
            path = (parentPath + "/" + path).replaceAll("//", "/");
        }

        return path;
    }

    public static String convertToAbsolute(String path, String currentDir, Map<String, String> rootDirs) {
        if (path.startsWith("./")) {
            path = path.substring(2);
        }

        if (!path.startsWith("/")) {
            path = (currentDir + "/" + path).replaceAll("//", "/");
        }

        if (path.equals("/")) {
            return "/";
        }

        String nativePath = getNativePath(path, currentDir, rootDirs);
        return convertNativeToFtpPath(nativePath, rootDirs);
    }
}
