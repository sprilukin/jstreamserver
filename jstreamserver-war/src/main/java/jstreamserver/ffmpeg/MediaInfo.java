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

package jstreamserver.ffmpeg;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO with info about media streams
 */
public class MediaInfo {
    public static final String INPUT_PATTERN = "Input[\\s]+#[\\d]+,[\\s][\\w\\d,]+,[\\s]+from[\\s]+'[^']+':";
    public static final String DURATION_PATTERN = "[\\s]+Duration:[\\s]+([\\d]+:[\\d]+:[\\d]+\\.[\\d]+),[\\s]+start:[\\s]+([\\d]+(\\.[\\d]+)?),[\\s]+bitrate:[\\s]+([\\d]+[\\s]+[\\w/]+)";

    private String bitrate;
    private String duration;
    private List<AudioStreamInfo> audioStreams = new ArrayList<AudioStreamInfo>();
    private List<VideoStreamInfo> videoStreams = new ArrayList<VideoStreamInfo>();
    private Map<String, String> metadata = new LinkedHashMap<String, String>();

    public String getBitrate() {
        return bitrate;
    }

    public void setBitrate(String bitrate) {
        this.bitrate = bitrate;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public List<AudioStreamInfo> getAudioStreams() {
        return audioStreams;
    }

    public void setAudioStreams(List<AudioStreamInfo> audioStreams) {
        this.audioStreams = audioStreams;
    }

    public List<VideoStreamInfo> getVideoStreams() {
        return videoStreams;
    }

    public void setVideoStreams(List<VideoStreamInfo> videoStreams) {
        this.videoStreams = videoStreams;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
