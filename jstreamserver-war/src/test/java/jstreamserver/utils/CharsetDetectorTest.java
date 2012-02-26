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

import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link CharsetDetector}
 *
 * @author Sergey Prilukin
 */
public class CharsetDetectorTest {

    @Test
    public void testWindows1251Detection() throws Exception {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("jstreamserver/utils/windows-1251.txt");
        String charset = CharsetDetector.detect(is);
        assertEquals("windows-1251", charset);
        is.close();
    }

    @Test
    public void testUTF8Detection() throws Exception {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("jstreamserver/utils/utf-8.txt");
        String charset = CharsetDetector.detect(is);
        assertEquals("utf-8", charset);
        is.close();
    }
}
