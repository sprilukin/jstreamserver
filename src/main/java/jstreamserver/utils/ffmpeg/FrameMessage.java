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

package jstreamserver.utils.ffmpeg;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * POJO which incapsulates encoded frame info
 *
 * @author Sergey Prilukin
 */
public final class FrameMessage {
    public static final String FRAME_PATTERN = "frame=[\\s]*([\\d]+)[\\s]*fps=[\\s]*([\\d]+)[\\s]*q=([\\d\\.]+)[\\s]*size=[\\s]*([\\d]+)kB[\\s]*time=([\\d]+:[\\d]+:[\\d]+\\.[\\d]+)[\\s]*bitrate=[\\s]*([\\d\\.]+)kbits/s[\\s]*dup=([\\d]+)[\\s]*drop=([\\d]+)";
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss.SS");
    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

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
        sb.append(", time=").append(DATE_FORMAT.format(new Date(time)));
        sb.append(", bitrate=").append(bitrate);
        sb.append(", dup=").append(dup);
        sb.append(", drop=").append(drop);
        sb.append('}');
        return sb.toString();
    }
}
