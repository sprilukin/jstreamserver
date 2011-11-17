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

import jstreamserver.utils.ffmpeg.FFMpegSegmenter;
import jstreamserver.utils.ffmpeg.FrameMessage;
import jstreamserver.utils.ffmpeg.ProgressListener;
import org.junit.Test;

import java.io.File;

/**
 * Tests for {@link FFMpegSegmenterTest}
 *
 * @author Sergey Prilukin
 */
public class FFMpegSegmenterTest {

    @Test
    public void testFFMpegConversion() throws Exception {

        File fil = new File("stream");
        fil.mkdirs();

        final FFMpegSegmenter ffMpegSegmenter = new FFMpegSegmenter();

        ProgressListener progressListener = new ProgressListener() {
            int frameCount = 0;

            @Override
            public void onFrameMessage(FrameMessage frameMessage) {
                System.out.println(frameMessage.toString());

                frameCount++;

                if (frameCount > 10) {
                    ffMpegSegmenter.destroy();
                }
            }

            @Override
            public void onProgress(String progressString) {
                System.out.println(Thread.currentThread().getName() + " " + progressString);
            }

            @Override
            public void onFinish(int exitCode) {
                System.out.println("FFMpeg process finished. Exit code: " + exitCode);
            }
        };

        ffMpegSegmenter.start("d:\\work\\projects\\my\\java\\jstreamserver\\ffmpeg.exe", "d:\\install\\IPad\\segmenter\\segmenter.exe",
                "-i d:\\movies\\drakon.avi -f mpegts -acodec libmp3lame -ab 64000 -s 480x320 -vcodec libx264 -b 480000 -flags +loop -cmp +chroma -partitions +parti4x4+partp8x8+partb8x8 -subq 5 -trellis 1 -refs 1 -coder 0 -me_range 16  -keyint_min 25 -sc_threshold 40 -i_qfactor 0.71 -bt 400k -maxrate 524288 -bufsize 524288 -rc_eq 'blurCplx^(1-qComp)' -qcomp 0.6 -qmin 10 -qmax 51 -qdiff 4 -level 30 -aspect 480:320 -g 30 -async 2 -",
                "- 10 stream/stream stream/stream.m3u8 http://localhost:8888/d/work/projects/my/java/jstreamserver-livestreaming-branch/stream 10 5", progressListener);

        ffMpegSegmenter.waitFor();
    }
}
