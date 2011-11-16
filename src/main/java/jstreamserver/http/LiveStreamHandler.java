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
import jstreamserver.utils.Config;
import jstreamserver.utils.Destroyable;
import jstreamserver.utils.EncodingUtil;
import jstreamserver.utils.HtmlRenderer;
import jstreamserver.utils.ffmpeg.FFMpegSegmenter;
import jstreamserver.utils.ffmpeg.FrameMessage;
import jstreamserver.utils.ffmpeg.ProcessAwareProgressListener;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.text.MessageFormat;

/**
 * HTTP Live stream handler
 * which implements <a href="http://developer.apple.com/resources/http-streaming/">HTTP Live Streaming</a>
 * {@code FFMpeg} and some implementation of {@code video segmenter} are used on backend.
 *
 * @author Sergey Prilukin
 */
public final class LiveStreamHandler extends BaseHandler {

    public static final String HANDLE_PATH = "/livestream";
    public static final String LIVE_STREAM_FILE_PREFIX = "stream";
    public static final String PLAYLIST_EXTENSION = "m3u8";
    public static final String LIVE_STREAM_FILE_PATH = HANDLE_PATH.substring(1) + "/" + LIVE_STREAM_FILE_PREFIX;
    public static final String PLAYLIST_FULL_PATH = HANDLE_PATH + "/" + LIVE_STREAM_FILE_PREFIX + "." + PLAYLIST_EXTENSION;
    public static final int DESTROY_SEGMENTER_DELAY = 1000;
    public static final int START_STREAMING_DELAY = 4000;

    private String currentStreamingFile;
    private Thread segmenterKiller;
    private FFMpegSegmenter ffMpegSegmenter;
    private ProcessAwareProgressListener progressListener;

    public LiveStreamHandler() {
        super();
    }

    public LiveStreamHandler(Config config) {
        super(config);
    }

    @Override
    public InputStream getResponseAsStream(HttpRequestContext httpRequestContext) throws IOException {

        if (PLAYLIST_FULL_PATH.equals(httpRequestContext.getRequestURI().getPath())) {
            File playlist = new File(LIVE_STREAM_FILE_PATH + "." + PLAYLIST_EXTENSION);
            return getPlayList(playlist, httpRequestContext);
        } else if (HANDLE_PATH.equals(httpRequestContext.getRequestURI().getPath())) {
            String param = httpRequestContext.getRequestURI().getRawQuery();

            String fileString = param.split("=")[1];
            File file = getFile(URLDecoder.decode(fileString, EncodingUtil.UTF8_ENCODING));
            if (!file.exists() || !file.isFile() || file.isHidden()) {
                return rendeResourceNotFound(fileString, httpRequestContext);
            } else {
                return getLiveStream(file, httpRequestContext);
            }
        } else {
            String path = httpRequestContext.getRequestURI().getPath();
            File file = new File(path.substring(1));
            if (file.exists() && file.isFile()) {
                return getResource(file, httpRequestContext);
            } else {
                return rendeResourceNotFound(path, httpRequestContext);
            }
        }
    }

    private InputStream getPlayList(File playListFile, HttpRequestContext httpRequestContext) throws IOException {
        startSegmenterKiller();
        return getResource(playListFile, httpRequestContext);
    }

    private InputStream getLiveStream(File file, HttpRequestContext httpRequestContext) throws IOException {

        if (ffMpegSegmenter != null) {
            ffMpegSegmenter.destroy();
        }

        try {
            Thread.sleep(DESTROY_SEGMENTER_DELAY);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        ffMpegSegmenter = new FFMpegSegmenter();
        progressListener = new LiveStreamProgressListener(ffMpegSegmenter);

        ffMpegSegmenter.start(
                getConfig().getFfmpegLocation(),
                getConfig().getSegmenterLocation(),
                MessageFormat.format("-i \"{0}\" {1} -", file.getAbsolutePath(), getConfig().getFfmpegParams()),
                MessageFormat.format("- " + getConfig().getSegmenterParams(), LIVE_STREAM_FILE_PATH, PLAYLIST_FULL_PATH.substring(1)),
                progressListener);

        try {
            Thread.sleep(START_STREAMING_DELAY);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        byte[] response = HtmlRenderer.renderLiveStreamPage(PLAYLIST_FULL_PATH).getBytes();

        setContentType(DEFAULT_HTML_CONTENT_TYPE, httpRequestContext);
        setResponseSize(response.length, httpRequestContext);
        setResponseCode(HttpURLConnection.HTTP_OK, httpRequestContext);
        return new ByteArrayInputStream(response);
    }

    private void startSegmenterKiller() {
        if (segmenterKiller == null || !segmenterKiller.isAlive()) {
            //TODO
        }
    }

    class LiveStreamProgressListener extends ProcessAwareProgressListener {
        LiveStreamProgressListener(Destroyable process) {
            super(process);
        }

        @Override
        public void onFrameMessage(FrameMessage frameMessage) {
            System.out.println(frameMessage.toString());
        }

        @Override
        public void onProgress(String progressString) {
            System.out.println(progressString);
        }

        @Override
        public void onFinish(int exitCode) {
            System.out.println("Segmenter finished. Exit code: " + exitCode);
        }
    }
}
