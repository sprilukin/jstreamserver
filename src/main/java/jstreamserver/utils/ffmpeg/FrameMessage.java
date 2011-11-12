package jstreamserver.utils.ffmpeg;

import java.math.BigDecimal;
import java.util.Date;

/**
 * POJO which incapsulates encoded frame info
 *
 * @author Sergey Prilukin
 */
public final class FrameMessage {
    private long frameNumber;
    private int fps;
    private BigDecimal q;
    private long size;
    private long time;
    private BigDecimal bitrate;
    private long dup;
    private long drop;

    public long getFrameNumber() {
        return frameNumber;
    }

    public void setFrameNumber(long frameNumber) {
        this.frameNumber = frameNumber;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public BigDecimal getQ() {
        return q;
    }

    public void setQ(BigDecimal q) {
        this.q = q;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public BigDecimal getBitrate() {
        return bitrate;
    }

    public void setBitrate(BigDecimal bitrate) {
        this.bitrate = bitrate;
    }

    public long getDup() {
        return dup;
    }

    public void setDup(long dup) {
        this.dup = dup;
    }

    public long getDrop() {
        return drop;
    }

    public void setDrop(long drop) {
        this.drop = drop;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("FrameMessage");
        sb.append("{frameNumber=").append(frameNumber);
        sb.append(", fps=").append(fps);
        sb.append(", q=").append(q);
        sb.append(", size=").append(size);
        sb.append(", time=").append(FFMpegWrapper.DATE_FORMAT.format(new Date(time)));
        sb.append(", bitrate=").append(bitrate);
        sb.append(", dup=").append(dup);
        sb.append(", drop=").append(drop);
        sb.append('}');
        return sb.toString();
    }
}
