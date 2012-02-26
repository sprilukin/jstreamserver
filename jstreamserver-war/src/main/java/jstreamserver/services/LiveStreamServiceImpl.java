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

import jstreamserver.ffmpeg.FrameMessage;
import jstreamserver.ffmpeg.ProgressListener;
import jstreamserver.utils.ConfigReader;
import jstreamserver.utils.LiveStreamer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service which allows to start or stop livestreaming for specified file
 *
 * @author Sergey Prilukin
 */

@Service
public class LiveStreamServiceImpl implements LiveStreamService {
    private static final Log log = LogFactory.getLog(LiveStreamServiceImpl.class);
    private final Map<Integer, LiveStreamer> liveStreams = new LinkedHashMap<Integer, LiveStreamer>();
    private final Map<String, Integer> sessionsMap = new LinkedHashMap<String, Integer>();

    @Autowired
    private ConfigReader configReader;

    public LiveStreamServiceImpl() {
    }

    @Override
    public Integer createLiveStream(File file, String startTime, Integer audioStreamId, String contextPath, String sessionId) throws IOException {
        synchronized (liveStreams) {

            //Find appropriate liveStreamId.
            //Use user's session if possible
            Integer liveStreamId = sessionsMap.get(sessionId);
            if (liveStreamId == null) {
                liveStreamId = liveStreams.size();
                if (liveStreamId >= configReader.getMaxLiveStreams()) {
                    liveStreamId--;
                }
            }

            //Remove sessionId by given liveStreamId if present
            removeSessionMapEntryByValue(liveStreamId);

            //Destroy liveStream by given liveStreamId if present
            LiveStreamer liveStreamer = liveStreams.remove(liveStreamId);
            if (liveStreamer != null) {
                liveStreamer.destroyLiveStream();
            }

            //Start new liveStream with given liveStreamId
            sessionsMap.put(sessionId, liveStreamId);
            liveStreamer = addLiveStreamer(liveStreamId, contextPath);
            liveStreamer.startLiveStream(file, startTime, audioStreamId);

            return liveStreamId;
        }
    }

    private void removeSessionMapEntryByValue(Integer liveStreamId) {
        final Iterator<Map.Entry<String, Integer>> iterator = sessionsMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            if (entry.getValue().equals(liveStreamId)) {
                iterator.remove();
                break;
            }
        }
    }

    private LiveStreamer addLiveStreamer(Integer id, String contextPath) {
        DeadStreamsCleaner deadStreamsCleaner = new DeadStreamsCleaner();
        LiveStreamer liveStreamer = new LiveStreamer(contextPath, id.toString(), deadStreamsCleaner, configReader);
        deadStreamsCleaner.setLiveStreamer(liveStreamer, id);

        liveStreams.put(id, liveStreamer);
        return liveStreamer;
    }

    @Override
    public void destroyLiveStream(Integer liveStreamId) throws IOException {
        synchronized (liveStreams) {
            LiveStreamer liveStreamer = liveStreams.remove(liveStreamId);
            liveStreamer.destroyLiveStream();
        }
    }

    @Override
    public File getTSFile(String path, Integer liveStreamId) throws IOException {
        //synchronized (liveStreams) { //TOO slow with sync
            return liveStreams.get(liveStreamId).getTSFile(path);
        //}
    }

    @Override
    public InputStream getPlayList(Integer liveStreamId) throws IOException {
        //synchronized (liveStreams) {
            return liveStreams.get(liveStreamId).getPlayList();
        //}
    }
   
    class DeadStreamsCleaner implements ProgressListener {
        private LiveStreamer liveStreamer;
        private Integer id;

        DeadStreamsCleaner() {
        }

        public void setLiveStreamer(LiveStreamer liveStreamer, Integer id) {
            this.liveStreamer = liveStreamer;
            this.id = id;
        }

        @Override
        public void onFrameMessage(FrameMessage frameMessage) {
            //log.debug(frameMessage);
        }

        @Override
        public void onProgress(String progressString) {
            //log.debug(progressString);
        }

        @Override
        public void onFinish(int exitCode) {
            //Do not remove dead live stream from map - this allows
            //to read static resources of this live stream until new live
            //stream will evict this one.
            /*synchronized (liveStreams) {
                LiveStreamer ls = liveStreams.get(id);
                if (ls == liveStreamer) {
                    liveStreams.remove(id);
                }

                //to avoid circular links
                liveStreamer = null;
            }

            log.debug(String.format("Dead liveStream [%s] has been removed", id));*/
        }

        @Override
        public void onPlayListCreated() {
            /* do nothing */
        }
    }
}
