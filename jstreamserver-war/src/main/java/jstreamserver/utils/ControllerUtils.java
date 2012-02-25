/*
 * Copyright (c) 2012 by Sergey Prilukin
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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;

/**
 * Utility metods for controllers
 *
 * @author Sergey Prilukin
 */
@Component
public class ControllerUtils {

    @Autowired
    private MimeProperties mimeProperties;

    @Autowired
    private ConfigReader configReader;

    public InputStream getFileAsStream(File file, String range, HttpServletResponse response) throws IOException {
        String extension = FilenameUtils.getExtension(file.getName());
        String mimeType = mimeProperties.getProperty(extension.toLowerCase());

        setCommonResourceHeaders(mimeType, response);
        response.setHeader("Accept-Ranges", "bytes");

        if (range != null) {
            long[] rangeArray = parseRange(range, file.length());
            String contentRange = String.format(HttpUtils.CONTENT_RANGE_FORMAT,
                    rangeArray[0], rangeArray[1], file.length());

            response.setHeader(HttpUtils.CONTENT_RANGE_HEADER, contentRange);

            //Range should be an integer
            int rangeLength = (int)(rangeArray[1] - rangeArray[0] + 1);

            response.setStatus(HttpURLConnection.HTTP_PARTIAL);

            return new BufferedInputStream(new RandomAccessFileInputStream(file, rangeArray[0], rangeLength));
        } else {
            //We want to open resource, not to download it
            //so do not set content disposition header
            //response.setHeader(
            //        HttpUtils.CONTENT_DISPOSITION_HEADER,
            //        String.format(HttpUtils.CONTENT_DISPOSITION_FORMAT, file.getName()));

            return new BufferedInputStream(new FileInputStream(file));
        }
    }

    public void setCommonResourceHeaders(String mimeType, HttpServletResponse response) {

        //Set response headers
        String contentType = mimeType != null ? mimeType : "application/octet-stream";
        if (contentType.startsWith("text")) {
            contentType = contentType + "; charset=" + configReader.getDefaultTextCharset();
        }

        response.setHeader(HttpUtils.CONTENT_TYPE_HEADER, contentType);
        response.setDateHeader("Expires", 0);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-store,private,no-cache");
        response.setHeader("Connection", "keep-alive");
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

    public File getFile(String path) {
        if (path == null || path.isEmpty() || path.equals("/")) {
            return null;
        }

        String rootDir = path.replaceFirst("\\/", "").replaceAll("\\/.*$", "");
        String fsPath = path.replaceFirst("\\/[^\\/]+", "");
        return new File(configReader.getRootDirs().get(rootDir) + fsPath);
    }

    public void writeStream(InputStream is, HttpServletResponse response) throws IOException {
        try {
            IOUtils.copyLarge(is, response.getOutputStream());
        } finally {
            is.close();
            response.getOutputStream().flush();
            response.getOutputStream().close();
        }
    }
}
