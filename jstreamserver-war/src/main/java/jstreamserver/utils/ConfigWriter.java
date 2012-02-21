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

import java.util.Map;
import java.util.Properties;

/**
 * Allows to write config
 *
 * @author Sergey Prilukin
 */
public interface ConfigWriter {

    public void setFromProoperties(Properties props);

    public void setPort(int port);

    public void setHost(String host);

    public void setRootDirs(Map<String, String> rootDirs);

    public void setFfmpegLocation(String ffmpegLocation);

    public void setFfmpegParams(String ffmpegParams);

    public void setSegmenterLocation(String segmenterLocation);

    public void setSegmentDurationInSec(int segmentDurationInSec);

    public void setSegmentWindowSize(int segmentWindowSize);

    public void setSegmenterSearchKillFile(int segmenterSearchKillFile);

    public void setSegmenterMaxtimeout(int segmenterMaxtimeout);

    public void setDefaultTextCharset(String defaultTextCharset);

    public void setFtpPort(int ftpPort);
}
