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
    private int port = 8888;
    private String host = "0.0.0.0";
    private int maxThreads = 10;
    private String charset = "windows-1251";
    private Map<String, String> rootDirs = new TreeMap<String, String>();
    private String mimeProperties = null;
    private int bufferSize = 1024 * 1024; //1MB
    private String ffmpegLocation = null; //ffmpeg not available by default
    private String ffmpegParams = "-f mpegts -acodec libmp3lame -ab 64000 -s 480x320 -vcodec libx264 -b 480000 -flags +loop -cmp +chroma -partitions +parti4x4+partp8x8+partb8x8 -subq 5 -trellis 1 -refs 1 -coder 0 -me_range 16  -keyint_min 25 -sc_threshold 40 -i_qfactor 0.71 -bt 400k -maxrate 524288 -bufsize 524288 -rc_eq 'blurCplx^(1-qComp)' -qcomp 0.6 -qmin 10 -qmax 51 -qdiff 4 -level 30 -aspect 480:320 -g 30 -async 2";
    private String segmenterLocation = null; //segmenter not available by default
    private String segmenterParams = "10 {0} {1} {2} 5 1";

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

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public String getFfmpegLocation() {
        return ffmpegLocation;
    }

    public void setFfmpegLocation(String ffmpegLocation) {
        this.ffmpegLocation = ffmpegLocation;
    }

    public String getFfmpegParams() {
        return ffmpegParams;
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

    public String getSegmenterParams() {
        return segmenterParams;
    }

    public void setSegmenterParams(String segmenterParams) {
        this.segmenterParams = segmenterParams;
    }

    public boolean httpLiveStreamingSupported(String extension, String mimeType) {
        return mimeType.startsWith("video") && !iosSupportedVideoExtensions.contains(extension) &&
                ffmpegLocation != null && segmenterLocation != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Jstreamserver config:\r\n");
        sb.append("listening on: ").append(host).append(":").append(port).append("\r\n");
        sb.append("max threads count: ").append(maxThreads).append("\r\n");
        sb.append("response buffer size: ").append(bufferSize).append("\r\n");
        if (mimeProperties != null) {
            sb.append("mimeProperties: ").append(mimeProperties).append("\r\n");
        }
        if (ffmpegLocation != null) {
            sb.append("ffmpegLocation: ").append(ffmpegLocation).append("\r\n");
            sb.append("ffmpegParams: ").append(ffmpegParams).append("\r\n");
        } else {
            sb.append("ffmpegLocation not set - live conversion is not available\r\n");
        }

        if (segmenterLocation != null) {
            sb.append("segmenterLocation: ").append(segmenterLocation).append("\r\n");
            sb.append("segmenterParams: ").append(segmenterParams).append("\r\n");
        } else {
            sb.append("segmenterLocation not set - live conversion is not available\r\n");
        }

        sb.append("rootDirs:\r\n");
        for (Map.Entry<String, String> entry: rootDirs.entrySet()) {
            sb.append("\"").append(entry.getValue()).append("\"").append(" with label: ").append(entry.getKey()).append("\r\n");
        }

        return sb.toString();
    }
}
