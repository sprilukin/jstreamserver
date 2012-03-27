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

import java.util.List;
import java.util.Map;

/**
 * Allows to read config
 *
 * @author Sergey Prilukin
 */
public interface ConfigReader {

    public int getPort();

    public String getHost();

    public Map<String, String> getRootDirs();

    public String getFfmpegLocation();

    public String getFfmpegParams();

    public String getSegmenterLocation();

    public int getSegmentDurationInSec();

    public int getSegmentWindowSize();

    public int getSegmenterSearchKillFile();

    public int getSegmenterMaxtimeout();

    public String getSegmenterParams();

    public String getDefaultTextCharset();

    public int getFtpPort();
    
    public List<String> getVideoTypesForHTML5(String userAgent);

    public Boolean getSupportsLiveStream(String userAgent);

    public int getMaxLiveStreams();
}
