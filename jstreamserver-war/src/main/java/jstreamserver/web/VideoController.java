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

package jstreamserver.web;

import jstreamserver.dto.VideoSource;
import jstreamserver.dto.VideoTag;
import jstreamserver.services.LiveStreamService;
import jstreamserver.utils.ConfigReader;
import jstreamserver.utils.MimeProperties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller to show video
 *
 * @author Sergey Prilukin
 */

@Controller
public class VideoController {

    public static final String DEFAULT_START_TIME = "00:00:00";

    private static final Log log = LogFactory.getLog(VideoController.class);

    @Autowired
    private LiveStreamService liveStreamService;

    @Autowired
    private ConfigReader configReader;

    @Autowired
    private MimeProperties mimeProperties;

    @Autowired
    private ControllerUtils controllerUtils;

    @RequestMapping("/video")
    public @ResponseBody VideoTag getVideoTag(
            @RequestParam(value = "path", required = false) String path,
            @RequestParam(value = "stream", required = false) Integer stream,
            @RequestParam(value = "time", required = false) String time,
            @RequestHeader(value = "User-Agent", required = false, defaultValue = "default") String userAgent,
            HttpServletRequest request) throws Exception {


        Integer liveStreamId = null;
        File videoFile = controllerUtils.getFile(path);

        Boolean supportsLiveStreaming = configReader.getSupportsLiveStream(userAgent);
        if (supportsLiveStreaming) {
            String extension = FilenameUtils.getExtension(videoFile.getName());
            List<String> supportedVideoTypes = configReader.getVideoTypesForHTML5(userAgent);

            if (!supportedVideoTypes.contains(extension)) {
                liveStreamId = liveStreamService.createLiveStream(
                        videoFile, time, stream, request.getContextPath(), request.getSession(true).getId());
            }
        } else {
            /*
                do nothing for now
                in future use supportedVideoTypes to convert video to supported type
            */
        }

        return getVideoListTag(videoFile, path, time, liveStreamId);
    }

    @RequestMapping("/playlist/{id}")
    public void downloadResource(
            @PathVariable(value = "id") Integer id,
            HttpServletResponse response) throws Exception {

        InputStream is = liveStreamService.getPlayList(id);
        controllerUtils.setCommonResourceHeaders(mimeProperties.getProperty("m3u8"), response);
        controllerUtils.writeStream(is, response);
    }

    @RequestMapping("/livestream/{videoFile:[\\w]+}{liveStreamId:[\\d]+}{suffix:[\\.\\w\\d\\-]+}")
    public void downloadResource(
            @PathVariable("videoFile") String videoFile,
            @PathVariable("liveStreamId") Integer liveStreamId,
            @PathVariable("suffix") String suffix,
            @RequestHeader(value = "Range", required = false) String range,
            HttpServletResponse response) throws Exception {

        File file = liveStreamService.getTSFile(String.format("%s%s%s", videoFile, liveStreamId, suffix), liveStreamId);
        if (file != null && file.exists() && file.isFile()) {
            InputStream is = controllerUtils.getFileAsStream(file, range, response);

            controllerUtils.writeStream(is, response);
        } else {
            response.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
        }
    }

    private VideoTag getVideoListTag(File videoFile, String path, String startTime, Integer liveStreamId) throws IOException {
        VideoTag videoTag = new VideoTag();

        videoTag.setStartTime(startTime != null ? startTime : DEFAULT_START_TIME);

        File subtitles = new File(videoFile.getParentFile(), FilenameUtils.getBaseName(videoFile.getName()) + ".srt");
        if (subtitles.exists() && subtitles.isFile()) {
            videoTag.setSubtitle(FileUtils.readFileToString(subtitles, configReader.getDefaultTextCharset()));
        }

        if (liveStreamId != null) {
            String mimeType = mimeProperties.getProperty("m3u8");
            videoTag.getSources().add(new VideoSource(mimeType, "/playlist/" + liveStreamId));
        }

        String mimeType = mimeProperties.getProperty(FilenameUtils.getExtension(videoFile.getName()), "application/octet-stream");
        videoTag.getSources().add(new VideoSource(mimeType, "/index?path=" + path));

        return videoTag;
    }

}
