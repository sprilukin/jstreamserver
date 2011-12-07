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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * POJO which holds config settings for the server.
 *
 * @author Sergey Prilukin
 */
public final class Config {
    public static final String SEGMENTER_PARAMS_FORMAT = "- {0} %s %s / {1} {2}";
    public static final String FFMPEG_PARAMS_FORMAT = "-i \"%s\" {0} -";

    private int port = 8888;
    private String host = "0.0.0.0";
    private int maxThreads = 10;
    private String charset = "windows-1251";
    private Map<String, String> rootDirs = new TreeMap<String, String>();
    private String mimeProperties = null;
    private String ffmpegLocation = null; //ffmpeg not available by default
    private String ffmpegParams = "-f mpegts -acodec libmp3lame -ab 64000 -s 480x320 -vcodec libx264 -b 480000 -flags +loop -cmp +chroma -partitions +parti4x4+partp8x8+partb8x8 -subq 5 -trellis 1 -refs 1 -coder 0 -me_range 16  -keyint_min 25 -sc_threshold 40 -i_qfactor 0.71 -bt 400k -maxrate 524288 -bufsize 524288 -rc_eq 'blurCplx^(1-qComp)' -qcomp 0.6 -qmin 10 -qmax 51 -qdiff 4 -level 30 -aspect 480:320 -g 30 -async 2";
    private String segmenterLocation = null; //segmenter not available by default
    private int segmentDurationInSec = 10;
    private int segmentWindowSize = 2 * 60 * 60 / segmentDurationInSec;
    private int segmenterSearchKillFile = 1;
    private int destroySegmenterDelay = 1000;
    private int startSegmenterDelay = 5000;
    private int segmenterMaxtimeout = Math.max(segmentDurationInSec * 100 * 3, 30000); //3 times of segment duration. should be enough


    private static List<String> iosSupportedVideoExtensions = new ArrayList<String>();
    static {
        iosSupportedVideoExtensions.add("qt");
        iosSupportedVideoExtensions.add("mov");
        iosSupportedVideoExtensions.add("mp4");
        iosSupportedVideoExtensions.add("m4v");
        iosSupportedVideoExtensions.add("ts");
    }

    public Config() {
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public Map<String, String> getRootDirs() {
        return rootDirs;
    }

    public void setRootDirs(Map<String, String> rootDirs) {
        this.rootDirs = rootDirs;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getMimeProperties() {
        return mimeProperties;
    }

    public void setMimeProperties(String mimeProperties) {
        this.mimeProperties = mimeProperties;
    }

    public String getFfmpegLocation() {
        return ffmpegLocation;
    }

    public void setFfmpegLocation(String ffmpegLocation) {
        this.ffmpegLocation = ffmpegLocation;
    }

    public String getFfmpegParams() {
        return MessageFormat.format(FFMPEG_PARAMS_FORMAT, ffmpegParams);
    }

    public void setFfmpegParams(String ffmpegParams) {
        this.ffmpegParams = ffmpegParams;
    }

    public String getSegmenterLocation() {
        return segmenterLocation;
    }

    public void setSegmenterLocation(String segmenterLocation) {
        this.segmenterLocation = segmenterLocation;
    }

    public int getSegmentDurationInSec() {
        return segmentDurationInSec;
    }

    public void setSegmentDurationInSec(int segmentDurationInSec) {
        this.segmentDurationInSec = segmentDurationInSec;
    }

    public int getSegmentWindowSize() {
        return segmentWindowSize;
    }

    public void setSegmentWindowSize(int segmentWindowSize) {
        this.segmentWindowSize = segmentWindowSize;
    }

    public int getSegmenterSearchKillFile() {
        return segmenterSearchKillFile;
    }

    public void setSegmenterSearchKillFile(int segmenterSearchKillFile) {
        this.segmenterSearchKillFile = segmenterSearchKillFile;
    }

    public int getDestroySegmenterDelay() {
        return destroySegmenterDelay;
    }

    public void setDestroySegmenterDelay(int destroySegmenterDelay) {
        this.destroySegmenterDelay = destroySegmenterDelay;
    }

    public int getStartSegmenterDelay() {
        return startSegmenterDelay;
    }

    public void setStartSegmenterDelay(int startSegmenterDelay) {
        this.startSegmenterDelay = startSegmenterDelay;
    }

    public int getSegmenterMaxtimeout() {
        return segmenterMaxtimeout;
    }

    public void setSegmenterMaxtimeout(int segmenterMaxtimeout) {
        this.segmenterMaxtimeout = segmenterMaxtimeout;
    }

    public String getSegmenterParams() {
        return MessageFormat.format(
                SEGMENTER_PARAMS_FORMAT,
                String.valueOf(segmentDurationInSec),
                String.valueOf(segmentWindowSize),
                String.valueOf(segmenterSearchKillFile));
    }

    public boolean httpLiveStreamingSupported(String extension, String mimeType) {
        return mimeType != null && extension != null &&
                mimeType.startsWith("video") && !iosSupportedVideoExtensions.contains(extension) &&
                ffmpegLocation != null && segmenterLocation != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Jstreamserver config:\r\n");
        sb.append("listening on: ").append(host).append(":").append(port).append("\r\n");
        sb.append("max threads count: ").append(maxThreads).append("\r\n");
        if (mimeProperties != null) {
            sb.append("mimeProperties: ").append(mimeProperties).append("\r\n");
        }
        if (ffmpegLocation != null) {
            sb.append("ffmpegLocation: ").append(ffmpegLocation).append("\r\n");
            sb.append("ffmpegParams: ").append(ffmpegParams).append("\r\n");
        } else {
            sb.append("ffmpegLocation not set - HTTP Live Streaming is not available\r\n");
        }

        if (segmenterLocation != null) {
            sb.append("segmenterLocation: ").append(segmenterLocation).append("\r\n");
            //sb.append("segmenterParams: ").append(segmenterParams).append("\r\n");
            sb.append("segmentDurationInSec: ").append(segmentDurationInSec).append("\r\n");
            sb.append("segmentWindowSize: ").append(segmentWindowSize).append("\r\n");
            sb.append("segmenterSearchKillFile: ").append(segmenterSearchKillFile).append("\r\n");
            sb.append("destroySegmenterDelay: ").append(destroySegmenterDelay).append("\r\n");
            sb.append("startSegmenterDelay: ").append(startSegmenterDelay).append("\r\n");
            sb.append("segmenterMaxtimeout: ").append(segmenterMaxtimeout).append("\r\n");
        } else {
            sb.append("segmenterLocation not set - HTTP Live Streaming is not available\r\n");
        }

        sb.append("rootDirs:\r\n");
        for (Map.Entry<String, String> entry: rootDirs.entrySet()) {
            sb.append("\"").append(entry.getValue()).append("\"").append(" with label: ").append(entry.getKey()).append("\r\n");
        }

        return sb.append("\r\n").toString();
    }
}
