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
import jstreamserver.dto.FileListEntry;
import jstreamserver.utils.Config;
import jstreamserver.utils.HttpUtils;
import jstreamserver.utils.velocity.VelocityModel;
import jstreamserver.utils.velocity.VelocityRenderer;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Main handler for the server.
 * All server logic put here.
 *
 * @author Sergey Prilukin
 */
public final class StreamServerHandler extends BaseHandler {

    public static final String PATH_PARAM = "path";
    public static final String DIRECTORY_SEPARATOR = "/";
    public static final String ROOT_DIRECTORY = DIRECTORY_SEPARATOR;
    public static final String PARENT_FOLDER_NAME = "[..]";

    private static final Comparator<FileListEntry> FILE_LIST_COMPARATOR = new Comparator<FileListEntry>() {
        @Override
        public int compare(FileListEntry o1, FileListEntry o2) {
            if (PARENT_FOLDER_NAME.equals(o1.getName())) {
                return -1;
            } else if (PARENT_FOLDER_NAME.equals(o2.getName())) {
                return 1;
            } else if (o1.getDirectory() && !o2.getDirectory()) {
                return -1;
            } else if (o2.getDirectory() && !o1.getDirectory()) {
                return 1;
            } else {
                return o1.getName().compareTo(o2.getName());
            }
        }
    };

    public StreamServerHandler() {
        super();
    }

    public InputStream getResponseAsStream(HttpRequestContext httpRequestContext) throws IOException {
        String path = HttpUtils.getURLParams(httpRequestContext.getRequestURI().getRawQuery()).get(PATH_PARAM);
        if (path == null || ROOT_DIRECTORY.equals(path)) {
            return renderDirectory(ROOT_DIRECTORY, getFilesFromNamesList(getConfig().getRootDirs().values()), httpRequestContext);
        } else {
            path = URLDecoder.decode(path, HttpUtils.DEFAULT_ENCODING);
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

    private List<File> getFilesFromNamesList(Collection<String> names) {
        List<File> files = new ArrayList<File>();
        for (String name: names) {
            files.add(new File(name));
        }

        return files;
    }

    private List<File> getDirectoryContent(File dir) {
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !pathname.isHidden();
            }
        });

        return Arrays.asList(files);
    }

    private List<FileListEntry> getFiles(List<File> files, String parentPath) {

        List<FileListEntry> fileList = new ArrayList<FileListEntry>();

        try {

            if (!ROOT_DIRECTORY.equals(parentPath)) {
                String parentDirPath = parentPath.replaceAll("\\/$", "").replaceAll("\\/[^\\/]+$", "");
                FileListEntry parentDir = new FileListEntry();
                parentDir.setDirectory(true);
                parentDir.setName(PARENT_FOLDER_NAME);
                parentDir.setUrl(URLEncoder.encode(parentDirPath.isEmpty() ? ROOT_DIRECTORY : parentDirPath, HttpUtils.DEFAULT_ENCODING));

                fileList.add(parentDir);
            }

            String parentDir = ROOT_DIRECTORY.equals(parentPath) ? ROOT_DIRECTORY : parentPath + DIRECTORY_SEPARATOR;


            for (File file : files) {
                FileListEntry entry = new FileListEntry();

                if (file.isFile()) {
                    String extension = FilenameUtils.getExtension(file.getName());
                    String mimeType = getMimeProperties().getProperty(extension.toLowerCase(), "application/octet-stream");

                    entry.setDirectory(false);
                    entry.setMimeType(mimeType);
                    entry.setExtension(extension);
                } else {
                    entry.setDirectory(true);
                }

                String name = file.getName();
                if (name.isEmpty()) {
                    name = file.getPath().replaceAll("[\\/\\\\\\:]", "");
                }

                entry.setName(name);
                entry.setUrl(URLEncoder.encode(parentDir + name, HttpUtils.DEFAULT_ENCODING));

                fileList.add(entry);
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        Collections.sort(fileList, FILE_LIST_COMPARATOR);

        return fileList;
    }

    private InputStream renderDirectory(String path, List<File> children, HttpRequestContext httpRequestContext) throws IOException {
        VelocityModel model = new VelocityModel();
        model.put("files", getFiles(children, path));
        model.put("config", getConfig());

        InputStream result = VelocityRenderer.renderTemplate("jstreamserver/templates/directory.vm", model);

        setResponseSize(result.available(), httpRequestContext);
        setResponseCode(HttpURLConnection.HTTP_OK, httpRequestContext);
        setContentType(HttpUtils.DEFAULT_TEXT_CONTENT_TYPE, httpRequestContext);
        return result;
    }
}
