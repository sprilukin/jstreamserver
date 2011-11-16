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
    public static final int START_STREAMING_DELAY = 5000;

    private String currentStreamingFile;
    private Thread segmenterKiller;

    public LiveStreamHandler() {
        super();
    }

    public LiveStreamHandler(Config config) {
        super(config);
    }

    @Override
    public synchronized InputStream getResponseAsStream(HttpRequestContext httpRequestContext) throws IOException {
        String param = httpRequestContext.getRequestURI().getRawQuery();

        if (param != null && param.startsWith("playList")) {
            File playlist = new File("stream.m3u8");
            return getPlayList(playlist, httpRequestContext);
        }

        if (param != null && param.startsWith("file")) {
            String fileString = param.split("\\&")[0].split("=")[1];
            File file = getFile(URLDecoder.decode(fileString, EncodingUtil.UTF8_ENCODING));
            if (!file.exists() || !file.isFile() || file.isHidden()) {
                return rendeResourceNotFound(fileString, httpRequestContext);
            } else {
                return getLiveStream(file, httpRequestContext);
            }
        } else {
            throw new IllegalStateException("Not implemented.");
        }
    }

    private InputStream getPlayList(File playListFile, HttpRequestContext httpRequestContext) throws IOException {
        startSegmenterKiller();
        return getResource(playListFile, httpRequestContext);
    }

    private InputStream getLiveStream(File file, HttpRequestContext httpRequestContext) throws IOException {

        FFMpegSegmenter ffMpegSegmenter = new FFMpegSegmenter();

        ProcessAwareProgressListener progressListener = new ProcessAwareProgressListener(ffMpegSegmenter) {
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
        };

        String playListUrlPrefix = httpRequestContext.getRequestHeaders().get("Host").get(0) + LiveStreamHandler.HANDLE_PATH;

        ffMpegSegmenter.start(
                getConfig().getFfmpegLocation(),
                getConfig().getSegmenterLocation(),
                MessageFormat.format("-i \"{0}\" {1} -", file.getAbsolutePath(), getConfig().getFfmpegParams()),
                MessageFormat.format("- " + getConfig().getSegmenterParams(), "stream", "stream.m3u8", playListUrlPrefix), progressListener);

        try {
            Thread.sleep(START_STREAMING_DELAY);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        byte[] response = HtmlRenderer.renderLiveStreamPage(playListUrlPrefix + "/stream.m3u8").getBytes();

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
}
