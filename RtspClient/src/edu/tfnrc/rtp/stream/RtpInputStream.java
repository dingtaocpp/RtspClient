package edu.tfnrc.rtp.stream;

import android.util.Log;
import edu.tfnrc.rtp.media.format.Format;
import edu.tfnrc.rtp.packet.RtpPacket;
import edu.tfnrc.rtp.packet.RtpPacketReceiver;
import edu.tfnrc.rtp.util.Buffer;

/**
 * RTP input stream from RtpPacket to Buffer
 * Add RTCP between RTP and Session
 *
 * Created by leip on 2015/12/8.
 */
public class RtpInputStream implements ProcessorInputStream {

    //debug
    private final String TAG = "RtpInputStream";

    private int localPort;

    private RtpPacketReceiver rtpReceiver = null;

    private Buffer buffer = new Buffer();

    private Format inputFormat = null;

    /**
     * Constructor
     *
     * @param localPort Local port
     * @param inputFormat Input format
     */
    public RtpInputStream(int localPort, Format inputFormat){
        this.localPort = localPort;
        this.inputFormat = inputFormat;

        //new RtcpSession
    }

    /**
     * Open the input stream
     *
     * @throws Exception
     */
    public void open() throws Exception {

        // Create the RTP receiver
        rtpReceiver = new RtpPacketReceiver(localPort);
        // Create the RTCP receiver
//        rtcpReceiver = new RtcpPacketReceiver(localPort + 1, rtcpSession);
//        rtcpReceiver.start();
    }

    /**
     * Close the input stream
     */
    public void close() {
        try {
            // Close the RTP receiver
            if (rtpReceiver != null) {
                rtpReceiver.close();
            }
            // Close the RTCP receiver
        } catch (Exception e) {
            Log.e(TAG, "Can't close correctly RTP ressources", e);
        }
    }

    /**
     * Returns the RTP receiver
     *
     * @return RTP receiver
     */
    public RtpPacketReceiver getRtpReceiver() {
        return rtpReceiver;
    }

    /**
     * Read from the input stream without blocking
     *
     * @return Buffer
     * @throws Exception
     */
    public Buffer read() throws Exception{
        //Wait and read a RTP packet
        RtpPacket rtpPacket = rtpReceiver.readRtpPacket();
        if(rtpPacket == null){
            return null;
        }

        //Create a buffer
        buffer.setData(rtpPacket.data);
        buffer.setLength(rtpPacket.payloadLength);
        buffer.setOffset(0);
        buffer.setFormat(inputFormat);
        buffer.setSequenceNumber(rtpPacket.seqnum);
        buffer.setFlags(Buffer.FLAG_RTP_MARKER | Buffer.FLAG_RTP_TIME);
        buffer.setRTPMarker(rtpPacket.marker != 0);
        buffer.setTimeStamp(rtpPacket.timestamp);

        inputFormat = null;
        return buffer;
    }
}
