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

import jstreamserver.ffmpeg.FFMpegConstants;
import jstreamserver.ffmpeg.FFMpegSegmenter;
import jstreamserver.ffmpeg.FrameMessage;
import jstreamserver.ffmpeg.ProgressListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

/**
 * Class to operate with LiveStream
 *
 * @author Sergey Prilukin
 */
public final class LiveStreamer {

    private static final Log log = LogFactory.getLog(LiveStreamer.class);

    public static final String LIVE_STREAM_FOLDER = "livestream";
    public static final String LIVE_STREAM_FILE_PREFIX = "stream%s";
    public static final String PLAYLIST_EXTENSION = "m3u8";
    public static final String LIVE_STREAM_FILE_PATH = LIVE_STREAM_FOLDER + "/" + LIVE_STREAM_FILE_PREFIX;
    public static final String PLAYLIST_FULL_PATH = LIVE_STREAM_FILE_PATH + "." + PLAYLIST_EXTENSION;

    private ConfigReader configReader;

    private FFMpegSegmenter ffMpegSegmenter;
    private final Object ffmpegSegmenterMonitor = new Object();
    private final Object playListCreatedMonitor = new Object();
    private ProgressListener progressListener;
    private SegmenterKiller segmenterKiller;

    private String liveStreamFolderSuffix;
    private String contextPath;

    public LiveStreamer(String contextPath, String liveStreamFolderSuffix, ProgressListener progressListener, ConfigReader configReader) {
        this.liveStreamFolderSuffix = liveStreamFolderSuffix;
        this.contextPath = contextPath.endsWith("/") ? contextPath : contextPath + "/";
        this.progressListener = new LiveStreamProgressListener(progressListener);
        this.configReader = configReader;
    }

    private String appendLiveStreamFolderSuffix(String notFormattedString) {
        return String.format(notFormattedString, liveStreamFolderSuffix);
    }

    public File getTSFile(String path) {
        return new File(LIVE_STREAM_FOLDER + "/" + path);
    }
    
    /**
     * Playlist file is written at the same time by another thread (by segmenter namely)
     * and thus this thread can read non-completed version of the file.
     * In this method we ensure that last line of playlist matches one of the possible formats
     */
    public InputStream getPlayList() throws IOException {

        boolean fileIsOk = false;
        StringBuilder sb = null;

        while (!fileIsOk) {
            String fileName = appendLiveStreamFolderSuffix(PLAYLIST_FULL_PATH);
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            sb = new StringBuilder();

            String line = "";

            while (true) {
                String temp = reader.readLine();

                if (temp != null) {
                    line = temp;
                    sb.append(line).append("\n");
                } else {
                    fileIsOk = line.matches("^(.*\\.ts|#.*)$");
                    reader.close();
                    break;
                }
            }
        }

        return new ByteArrayInputStream(sb.toString().getBytes());
    }

    public void destroyLiveStream() {
        synchronized (ffmpegSegmenterMonitor) {
            if (ffMpegSegmenter != null) {
                ffMpegSegmenter.destroy();
                ffMpegSegmenter = null;
            }
        }

        File streamDir = new File(appendLiveStreamFolderSuffix(LIVE_STREAM_FOLDER));
        if (!streamDir.exists()) {
            streamDir.mkdirs();
        }

        //Remove all .ts and .m3u8 files
        String[] files = streamDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                //String extension = FilenameUtils.getExtension(name);
                return name.startsWith(appendLiveStreamFolderSuffix(LIVE_STREAM_FILE_PREFIX));
            }
        });

        for (String fileName: files) {
            File file = new File(streamDir + "/" + fileName);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    public void startLiveStream(File file, String startTime, Integer audioStreamId) throws IOException {

        destroyLiveStream();

        //Need to use HTTP Live Streaming
        String ffmpegMapStreamParams = audioStreamId != null ? String.format(FFMpegConstants.FFMPEG_AUDIO_STREAM_SELECTION_FORMAT, audioStreamId) : "";
        String ffmpegStartTimeParam = startTime != null ? String.format(FFMpegConstants.FFMPEG_START_TIME_FORMAT, startTime ) : "";

        synchronized (ffmpegSegmenterMonitor) {
            ffMpegSegmenter = new FFMpegSegmenter();

            ffMpegSegmenter.start(
                    configReader.getFfmpegLocation(),
                    configReader.getSegmenterLocation(),
                    String.format(configReader.getFfmpegParams(), file.getAbsolutePath(), ffmpegMapStreamParams, ffmpegStartTimeParam),
                    String.format(configReader.getSegmenterParams(),
                            appendLiveStreamFolderSuffix(LIVE_STREAM_FILE_PATH),
                            appendLiveStreamFolderSuffix(PLAYLIST_FULL_PATH),
                            contextPath),
                    progressListener);

            try {
                synchronized (playListCreatedMonitor) {
                    playListCreatedMonitor.wait();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        updateSegmenterKiller();
    }

    private void updateSegmenterKiller() {
        if (segmenterKiller == null) {
            segmenterKiller = new SegmenterKiller();
            segmenterKiller.start();
        } else {
            segmenterKiller.clearTimeout();
        }
    }

    class SegmenterKiller extends Thread {
        private boolean timeoutFlag = false;

        public void clearTimeout() {
            synchronized (ffmpegSegmenterMonitor) {
                timeoutFlag = false;
                ffmpegSegmenterMonitor.notify();
            }
        }

        @Override
        public void run() {
            try {
                synchronized (ffmpegSegmenterMonitor) {
                    while (true) {
                        timeoutFlag = true;
                        ffmpegSegmenterMonitor.wait(configReader.getSegmenterMaxtimeout());
                        if (timeoutFlag && ffMpegSegmenter != null) {
                            log.debug("Destroying idle ffmpeg segmenter...");
                            timeoutFlag = false;
                            destroyLiveStream();
                            ffmpegSegmenterMonitor.wait();
                        }
                    }
                }
            } catch (InterruptedException e) {
                /* do nothing */
            }
        }
    }

    class LiveStreamProgressListener implements ProgressListener {

        private ProgressListener listener;

        LiveStreamProgressListener(ProgressListener listener) {
            this.listener = listener;
        }

        @Override
        public void onFrameMessage(FrameMessage frameMessage) {
            listener.onFrameMessage(frameMessage);
        }

        @Override
        public void onProgress(String progressString) {
            listener.onProgress(progressString);
        }

        @Override
        public void onPlayListCreated() {
            synchronized (playListCreatedMonitor) {
                playListCreatedMonitor.notify();
            }

            listener.onPlayListCreated();
        }

        @Override
        public void onFinish(int exitCode) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Segmenter [%s] finished. Exit code: %s", liveStreamFolderSuffix, exitCode));
            }

            listener.onFinish(exitCode);
        }
    }

}
