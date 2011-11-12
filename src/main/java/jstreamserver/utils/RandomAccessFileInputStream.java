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

package jstreamserver.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * Helper class which wraps {@link java.io.RandomAccessFile} into {@link java.io.InputStream}
 *
 * @author Sergey Prilukin
 */
public final class RandomAccessFileInputStream extends InputStream {
    private RandomAccessFile raf;
    private int maxBytesToRead;
    private int currentPos = 0;

    public RandomAccessFileInputStream(RandomAccessFile raf) {
        this.raf = raf;
        this.maxBytesToRead = Integer.MAX_VALUE;
    }

    public RandomAccessFileInputStream(RandomAccessFile raf, long startPos, int maxBytesToRead) {
        this.raf = raf;
        this.maxBytesToRead = maxBytesToRead;
        try {
            if (startPos > 0) {
                raf.seek(startPos);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public RandomAccessFileInputStream(File file, long startPos, int maxBytesToRead) {
        this.maxBytesToRead = maxBytesToRead;
        try {
            this.raf = new RandomAccessFile(file, "r");
            if (startPos > 0) {
                raf.seek(startPos);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int read() throws IOException {
        if (maxBytesToRead - currentPos > 0) {
            currentPos++;
            return raf.read();
        } else {
            return -1;
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int maxBytesLeftToRead = maxBytesToRead - currentPos;
        if (maxBytesLeftToRead > 0) {
            int readLength = Math.min(maxBytesLeftToRead, len);
            currentPos += readLength;
            return raf.read(b, off, readLength);
        } else {
            return -1;
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        raf.close();
    }
}
