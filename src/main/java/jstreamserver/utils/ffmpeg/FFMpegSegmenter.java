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

package jstreamserver.utils.ffmpeg;

import jstreamserver.utils.RuntimeExecutor;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class implements HTTP Streaming technology
 * described <a href="http://developer.apple.com/library/ios/#documentation/networkinginternet/conceptual/streamingmediaguide/HTTPStreamingArchitecture/HTTPStreamingArchitecture.html">here</a>
 * by using <a href="http://ffmpeg.org">ffmpeg</a> and native MAC OS segmenter or some platform specific implementations:
 *  <ul><li><a href="http://code.google.com/p/httpsegmenter">windows</a></li>
 *  <li><a href="https://github.com/carsonmcdonald/HTTP-Live-Video-Stream-Segmenter-and-Distributor">linux</a></li></ul>
 *
 * @author Sergey Prilukin
 */
public final class FFMpegSegmenter {
    private RuntimeExecutor ffmpegExecutor = new RuntimeExecutor();
    private RuntimeExecutor segmenterExecutor = new RuntimeExecutor();
    private Thread streamCopier;
    private Thread segmenterInputStreamReader;
    private Thread segmenterErrorStreamReader;
    private Thread ffmpegErrorStreamReader;
    private Thread finishWaiter;

    public void setFfmpegExecutor(RuntimeExecutor ffmpegExecutor) {
        this.ffmpegExecutor = ffmpegExecutor;
    }

    public void setSegmenterExecutor(RuntimeExecutor segmenterExecutor) {
        this.segmenterExecutor = segmenterExecutor;
    }

    public void start(String ffmpegPath, String segmenterPath, String ffmpegParams, String segmenterParams, ProgressListener progressListener) throws IOException {
        ffmpegExecutor.execute(ffmpegPath, ffmpegParams.split("[\\s]+"));
        segmenterExecutor.execute(segmenterPath, segmenterParams.split("[\\s]+"));

        streamCopier = new Thread(new StreamCopier(ffmpegExecutor.getInputStream(), segmenterExecutor.getOutputStream()), "StreamCopier");
        segmenterInputStreamReader = new Thread(new InputReader(segmenterExecutor.getInputStream(), progressListener), "SegmenterInputReader");
        segmenterErrorStreamReader = new Thread(new InputReader(segmenterExecutor.getErrorStream(), progressListener), "SegmenterErrorReader");
        ffmpegErrorStreamReader = new Thread(new InputReader(ffmpegExecutor.getErrorStream(), progressListener), "FFMpegInputReader");
        finishWaiter = new Thread(new FinishWaiter(progressListener));

        streamCopier.start();
        segmenterInputStreamReader.start();
        segmenterErrorStreamReader.start();
        ffmpegErrorStreamReader.start();
        finishWaiter.start();
    }

    public void destroy() {
        ffmpegExecutor.destroy();
        segmenterExecutor.destroy();
    }

    public void waitFor() throws InterruptedException {
        segmenterExecutor.waitFor();
        streamCopier.join();
        segmenterInputStreamReader.join();
        segmenterErrorStreamReader.join();
        ffmpegErrorStreamReader.join();
        finishWaiter.join();
    }

    class StreamCopier implements Runnable {
        private InputStream inputStream;
        private OutputStream outputStream;

        StreamCopier(InputStream inputStream, OutputStream outputStream) {
            this.inputStream = inputStream;
            this.outputStream = outputStream;
        }

        @Override
        public void run() {
            try {
                IOUtils.copyLarge(inputStream, outputStream);
            } catch (IOException e) {
                /* ignore */
            } finally {
                try {
                    inputStream.close();
                    outputStream.close();
                } catch (IOException e) {
                    /* Ignore */
                }
            }
        }
    }

    /**
     * Utility class which reads text lines from passed {@link InputStream}
     * And reports progress to passed {@link ProgressListener}
     */
    class InputReader implements Runnable {
        private BufferedReader reader;
        private ProgressListener progressListener;

        public InputReader(InputStream inputStream, ProgressListener progressListener) {
            this.reader = new BufferedReader(new InputStreamReader(inputStream));
            this.progressListener = progressListener;
        }

        @Override
        public void run() {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = Pattern.compile(FrameMessage.FRAME_PATTERN).matcher(line);
                    if (matcher.find()) {
                        FrameMessage frameMessage = new FrameMessage();
                        frameMessage.setFrameNumber(Long.parseLong(matcher.group(1)));
                        frameMessage.setFps(Integer.parseInt(matcher.group(2)));
                        frameMessage.setQ(new BigDecimal(matcher.group(3)));
                        frameMessage.setSize(Long.parseLong(matcher.group(4)));
                        frameMessage.setTime(FrameMessage.DATE_FORMAT.parse(matcher.group(5)).getTime());
                        frameMessage.setBitrate(new BigDecimal(matcher.group(6)));
                        frameMessage.setDup(Long.parseLong(matcher.group(7)));
                        frameMessage.setDrop(Long.parseLong(matcher.group(8)));

                        progressListener.onFrameMessage(frameMessage);
                    } else {
                        progressListener.onProgress(line);
                    }
                }
            } catch (Exception e) {
                /* ignore */
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    /* ignore */
                }
            }
        }
    }

    /**
     * Utility class which wait untill process is finished and
     * then calls {@code onFinish} method of passed {@link ProgressListener}
     */
    class FinishWaiter implements Runnable {
        private ProgressListener progressListener;

        public FinishWaiter(ProgressListener progressListener) {
            this.progressListener = progressListener;
        }

        @Override
        public void run() {
            try {
                ffmpegExecutor.waitFor();
                segmenterExecutor.waitFor();
                this.progressListener.onFinish(segmenterExecutor.getExitCode());
            } catch (InterruptedException e) {
                this.progressListener.onFinish(segmenterExecutor.getExitCode());
            }
        }
    }
}
