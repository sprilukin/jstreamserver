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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;

/**
 * POJO which holds config settings for the server.
 *
 * @author Sergey Prilukin
 */
@Component
public final class Config implements ConfigReader, InitializingBean {

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
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String pathToProperties = System.getProperty("jsproperties");

        if (pathToProperties != null) {
            Reader reader = new BufferedReader(new FileReader(pathToProperties));
            Properties properties = new Properties();
            properties.load(reader);
            setFromProperties(properties);
        }

        setFromProperties(System.getProperties());
    }

    @Value(value = "classpath:jstreamserver.properties")
    public void setResource(Resource resource) throws IOException {
        Properties properties = new Properties();
        properties.load(resource.getInputStream());
        setFromProperties(properties);
    }

    private Integer getIntValueFromProperties(Properties props, String key, Integer defValue) {
        String value = props.getProperty(key);
        return value != null ? Integer.parseInt(value) : defValue;
    }

    private void setFromProperties(Properties props) {
        setPort(getIntValueFromProperties(props, "port", port));
        setHost(props.getProperty("host", host));
        setFfmpegLocation(props.getProperty("ffmpegLocation", ffmpegLocation));
        setFfmpegParams(props.getProperty("ffmpegParams", ffmpegParams));
        setSegmenterLocation(props.getProperty("segmenterLocation", segmenterLocation));
        setSegmentDurationInSec(getIntValueFromProperties(props, "segmentDurationInSec", segmentDurationInSec));
        setSegmentWindowSize(getIntValueFromProperties(props, "segmentWindowSize", segmentWindowSize));
        setSegmenterSearchKillFile(getIntValueFromProperties(props, "segmenterSearchKillFile", segmenterSearchKillFile));
        setSegmenterMaxtimeout(getIntValueFromProperties(props, "segmenterMaxtimeout", segmenterMaxtimeout));
        setDefaultTextCharset(props.getProperty("defaultTextCharset", defaultTextCharset));
        setFtpPort(getIntValueFromProperties(props, "ftpPort", ftpPort));
        setMaxLiveStreams(getIntValueFromProperties(props, "maxLiveStreams", maxLiveStreams));

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

    public String getHost() {
        return host;
    }

    public Map<String, String> getRootDirs() {
        return rootDirs;
    }

    public String getFfmpegLocation() {
        return ffmpegLocation;
    }

    public String getFfmpegParams() {
        return MessageFormat.format(FFMpegConstants.FFMPEG_PARAMS_FORMAT, ffmpegParams);
    }

    public String getSegmenterLocation() {
        return segmenterLocation;
    }

    public int getSegmentDurationInSec() {
        return segmentDurationInSec;
    }

    public int getSegmentWindowSize() {
        return segmentWindowSize;
    }

    public int getSegmenterSearchKillFile() {
        return segmenterSearchKillFile;
    }

    public int getSegmenterMaxtimeout() {
        return segmenterMaxtimeout;
    }

    public String getDefaultTextCharset() {
        return defaultTextCharset;
    }

    public int getFtpPort() {
        return ftpPort;
    }

    @Override
    public Map<String, List<String>> getVideoTypesForHTML5() {
        return this.videoTypesForHTML5;
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


    public void setPort(Integer port) {
        if (port != null) {
            this.port = port;
        }        
    }

    public void setHost(String host) {
        if (host != null) {
            this.host = host;
        }
    }

    public void setRootDirs(Map<String, String> rootDirs) {
        if (rootDirs != null) {
            this.rootDirs.clear();
            this.rootDirs.putAll(rootDirs);
        }
    }

    public void setFfmpegLocation(String ffmpegLocation) {
        if (ffmpegLocation != null) {
            this.ffmpegLocation = ffmpegLocation
                    .replaceFirst("\\$\\{work\\.dir\\}", System.getProperty("user.dir").replaceAll("\\\\", "/"));
        }
    }

    public void setFfmpegParams(String ffmpegParams) {
        if (ffmpegParams != null) {
            this.ffmpegParams = ffmpegParams;
        }
    }

    public void setSegmenterLocation(String segmenterLocation) {
        if (segmenterLocation != null) {
            this.segmenterLocation = segmenterLocation
                    .replaceFirst("\\$\\{work\\.dir\\}", System.getProperty("user.dir").replaceAll("\\\\", "/"));
        }
    }

    public void setSegmentDurationInSec(Integer segmentDurationInSec) {
        if (segmentDurationInSec != null) {
            this.segmentDurationInSec = segmentDurationInSec;
        }
    }

    public void setSegmentWindowSize(Integer segmentWindowSize) {
        if (segmentWindowSize != null) {
            this.segmentWindowSize = segmentWindowSize;
        }
    }

    public void setSegmenterSearchKillFile(Integer segmenterSearchKillFile) {
        if (segmenterSearchKillFile != null) {
            this.segmenterSearchKillFile = segmenterSearchKillFile;
        }
    }

    public void setSegmenterMaxtimeout(Integer segmenterMaxtimeout) {
        if (segmenterMaxtimeout != null) {
            this.segmenterMaxtimeout = segmenterMaxtimeout;
        }
    }

    public void setDefaultTextCharset(String defaultTextCharset) {
        if (defaultTextCharset != null) {
            this.defaultTextCharset = defaultTextCharset;
        }
    }

    public void setFtpPort(Integer ftpPort) {
        if (ftpPort != null) {
            this.ftpPort = ftpPort;
        }
    }

    public void setVideoTypesForHTML5(Map<String, List<String>> videoTypesForHTML5) {
        if (videoTypesForHTML5 != null) {
            this.videoTypesForHTML5.clear();
            this.videoTypesForHTML5.putAll(videoTypesForHTML5);
        }
    }

    public void setMaxLiveStreams(Integer maxLiveStreams) {
        if (maxLiveStreams != null) {
            this.maxLiveStreams = maxLiveStreams;
        }
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
