package jstreamserver.utils.ffmpeg;

/**
 * Implementations can be used in {@link FFMpegWrapper}
 * to receive progress info from ffmpeg process
 *
 * @author Sergey
 */
public interface ProgressListener {
    public void onFrameMessage(FrameMessage frameMessage);
    public void onProgress(String progressString);
    public void onFinish(int exitCode);
}
