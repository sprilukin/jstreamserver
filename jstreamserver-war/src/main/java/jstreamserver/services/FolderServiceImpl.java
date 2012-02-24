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

package jstreamserver.services;

import jstreamserver.dto.BreadCrumb;
import jstreamserver.dto.FileListEntry;
import jstreamserver.utils.Config;
import jstreamserver.utils.ConfigReader;
import jstreamserver.utils.HttpUtils;
import jstreamserver.ffmpeg.FFMpegInformer;
import jstreamserver.ffmpeg.MediaInfo;
import jstreamserver.utils.MimeProperties;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Service to work with file system
 *
 * @author Sergey Prilukin
 */
@Service
public class FolderServiceImpl implements FolderService {

    public static final String PARENT_FOLDER_NAME = "[..]";
    public static final String ROOT_FOLDER_NAME = "/";
    public static final String DIRECTORY_SEPARATOR = "/";
    public static final String HOME_DIR_LABEL = "HOME";

    @Autowired
    private MimeProperties mimeProperties;

    @Autowired
    private ConfigReader configReader;

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


    @Override
    public List<FileListEntry> getFolderContent(File file, String path) {
        List<File> children = null;
        if (file == null) {
            children = getFilesFromNamesList(configReader.getRootDirs().values());
        } else {
            children = getDirectoryContent(file);
        }


        return getFiles(children, path == null ? ROOT_FOLDER_NAME : path);
    }

    @Override
    public List<BreadCrumb> getBreadCrumbs(String path) {
        return generateBreadCrumbs(path != null ? path : ROOT_FOLDER_NAME);
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
        List<FileListEntry> mediaFileList = new ArrayList<FileListEntry>();
        List<String> mediaFileNames = new ArrayList<String>();

        try {

            if (!ROOT_FOLDER_NAME.equals(parentPath)) {
                String parentDirPath = parentPath.replaceAll("\\/$", "").replaceAll("\\/[^\\/]+$", "");
                FileListEntry parentDir = new FileListEntry();
                parentDir.setDirectory(true);
                parentDir.setName(PARENT_FOLDER_NAME);
                parentDir.setUrl(URLEncoder.encode(parentDirPath.isEmpty() ? ROOT_FOLDER_NAME : parentDirPath, HttpUtils.DEFAULT_ENCODING));

                fileList.add(parentDir);
            }

            String parentDir = ROOT_FOLDER_NAME.equals(parentPath) ? ROOT_FOLDER_NAME : parentPath + DIRECTORY_SEPARATOR;


            for (File file : files) {
                addFile(fileList, mediaFileList, mediaFileNames, parentDir, file);
            }

            //Adding mediainfo to FileEnries
            if (!mediaFileNames.isEmpty()) {
                setMediaInfo(mediaFileList, mediaFileNames);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Collections.sort(fileList, FILE_LIST_COMPARATOR);

        Integer index = 0;
        for (FileListEntry file: fileList) {
            file.setId("fileList" + index++);
        }

        return fileList;
    }

    private void setMediaInfo(List<FileListEntry> mediaFileList, List<String> mediaFileNames) throws IOException {
        List<MediaInfo> mediaInfos = (new FFMpegInformer()).getInfo(mediaFileNames, configReader.getFfmpegLocation());

        for (int i = 0; i < mediaInfos.size(); i++) {
            mediaFileList.get(i).setMediaInfo(mediaInfos.get(i));
        }
    }

    private void addFile(List<FileListEntry> fileList, List<FileListEntry> mediaFileList, List<String> mediaFileNames, String parentDir, File file) throws UnsupportedEncodingException {
        FileListEntry entry = new FileListEntry();

        if (file.isFile()) {
            String extension = FilenameUtils.getExtension(file.getName());
            String mimeType = mimeProperties.getProperty(extension.toLowerCase(), "application/octet-stream");

            entry.setDirectory(false);
            entry.setMimeType(mimeType);
            entry.setExtension(extension);
            entry.setVideo(mimeType.startsWith("video"));
            entry.setAudio(mimeType.startsWith("audio"));

            if (entry.getVideo() || entry.getAudio()) {
                mediaFileList.add(entry);
                mediaFileNames.add(file.getPath());
            }
        } else {
            entry.setDirectory(true);
        }

        String name = file.getName();
        if (name.isEmpty()) {
            name = file.getPath().replaceAll("[\\/\\\\\\:]", "");
        }

        entry.setName(name);
        String urlEncodedPath = URLEncoder.encode(parentDir + name, HttpUtils.DEFAULT_ENCODING);
        entry.setUrl(urlEncodedPath);

        fileList.add(entry);
    }

    private List<BreadCrumb> generateBreadCrumbs(String path) {
        StringBuilder sb = new StringBuilder();
        List<BreadCrumb> breadCrumbs = new ArrayList<BreadCrumb>();

        String[] pathPieces = path.split("\\/");
        for (String dir: pathPieces) {
            if (sb.length() > 1 || sb.length() == 0) {
                sb.append(DIRECTORY_SEPARATOR);
            }

            sb.append(dir);
            breadCrumbs.add(new BreadCrumb(dir.isEmpty() ? HOME_DIR_LABEL : dir, sb.toString()));
        }

        return breadCrumbs;
    }
}
