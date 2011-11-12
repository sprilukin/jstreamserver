package jstreamserver.utils.ffmpeg;

import jstreamserver.utils.RuntimeExecutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class which wraps execution of ffmpeg library
 * using {@link RuntimeExecutor}.
 *
 * It can use instance of {@link ProgressListener}
 * to report the progress.
 */
public final class FFMpegWrapper {

    public static final String FRAME_PATTERN = "frame=[\\s]*([\\d]+)[\\s]*fps=[\\s]*([\\d]+)[\\s]*q=([\\d\\.]+)[\\s]*size=[\\s]*([\\d]+)kB[\\s]*time=([\\d]+:[\\d]+:[\\d]+\\.[\\d]+)[\\s]*bitrate=[\\s]*([\\d\\.]+)kbits/s[\\s]*dup=([\\d]+)[\\s]*drop=([\\d]+)";
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss.SS");
    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    final private RuntimeExecutor runtimeExecutor = new RuntimeExecutor();
    private Thread inputReader;
    private Thread errorReader;
    private Thread finishWaiter;

    /**
     * Utility class which reads from passed {@link BufferedReader}
     * And reports progress to passed {@link ProgressListener}
     */
    class InputReader implements Runnable {
        private BufferedReader reader;
        private ProgressListener progressListener;

        public InputReader(BufferedReader reader, ProgressListener progressListener) {
            this.reader = reader;
            this.progressListener = progressListener;
        }

        @Override
        public void run() {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = Pattern.compile(FRAME_PATTERN).matcher(line);
                    if (matcher.find()) {
                        FrameMessage frameMessage = new FrameMessage();
                        frameMessage.setFrameNumber(Long.parseLong(matcher.group(1)));
                        frameMessage.setFps(Integer.parseInt(matcher.group(2)));
                        frameMessage.setQ(new BigDecimal(matcher.group(3)));
                        frameMessage.setSize(Long.parseLong(matcher.group(4)));
                        frameMessage.setTime(DATE_FORMAT.parse(matcher.group(5)).getTime());
                        frameMessage.setBitrate(new BigDecimal(matcher.group(6)));
                        frameMessage.setDup(Long.parseLong(matcher.group(7)));
                        frameMessage.setDrop(Long.parseLong(matcher.group(8)));

                        progressListener.onFrameMessage(frameMessage);
                    } else {
                        progressListener.onProgress(line);
                    }
                }
            } catch (Exception e) {
                /* ignore */
            }
        }
    }

    /**
     * Utility class which wait untill process is finished and
     * then calls {@code onFinish} method of passed {@link ProgressListener}
     */
    class FinishWaiter implements Runnable {
        private ProgressListener progressListener;

        public FinishWaiter(ProgressListener progressListener) {
            this.progressListener = progressListener;
        }

        @Override
        public void run() {
            try {
                runtimeExecutor.waitFor();
                this.progressListener.onFinish(runtimeExecutor.getExitCode());
            } catch (InterruptedException e) {
                this.progressListener.onFinish(runtimeExecutor.getExitCode());
            }
        }
    }

    public FFMpegWrapper(String pathToFFMpegBinary, String[] params, final ProgressListener progressListener) throws IOException {
        runtimeExecutor.execute(pathToFFMpegBinary, params);
        setProgressListener(progressListener);
    }

    public FFMpegWrapper(String pathToFFMpegBinary, String[] params) throws IOException {
        runtimeExecutor.execute(pathToFFMpegBinary, params);
    }

    public void setProgressListener(final ProgressListener progressListener) {
        if (progressListener != null) {
            final BufferedReader input = new BufferedReader(new InputStreamReader(runtimeExecutor.getInputStream()));
            final BufferedReader error = new BufferedReader(new InputStreamReader(runtimeExecutor.getErrorStream()));

            inputReader = (new Thread(new InputReader(input, progressListener)));
            errorReader = (new Thread(new InputReader(error, progressListener)));
            finishWaiter = (new Thread(new FinishWaiter(progressListener)));

            inputReader.start();
            errorReader.start();
            finishWaiter.start();
        }
    }

    public void destroy() {
        runtimeExecutor.destroy();
    }

    public void waitFor() throws InterruptedException {
        runtimeExecutor.waitFor();
    }
}
