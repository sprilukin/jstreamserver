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

import jstreamserver.utils.RuntimeExecutor;
import jstreamserver.utils.ffmpeg.FFMpegInformer;
import jstreamserver.utils.ffmpeg.MediaInfo;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.inject.annotation.InjectInto;
import org.unitils.inject.annotation.TestedObject;
import org.unitils.mock.Mock;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link jstreamserver.utils.ffmpeg.FFMpegInformer}
 *
 * @author Sergey Prilukin
 */
public class FFmpegInformerTest extends UnitilsJUnit4 {

    @TestedObject
    private FFMpegInformer ffMpegInformer;

    @InjectInto(property = "ffmpegExecutor")
    private Mock<RuntimeExecutor> runtimeExecutorMock;

    private String ffmpegExecutionResults = "ffmpeg version N-34549-g13b7781, Copyright (c) 2000-2011 the FFmpeg developers\n" +
            "  built on Nov  6 2011 22:02:08 with gcc 4.6.1\n" +
            "  configuration: --enable-gpl --enable-version3 --disable-w32threads --enable-runtime-cpudetect --enable-avisynth --enable-bzlib --enable-frei0r --enable-libopencore-amrnb --enable-libopencore-amrwb --enable-libfreetype --enable-libgsm --enable-libmp3lame --enable-libopenjpeg --enable-librtmp --enable-libschroedinger --enable-libspeex --enable-libtheora --enable-libvo-aacenc --enable-libvo-amrwbenc --enable-libvorbis --enable-libvpx --enable-libx264 --enable-libxavs --enable-libxvid --enable-zlib\n" +
            "  libavutil    51. 24. 0 / 51. 24. 0\n" +
            "  libavcodec   53. 28. 0 / 53. 28. 0\n" +
            "  libavformat  53. 19. 0 / 53. 19. 0\n" +
            "  libavdevice  53.  4. 0 / 53.  4. 0\n" +
            "  libavfilter   2. 47. 0 /  2. 47. 0\n" +
            "  libswscale    2.  1. 0 /  2.  1. 0\n" +
            "  libpostproc  51.  2. 0 / 51.  2. 0\n" +
            "[mpeg4 @ 003692E0] Invalid and inefficient vfw-avi packed B frames detected\n" +
            "Input #0, avi, from '90210.s04e10.avi':\n" +
            "  Metadata:\n" +
            "    encoder         : MEncoder SVN-r32669-4.3.2\n" +
            "  Duration: 00:41:30.86, start: 0.000000, bitrate: 1178 kb/s\n" +
            "    Stream #0:0: Video: mpeg4 (Advanced Simple Profile) (XVID / 0x44495658), yuv420p, 624x352 [SAR 1:1 DAR 39:22], 23.98 tbr, 23.98 tbn, 23.98 tbc\n" +
            "    Stream #0:1: Audio: mp3 (U[0][0][0] / 0x0055), 48000 Hz, stereo, s16, 160 kb/s\n" +
            "Input #1, matroska,webm, from 'Net (1995) AVC.mkv':\n" +
            "  Duration: 01:49:43.00, start: 0.000000, bitrate: 2769 kb/s\n" +
            "    Stream #1:0(eng): Video: h264 (Constrained Baseline), yuv420p, 720x560, SAR 199:142 DAR 1791:994, 25 fps, 25 tbr, 1k tbn, 50 tbc (default)\n" +
            "    Stream #1:1(rus): Audio: aac, 48000 Hz, 5.1, s16 (default)\n" +
            "    Metadata:\n" +
            "      title           : ╨Ф╤Г╨▒╨╗╤П╨╢\n" +
            "    Stream #1:2(rus): Audio: aac, 48000 Hz, stereo, s16\n" +
            "    Metadata:\n" +
            "      title           : ╨Ц╨╕╨▓╨╛╨▓\n" +
            "    Stream #1:3(rus): Audio: aac, 48000 Hz, 5.1, s16\n" +
            "    Metadata:\n" +
            "      title           : ╨Ъ╨░╤А╤Ж╨╡╨▓\n" +
            "    Stream #1:4(eng): Audio: aac, 48000 Hz, 5.1, s16\n" +
            "    Metadata:\n" +
            "      title           : ╨Р╨╜╨│╨╗╨╕╨╣╤Б╨║╨╕╨╣\n" +
            "    Stream #1:5(rus): Subtitle: text\n" +
            "Input #2, matroska,webm, from 'Global.Survival.Guide.720p.mkv':\n" +
            "  Duration: 00:43:24.20, start: 0.000000, bitrate: 3603 kb/s\n" +
            "    Stream #2:0(eng): Video: h264 (High), yuv420p, 1280x720, SAR 1:1 DAR 16:9, 25 fps, 25 tbr, 1k tbn, 50 tbc\n" +
            "    Stream #2:1: Audio: ac3, 48000 Hz, 5.1(side), s16, 384 kb/s (default)\n" +
            "At least one output file must be specified";

    @Test
    public void testFFMpegInformer() throws Exception {
        runtimeExecutorMock.returns(new ByteArrayInputStream(ffmpegExecutionResults.getBytes())).getInputStream();
        runtimeExecutorMock.returns(new ByteArrayInputStream(new byte[0])).getErrorStream();
        runtimeExecutorMock.returns(0).getExitCode();

        List<String> files = new ArrayList<String>();
        files.add("90210.s04e10.avi");
        files.add("Net (1995) AVC.mkv");
        files.add("Global.Survival.Guide.720p.mkv");

        List<MediaInfo> result = ffMpegInformer.getInfo(files, "test/ffmpeg");

        runtimeExecutorMock.assertInvoked().execute("test/ffmpeg", null);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(4, result.get(1).getAudioStreams().size());
        assertTrue(result.get(1).getAudioStreams().get(0).getDefaultStream());
    }
}
