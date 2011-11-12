package jstreamserver.ui;

import jstreamserver.utils.ffmpeg.FFMpegWrapper;
import jstreamserver.utils.ffmpeg.FrameMessage;
import jstreamserver.utils.ffmpeg.ProgressListener;
import org.junit.Test;

import java.io.File;

/**
 * Tests for {@link FFMpegWrapperTest}
 *
 * @author Sergey Prilukin
 */
public class FFMpegWrapperTest {

    @Test
    public void testFFMpegConversion() throws Exception {

        File file = new File("d:\\movies\\A&M_2.mov");
        if (file.exists()) {
            file.delete();
        }

        final FFMpegWrapper ffmpeg = new FFMpegWrapper("d:\\movies\\ffmpeg.exe",
                new String[] {"-i", "d:\\movies\\A&M_2.avi", "-sameq", "d:\\movies\\A&M_2.mov"});

        ffmpeg.setProgressListener(new ProgressListener() {
            int frameCount = 0;

            @Override
            public void onFrameMessage(FrameMessage frameMessage) {
                System.out.println(frameMessage.toString());

                frameCount++;

                if (frameCount > 5) {
                    ffmpeg.destroy();
                }
            }

            @Override
            public void onProgress(String progressString) {
                System.out.println(progressString);
            }

            @Override
            public void onFinish(int exitCode) {
                System.out.println("FFMpeg process finished. Exit code: " + exitCode);
            }
        });

        ffmpeg.waitFor();
    }
}
