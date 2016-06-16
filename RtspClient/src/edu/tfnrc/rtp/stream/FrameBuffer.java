/*
package edu.tfnrc.rtp.stream;

import edu.tfnrc.rtp.util.Buffer;


*/
/**
 * Buffer of one output frame, containing the frame's sequence, RTP packets' sequence range
 * if the frame is iframe.
 * If a FrameBuffer is lost, drop the later FrameBuffers until receiving a iframe.
 *
 * Created by leip on 2016/3/7.
 *//*

public class FrameBuffer extends Buffer {

    private static final int H264_NAL_HEADER_LENGTH = 4;

    private long queueNumber = 0;

    private long startSeqnum = 0;

    private long endSeqnum = Integer.MAX_VALUE;

    private boolean isIFrame = false;

    public FrameBuffer(){
        super();
    }

    public long getQueueNumber() {
        return queueNumber;
    }

    public void setQueueNumber(long queueNumber) {
        this.queueNumber = queueNumber;
    }

    public long getStartSeqnum() {
        return startSeqnum;
    }

    public void setStartSeqnum(long startSeqnum) {
        this.startSeqnum = startSeqnum;
    }

    public long getEndSeqnum() {
        return endSeqnum;
    }

    public void setEndSeqnum(long endSeqnum) {
        this.endSeqnum = endSeqnum;
    }

    public boolean isIFrame() {
        return isIFrame;
    }

    public void setIsIFrame(boolean isIFrame) {
        this.isIFrame = isIFrame;
    }

    //Checking if the frame is iframe
    public boolean checkIFrame() {

    }
}
*/
