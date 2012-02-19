/*
 * Copyright (c) 2012 Sergey Prilukin
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

import org.junit.Before;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.inject.annotation.TestedObject;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link CustomFileSystemView}
 *
 * @author Sergey Prilukin
 */
public class CustomFileSystemViewTest extends UnitilsJUnit4 {

    @TestedObject
    private CustomFileSystemView customFileSystemView;

    private Map<String, String> rootDirs;

    @Before
    public void before() {
        rootDirs = new HashMap<String, String>();
        rootDirs.put("c", "c:\\");
        rootDirs.put("d", "d:\\");
    }

    @Test
    public void testCannotCDUPFromRootDir() throws Exception {
        customFileSystemView.setRootDirs(rootDirs);

        boolean changed = customFileSystemView.changeWorkingDirectory("..");
        assertFalse(changed);
        assertEquals(RootFtpDir.ROOT_PATH,  customFileSystemView.getWorkingDirectory().getAbsolutePath());
    }

    @Test
    public void testChangeToNonASCIIDir() throws Exception {
        customFileSystemView.setRootDirs(rootDirs);

        boolean changed = customFileSystemView.changeWorkingDirectory("d/test test/папка 1");
        assertTrue(changed);
        assertEquals("/d/test test/папка 1",  customFileSystemView.getWorkingDirectory().getAbsolutePath());
    }

    @Test
    public void testChangeDirRelatively() throws Exception {
        customFileSystemView.setRootDirs(rootDirs);

        boolean changed = customFileSystemView.changeWorkingDirectory("d/test");
        assertTrue(changed);
        assertEquals("/d/test",  customFileSystemView.getWorkingDirectory().getAbsolutePath());

        changed = customFileSystemView.changeWorkingDirectory("./../../d/test/../..");
        assertTrue(changed);
        assertEquals("/",  customFileSystemView.getWorkingDirectory().getAbsolutePath());
    }
}
