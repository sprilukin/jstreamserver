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

import java.io.File;
import java.util.List;

/**
 * Service to work with file system
 *
 * @author Sergey Prilukin
 */
public interface FolderService {

    /**
     * Return folder content for given path
     *
     * @param file folder in file system. {@code null} for root folder
     * @return folder content for given folder
     */
    public List<FileListEntry> getFolderContent(File file, String path);

    /**
     * Return breadcrumbs for given path
     * @param path path to folder
     * @return breadcrumbs for given folder
     */
    public List<BreadCrumb> getBreadCrumbs(String path);
}
