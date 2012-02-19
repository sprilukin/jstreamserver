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

import org.apache.ftpserver.ftplet.FtpFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Root directory
 *
 * @author Sergey Prilukin
 */
public class RootFtpDir implements FtpFile {
    public static String ROOT_PATH = "/";
    public static String ROOT_NAME = "/";

    private Map<String, String> rootDirs;

    public RootFtpDir(Map<String, String> rootDirs) {
        this.rootDirs = rootDirs;
    }

    @Override
    public String getAbsolutePath() {
        return ROOT_PATH;
    }

    @Override
    public String getName() {
        return ROOT_NAME;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public boolean doesExist() {
        return true;
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public boolean isRemovable() {
        return false;
    }

    @Override
    public String getOwnerName() {
        return null;
    }

    @Override
    public String getGroupName() {
        return null;
    }

    @Override
    public int getLinkCount() {
        return 0;
    }

    @Override
    public long getLastModified() {
        return 0;
    }

    @Override
    public boolean setLastModified(long time) {
        return false;
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public boolean mkdir() {
        return false;
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public boolean move(FtpFile destination) {
        return false;
    }

    @Override
    public List<FtpFile> listFiles() {
        List<FtpFile> chidren = new ArrayList<FtpFile>(rootDirs.size());
        for (Map.Entry<String, String> dir: rootDirs.entrySet()) {
            File file = new File(dir.getValue());
            chidren.add(new CustomFtpFile(ROOT_PATH + dir.getKey(), file));
        }

        return chidren;
    }

    @Override
    public OutputStream createOutputStream(long offset) throws IOException {
        return null;
    }

    @Override
    public InputStream createInputStream(long offset) throws IOException {
        return null;
    }
}
