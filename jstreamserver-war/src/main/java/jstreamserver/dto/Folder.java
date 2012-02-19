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

package jstreamserver.dto;

import java.util.List;

/**
 * DTO object which represents folder content
 *
 * @author Sergey Prilukin
 */
public class Folder {
    private List<FileListEntry> files;
    private List<BreadCrumb> breadcrumbs;

    public Folder(List<FileListEntry> files, List<BreadCrumb> breadcrumbs) {
        this.files = files;
        this.breadcrumbs = breadcrumbs;
    }

    public List<FileListEntry> getFiles() {
        return files;
    }

    public void setFiles(List<FileListEntry> files) {
        this.files = files;
    }

    public List<BreadCrumb> getBreadcrumbs() {
        return breadcrumbs;
    }

    public void setBreadcrumbs(List<BreadCrumb> breadcrumbs) {
        this.breadcrumbs = breadcrumbs;
    }
}
