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

package jstreamserver.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Service to operate with live streaming
 *
 * @author Sergey Prilukin
 */
public interface LiveStreamService {

    /**
     * Create live stream from given file and return stream ID
     * 
     * @param file video file for which live stream should be created
     * @param startTime String in format "HH:mm:ss". If passed then video will be played from that point.                                         
     * @param audioStreamId number of audiostream which should be selected. Default audio stream will be choosen if 
     *                      parameter not passed
     * @param contextPath contextPath of web application to produce correct playlist
     * @return id of created live stream so it can de destroyed in future by calling {@link #destroyLiveStream(Integer)}
     * @throws IOException if exception occurs during stream initialization
     */
    public Integer createLiveStream(File file, String startTime, Integer audioStreamId, String contextPath) throws IOException;
    
    /**
     * Returns {@link InputStream} for .m3u8 playlist of livestream with given ID
     *
     * @param liveStreamId id of the stream
     * @return {@link InputStream} for .m3u8 playlist
     * @throws IOException if exception occurs during getting playlist
     */
    public InputStream getPlayList(Integer liveStreamId) throws IOException;

    /**
     * Destroys livestream by given id
     *
     * @param liveStreamId id of the stream which should e destroyed
     * @throws IOException if exception occurs during destroying
     */
    public void destroyLiveStream(Integer liveStreamId) throws IOException;

    /**
     * Returns TS file for give stream ID
     *
     * @param liveStreamId id of the stream which should e destroyed
     * @throws IOException if exception occurs during destroying
     */
    public File getTSFile(String path, Integer liveStreamId) throws IOException;
}
