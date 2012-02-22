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

import jstreamserver.ffmpeg.FFMpegConstants;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.*;

/**
 * POJO which holds config settings for the server.
 *
 * @author Sergey Prilukin
 */
@Component
public final class Config implements ConfigReader, ConfigWriter {

    private int port;
    private String host;
    private Map<String, String> rootDirs = new LinkedHashMap<String, String>();
    private String ffmpegLocation;
    private String ffmpegParams;
    private String segmenterLocation;
    private int segmentDurationInSec;
    private int segmentWindowSize;
    private int segmenterSearchKillFile;
    private int segmenterMaxtimeout;
    private String defaultTextCharset;
    private int ftpPort;
    public Map<String, List<String>> videoTypesForHTML5 = new LinkedHashMap<String, List<String>>();
    private int maxLiveStreams;

    public Config() {
        videoTypesForHTML5.put("ios", Arrays.asList("qt", "mov", "mp4", "m4v", "3gp", "3gpp"));
        videoTypesForHTML5.put("default", Arrays.asList("mp4", "m4v", "3gp", "3gpp"));
    }

    @Override
    public void setFromProoperties(Properties props) {
        setPort(Integer.valueOf(props.getProperty("port", "8888")));
        setHost(props.getProperty("host", "0.0.0.0"));
        setFfmpegLocation(props.getProperty("ffmpegLocation"));
        setFfmpegParams(props.getProperty("ffmpegParams", "-f mpegts -acodec libmp3lame -ab 64000 -ac 2 -s 480x320 -vcodec libx264 -b 480000 -flags +loop -cmp +chroma -partitions +parti4x4+partp8x8+partb8x8 -subq 5 -trellis 1 -refs 1 -coder 0 -me_range 16  -keyint_min 25 -sc_threshold 40 -i_qfactor 0.71 -bt 400k -maxrate 524288 -bufsize 524288 -rc_eq 'blurCplx^(1-qComp)' -qcomp 0.6 -qmin 10 -qmax 51 -qdiff 4 -level 30 -aspect 480:320 -g 30 -async 2"));
        setSegmenterLocation(props.getProperty("segmenterLocation"));
        setSegmentDurationInSec(Integer.valueOf(props.getProperty("segmentDurationInSec", "10")));
        setSegmentWindowSize(Integer.valueOf(props.getProperty("segmentWindowSize", "720")));
        setSegmenterSearchKillFile(Integer.valueOf(props.getProperty("segmenterSearchKillFile", "1")));
        setSegmenterMaxtimeout(Integer.valueOf(props.getProperty("segmenterMaxtimeout", "30000")));
        setDefaultTextCharset(props.getProperty("defaultTextCharset", "UTF-8"));
        setFtpPort(Integer.valueOf(props.getProperty("ftpPort", "21")));
        setMaxLiveStreams(Integer.valueOf(props.getProperty("maxLiveStreams", "5")));

        for (Map.Entry entry: props.entrySet()) {
            String key = (String)entry.getKey();
            String value = (String)entry.getValue();

            if (key.startsWith("rootdir.")) {
                rootDirs.put(key.substring(8), value);
            } else if (key.startsWith("html5VideoTypesFor.")) {
                videoTypesForHTML5.put(key.substring(19), Arrays.asList(value.split("[,\\s]+")));
            }
        }
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

    public Map<String, String> getRootDirs() {
        return rootDirs;
    }

    public void setRootDirs(Map<String, String> rootDirs) {
        this.rootDirs.clear();
        this.rootDirs.putAll(rootDirs);
    }

    public String getFfmpegLocation() {
        return ffmpegLocation;
    }

    public void setFfmpegLocation(String ffmpegLocation) {
        this.ffmpegLocation = ffmpegLocation;
    }

    public String getFfmpegParams() {
        return MessageFormat.format(FFMpegConstants.FFMPEG_PARAMS_FORMAT, ffmpegParams);
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

    public int getSegmenterMaxtimeout() {
        return segmenterMaxtimeout;
    }

    public void setSegmenterMaxtimeout(int segmenterMaxtimeout) {
        this.segmenterMaxtimeout = segmenterMaxtimeout;
    }

    public String getDefaultTextCharset() {
        return defaultTextCharset;
    }

    public void setDefaultTextCharset(String defaultTextCharset) {
        this.defaultTextCharset = defaultTextCharset;
    }

    public int getFtpPort() {
        return ftpPort;
    }

    public void setFtpPort(int ftpPort) {
        this.ftpPort = ftpPort;
    }

    @Override
    public Map<String, List<String>> getVideoTypesForHTML5() {
        return this.videoTypesForHTML5;
    }

    @Override
    public void setVideoTypesForHTML5(Map<String, List<String>> videoTypesForHTML5) {
        this.videoTypesForHTML5.clear();
        this.videoTypesForHTML5.putAll(videoTypesForHTML5);
    }

    public String getSegmenterParams() {
        return MessageFormat.format(
                FFMpegConstants.SEGMENTER_PARAMS_FORMAT,
                String.valueOf(segmentDurationInSec),
                String.valueOf(segmentWindowSize),
                String.valueOf(segmenterSearchKillFile));
    }

    public int getMaxLiveStreams() {
        return maxLiveStreams;
    }

    public void setMaxLiveStreams(int maxLiveStreams) {
        this.maxLiveStreams = maxLiveStreams;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Jstreamserver config:\r\n");
        sb.append("listening on: ").append(host).append(":").append(port).append("\r\n");
        sb.append("default text charset: ").append(defaultTextCharset).append("\r\n");
        sb.append("Built-in FTP server listen on: ").append(ftpPort).append("\r\n");
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
