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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Implemntation of {@link java.io.InputStream}
 * which allows to {@link java.io.InputStream} file which is grows dynamically
 * (for example a file which is written by other thread)
 *
 * @author Sergey Prilukin
 */
public class GrowingFileInputStream extends InputStream {

    public static final int DEFAULT_BUFFER_SIZE = 4 * 1024;
    public static final int MAX_TIMEOUT = 10 * 1000; //10 sec
    public static final int WAIT_TIME = 1000; //1 sec

    private InputStream inputStream;
    private File file;

    private int maxTimeout = MAX_TIMEOUT;
    private int waitTime = WAIT_TIME;
    private int bufferSize = DEFAULT_BUFFER_SIZE;

    //private final Object monitor = new Object();
    private int timeout = 0;

    public GrowingFileInputStream(InputStream inputStream, int maxTimeout, int waitTime) {
        this.maxTimeout = maxTimeout;
        this.waitTime = waitTime;
        this.inputStream = inputStream;
    }

    public GrowingFileInputStream(InputStream inputStream, int maxTimeout) {
        this(inputStream, maxTimeout, WAIT_TIME);
    }

    public GrowingFileInputStream(InputStream inputStream) {
        this(inputStream, MAX_TIMEOUT, WAIT_TIME);
    }

    public GrowingFileInputStream(File file, int maxTimeout, int waitTime, int bufferSize) {
        this.maxTimeout = maxTimeout;
        this.waitTime = waitTime;
        this.bufferSize = bufferSize;
        this.file = file;
    }

    public GrowingFileInputStream(File file, int maxTimeout, int waitTime) {
        this(file, maxTimeout, waitTime, DEFAULT_BUFFER_SIZE);
    }

    public GrowingFileInputStream(File file, int maxTimeout) {
        this(file, maxTimeout, WAIT_TIME, DEFAULT_BUFFER_SIZE);
    }

    public GrowingFileInputStream(File file) {
        this(file, MAX_TIMEOUT, WAIT_TIME, DEFAULT_BUFFER_SIZE);
    }

    interface ReadInCycleListener {
        public abstract int dataAvailable(int count) throws IOException;
        public int onTimeout();
    }

    abstract class ReadInCycleListenerAdapter implements ReadInCycleListener {
        public abstract int dataAvailable(int count) throws IOException;
        public int onTimeout() {
            //Default assumption on timeout - EOF.
            return -1;
        }
    }

    private void ensureInputStreamCreated() {
        if (this.inputStream != null) {
            return;
        }

        if (file != null) {
            //wait until file will be created
            try {
                while (timeout < maxTimeout) {
                    if (file.exists()) {
                        timeout = 0;
                        this.inputStream = new BufferedInputStream(new FileInputStream(file), bufferSize);
                        return;
                    } else {
                        synchronized (this) {
                            timeout += waitTime;
                            this.wait(waitTime);
                        }
                    }
                }

                throw new RuntimeException("Timeout reached while waiting for file creation");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    //Calls dataAvailable method of passed listener if can read some bytes.
    //In other case try to sleep {@code waitTime} and try again data will be availabe
    // or timeout will be reached
    private int readInCycle(ReadInCycleListener listener) throws IOException {
        ensureInputStreamCreated();

        try {
            while (timeout < maxTimeout) {
                if (inputStream.available() > 0) {
                    timeout = 0;
                    return listener.dataAvailable(inputStream.available());
                } else {
                    synchronized (this) {
                        timeout += waitTime;
                        this.wait(waitTime);
                    }
                }
            }

            //Reached timeout.
            return listener.onTimeout();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override
    public int read() throws IOException {
        return readInCycle(new ReadInCycleListenerAdapter() {
            @Override
            public int dataAvailable(int count) throws IOException {
                return inputStream.read();
            }
        });
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        return readInCycle(new ReadInCycleListenerAdapter() {
            @Override
            public int dataAvailable(int count) throws IOException {
                return inputStream.read(b, off, Math.min(count, len));
            }
        });
    }

    @Override
    public int read(final byte[] b) throws IOException {
        return readInCycle(new ReadInCycleListenerAdapter() {
            @Override
            public int dataAvailable(int count) throws IOException {
                return inputStream.read(b);
            }
        });
    }


    @Override
    public void close() throws IOException {
        super.close();
        inputStream.close();
    }
}
