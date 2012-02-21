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
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.inject.annotation.InjectInto;
import org.unitils.inject.annotation.TestedObject;
import org.unitils.mock.Mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link jstreamserver.ffmpeg.FFMpegSegmenterTest}
 *
 * @author Sergey Prilukin
 */
public class FFMpegSegmenterTest extends UnitilsJUnit4 {

    @TestedObject
    private FFMpegSegmenter ffMpegSegmenter;

    @InjectInto(property = "ffmpegExecutor")
    private Mock<RuntimeExecutor> ffmpegExecutor;

    @InjectInto(property = "segmenterExecutor")
    private Mock<RuntimeExecutor> segmenterExecutor;

    private String execResult = "ffmpeg version N-34549-g13b7781, Copyright (c) 2000-2011 the FFmpeg developers\n" +
            "  built on Nov  6 2011 22:02:08 with gcc 4.6.1\n" +
            "  configuration: --enable-gpl --enable-version3 --disable-w32threads --enable-runtime-cpudetect --enable-avisynth --enab\n" +
            "le-bzlib --enable-frei0r --enable-libopencore-amrnb --enable-libopencore-amrwb --enable-libfreetype --enable-libgsm --en\n" +
            "able-libmp3lame --enable-libopenjpeg --enable-librtmp --enable-libschroedinger --enable-libspeex --enable-libtheora --en\n" +
            "able-libvo-aacenc --enable-libvo-amrwbenc --enable-libvorbis --enable-libvpx --enable-libx264 --enable-libxavs --enable-\n" +
            "libxvid --enable-zlib\n" +
            "  libavutil    51. 24. 0 / 51. 24. 0\n" +
            "  libavcodec   53. 28. 0 / 53. 28. 0\n" +
            "  libavformat  53. 19. 0 / 53. 19. 0\n" +
            "  libavdevice  53.  4. 0 / 53.  4. 0\n" +
            "  libavfilter   2. 47. 0 /  2. 47. 0\n" +
            "  libswscale    2.  1. 0 /  2.  1. 0\n" +
            "  libpostproc  51.  2. 0 / 51.  2. 0\n" +
            "[avi @ 03AD1800] parser not found for codec pcm_s16le, packets or times may be invalid.\n" +
            "Input #0, avi, from 'd:\\movies\\A&M_2.avi':\n" +
            "  Duration: 00:03:20.86, start: 0.000000, bitrate: 1521 kb/s\n" +
            "    Stream #0:0: Video: mpeg4 (Simple Profile) (3IV2 / 0x32564933), yuv420p, 720x480 [SAR 1:1 DAR 3:2], 25 tbr, 29.97 tb\n" +
            "n, 25 tbc\n" +
            "    Stream #0:1: Audio: pcm_s16le ([1][0][0][0] / 0x0001), 44100 Hz, 2 channels, s16, 1411 kb/s\n" +
            "Please use -b:a or -b:v, -b is ambiguous\n" +
            "[buffer @ 03B12780] w:720 h:480 pixfmt:yuv420p tb:1/1000000 sar:1/1 sws_param:\n" +
            "[scale @ 03B0D1A0] w:720 h:480 fmt:yuv420p -> w:480 h:320 fmt:yuv420p flags:0x4\n" +
            "[libx264 @ 03B0CC20] using SAR=1/1\n" +
            "[libx264 @ 03B0CC20] using cpu capabilities: MMX2 SSE2Fast SSSE3 FastShuffle SSE4.1 Cache64\n" +
            "[libx264 @ 03B0CC20] profile High, level 3.0\n" +
            "[mpegts @ 01BCFA40] muxrate VBR, pcr every 2 pkts, sdt every 200, pat/pmt every 40 pkts\n" +
            "Output #0, mpegts, to 'pipe:':\n" +
            "  Metadata:\n" +
            "    encoder         : Lavf53.19.0\n" +
            "    Stream #0:0: Video: h264, yuv420p, 480x320 [SAR 1:1 DAR 3:2], q=10-51, 480 kb/s, 90k tbn, 25 tbc\n" +
            "    Stream #0:1: Audio: mp3, 44100 Hz, 2 channels, s16, 64 kb/s\n" +
            "Stream mapping:\n" +
            "  Stream #0:0 -> #0:0 (mpeg4 -> libx264)\n" +
            "  Stream #0:1 -> #0:1 (pcm_s16le -> libmp3lame)\n" +
            "Press [q] to stop, [?] for help\n" +
            "[mpegts @ 00037210]max_analyze_duration reached\n" +
            "[mpegts @ 00037210]Estimating duration from bitrate, this may be inaccurate\n" +
            "Output #0, mpegts, to 'livestream/stream':\n" +
            "    Stream #0.0: Video: 0x001b, yuv420p, 480x320, q=2-31, 90k tbn, 25 tbc\n" +
            "    Stream #0.1: Audio: 0x0003, 44100 Hz, 2 channels, 64 kb/s\n" +
            "[mpegts @ 00039fe0]muxrate 1 bps, pcr every 5 pkts, sdt every 200, pat/pmt every 40 pkts\n" +
            "[mpegts @ 00039fe0]st:1 error, non monotone timestamps 126000 >= 126000\n" +
            "Warning: Could not write frame of stream\n" +
            "frame= 1047 fps=178 q=22.0 size=    1534kB time=00:00:40.32 bitrate= 311.7kbits/s dup=896 drop=15\n" +
            "frame= 2713 fps= 91 q=28.0 size=    8334kB time=00:01:51.52 bitrate= 612.1kbits/s dup=1 drop=0\n" +
            "frame= 2723 fps= 90 q=24.0 size=    8335kB time=00:01:52.52 bitrate= 612.0kbits/s dup=1 drop=0\n" +
            "frame= 2733 fps= 88 q=26.0 size=    8336kB time=00:01:53.52 bitrate= 612.1kbits/s dup=1 drop=0\n" +
            "frame= 2743 fps= 9 q=28.0 size=    8337kB time=00:01:54.52 bitrate= 612.1kbits/s dup=1 drop=0\n" +
            "frame= 2753 fps= 10 q=28.0 size=    8338kB time=00:01:55.52 bitrate= 612.1kbits/s dup=1 drop=0\n" +
            "frame= 2763 fps= 100 q=28.0 size=    8339kB time=00:01:56.52 bitrate= 612.1kbits/s dup=1 drop=0\n" +
            "frame= 2773 fps= 91 q=28.0 size=    8340kB time=00:01:57.52 bitrate= 612.0kbits/s dup=1 drop=0\n" +
            "frame= 2783 fps= 91 q=28.0 size=    8341kB time=00:01:58.52 bitrate= 612.1kbits/s dup=1 drop=0\n" +
            "frame= 2793 fps= 91 q=28.0 size=    8343kB time=00:01:59.52 bitrate= 612.1kbits/s dup=1 drop=0\n" +
            "frame= 2803 fps= 91 q=28.0 size=    8346kB time=00:02:00.52 bitrate= 612.1kbits/s dup=1 drop=0\n" +
            "frame= 2813 fps= 91 q=28.0 size=    8347kB time=00:02:01.52 bitrate= 612.1kbits/s dup=1 drop=0\n" +
            "frame= 2823 fps= 91 q=28.0 size=    8348kB time=00:02:02.52 bitrate= 612.1kbits/s dup=1 drop=0\n" +
            "frame= 2833 fps= 91 q=28.0 size=    8349kB time=00:02:03.52 bitrate= 612.1kbits/s dup=1 drop=0\n" +
            "frame= 2843 fps= 91 q=28.0 size=    8350kB time=00:02:04.52 bitrate= 612.1kbits/s dup=1 drop=0\n" +
            "frame= 2853 fps= 91 q=28.0 size=    8351kB time=00:02:05.52 bitrate= 612.1kbits/s dup=1 drop=0\n";

    private final AtomicInteger progressCounter = new AtomicInteger(0);
    private final AtomicInteger frameCounter = new AtomicInteger(0);
    private final AtomicInteger finishCounter = new AtomicInteger(0);

    private ProgressListener progressListener = new ProgressListener() {

        @Override
        public void onFrameMessage(FrameMessage frameMessage) {
            System.out.println(frameMessage.toString());

            frameCounter.incrementAndGet();
        }

        @Override
        public void onProgress(String progressString) {
            System.out.println(Thread.currentThread().getName() + " " + progressString);

            progressCounter.incrementAndGet();
        }

        @Override
        public void onFinish(int exitCode) {
            System.out.println("FFMpeg process finished. Exit code: " + exitCode);

            finishCounter.incrementAndGet();
        }

        @Override
        public void onPlayListCreated() {
            //do nothing
        }
    };

    @Test
    public void testFFMpegConversion() throws Exception {
        ffmpegExecutor.returns(new ByteArrayInputStream(execResult.getBytes())).getErrorStream();
        ffmpegExecutor.returns(new ByteArrayInputStream("test".getBytes())).getInputStream();
        segmenterExecutor.returns(new ByteArrayInputStream(new byte[0])).getInputStream();
        segmenterExecutor.returns(new ByteArrayInputStream(new byte[0])).getErrorStream();
        segmenterExecutor.returns(new ByteArrayInputStream(new byte[0])).getErrorStream();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        segmenterExecutor.returns(baos).getOutputStream();


        ffMpegSegmenter.start("d:\\work\\projects\\my\\java\\jstreamserver\\ffmpeg.exe", "d:\\install\\IPad\\segmenter\\segmenter.exe",
                "-i \"d:\\movies\\drakon.avi\" -f mpegts -acodec libmp3lame -ab 64000 -s 480x320 -vcodec libx264 -b 480000 -flags +loop -cmp +chroma -partitions +parti4x4+partp8x8+partb8x8 -subq 5 -trellis 1 -refs 1 -coder 0 -me_range 16  -keyint_min 25 -sc_threshold 40 -i_qfactor 0.71 -bt 400k -maxrate 524288 -bufsize 524288 -rc_eq 'blurCplx^(1-qComp)' -qcomp 0.6 -qmin 10 -qmax 51 -qdiff 4 -level 30 -aspect 480:320 -g 30 -async 2 -",
                "- 10 stream/stream stream/stream.m3u8 stream 10 5", progressListener);

        ffMpegSegmenter.waitFor();

        assertEquals(16, frameCounter.get());
        assertEquals(44, progressCounter.get());
        assertEquals(1, finishCounter.get());

        ffmpegExecutor.assertInvoked().getInputStream();
        ffmpegExecutor.assertInvoked().getErrorStream();
        segmenterExecutor.assertInvoked().getInputStream();
        segmenterExecutor.assertInvoked().getErrorStream();
        segmenterExecutor.assertInvoked().getOutputStream();
    }
}
