package edu.tfnrc.rtp.packet;

import android.os.Environment;
import android.util.Log;
import edu.tfnrc.rtp.util.UDPConnection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Parse the data to RTP packets
 *
 * Created by leip on 2015/12/8.
 */
public class RtpPacketReceiver {

    //debug
    private static final String TAG = "RtpPacketReceiver";

    /**
    * RTP header length should be constant of 12
    * */
    private static final int RTP_HEADER_LENGTH = 12;

    /**
    * Max datagram packet size.
    * The size may be variable.
    * */
    private static int DEFAULT_DATAGRAM_SIZE = 4096;

    /**
    * Another datagram packet size corresponding to
     * the rtsp-hi3518.
    * */
    private static int HI3518_DATAGRAM_SIZE = 1414;

    /*
    * Received buffer size for UDP socket.
    * The size should be enough for decreasing packets loss ratio.
    * */
    private static int DEFAULT_UDP_BUFFER_SIZE = 102400;

    /**
    * Statistics
    * */
    private RtpStatistics stats = new RtpStatistics();

    /**
     * Flag that indicates if the received buffer size has been set or not
     */
    private boolean recvBufSizeSet = false;

    /**
     * Buffer size needed to received RTP packet
     */
    private int bufferSize = HI3518_DATAGRAM_SIZE;

    /**
    * UDP connection
    * */
    public UDPConnection connection = null;

    /**
     * Constructor
     *
     * @param port Listenning port
     * @throws IOException
     */
    public RtpPacketReceiver(int port) throws IOException{
        //create UDP server
        connection = new UDPConnection();

        connection.open(port);

        Log.d(TAG, "RTP receiver created on port " + port);

        connection.setReceivedBufferSize(DEFAULT_UDP_BUFFER_SIZE);
    }

    /**
     * Close the receiver
     */
    public void close(){
        if(connection != null){
            try{
                connection.close();
            }catch (Exception e){
                Log.w(TAG, "Can't close correctly the datagram connection");
            }
            connection = null;
        }
    }


    /**
     * Read a RTP packet (blocking method)
     *
     * @return RTP packet
     */
    public RtpPacket readRtpPacket(){
        try {
            //Wait a new packet
            byte[] data = connection.receive(bufferSize);

            //Parse the RTP packet
            RtpPacket pkt = parseRtpPacket(data);

            //debug
            StringBuilder debugStr = new StringBuilder("time stamp: ")
                    .append(pkt.timestamp).append('\n')
                    .append("payload length: ").append(pkt.payloadLength)
                    .append('\n').append("marker: ").append(pkt.marker)
                    .append('\n').append("seqnum: ").append(pkt.seqnum);



            Log.i(TAG,debugStr.toString());


            if(pkt.payloadType != 12){

                //update statistics
                stats.numPackets++;
                stats.numBytes += data.length;

                //TODO:Rtcp Session...


                return pkt;
            }else{
                //Drop the keep-alive packets(payload 12)
                return readRtpPacket();
            }
        } catch (Exception e){
            Log.e(TAG, "Can't parse the RTP packet", e);
            stats.numBadRtpPkts++;
            return null;
        }
    }

    /**
     * Set the size of the received buffer
     *
     * @param size New buffer size
     */
    public void setRecvBufSize(int size) {
        this.bufferSize = size;
    }

    /**
     * Parse the RTP packet
     *
     * @param data RTP packet not yet parsed
     * @return RTP packet
     */
    private RtpPacket parseRtpPacket(byte[] data){

        RtpPacket packet = new RtpPacket();

//        write(data);

        try{
            packet.length = data.length;

            //set received timestamp
            packet.receivedAt = System.currentTimeMillis();

            //Read marker
            if(((data[1] & 0xff) & 0x80) == 0x80){
                packet.marker = 1;
            }else {
                packet.marker = 0;
            }

            //Read payload type
            packet.payloadType = (byte)((data[1] & 0xff) & 0x7f);

            //Read seq number
            packet.seqnum = (short)(((data[2] & 0xff) << 8) | (data[3] & 0xff));

            //Read timestamp
//            packet.timestamp = (((data[4] & 0xff) << 24) | ((data[5] & 0xff) << 16)
//                                | ((data[6] & 0xff) << 8) | (data[7] & 0xff));

            /*Read timestamp from rtsp-hi3518*/
            packet.timestamp = (((data[8] & 0xff) << 24) | ((data[9] & 0xff) << 16)
                    | ((data[10] & 0xff) << 8) | (data[11] & 0xff));

            //Read SSRC
//            packet.ssrc = (((data[8] & 0xff) << 24) | ((data[9] & 0xff) << 16)
//                    | ((data[10] & 0xff) << 8) | (data[11] & 0xff));
            packet.ssrc = 0;

            //Read media data after 12 byte header
            //Read fu-indicator and fu-header
            byte head1 = data[RTP_HEADER_LENGTH], head2 = data[RTP_HEADER_LENGTH + 1];

            Log.i(TAG, "head1: " + head1 + "\thead2: " + head2);
            boolean isFirstFu = false;   //if the data is the first packet in a split frame
            if((head1 & 0x1f) == 28){  //frame is split

                if((head2 & 0xe0) == 0x80){   //the first packet of a frame
                    packet.payloadOffset = RTP_HEADER_LENGTH + 1;
                    isFirstFu = true;
                }else {
                    packet.payloadOffset = RTP_HEADER_LENGTH + 2;
                }

            } else {        //One frame in one packet
                packet.payloadOffset = RTP_HEADER_LENGTH;
            }
            packet.payloadLength = packet.length - packet.payloadOffset;
            packet.data = new byte[packet.payloadLength];
            System.arraycopy(data, packet.payloadOffset, packet.data, 0, packet.payloadLength);

            if(isFirstFu)
                packet.data[0] = (byte)((head1 & 0xe0) | (head2 & 0x1f));

            // Update the buffer size
            if (!recvBufSizeSet) {
                recvBufSizeSet = true;
                switch (packet.payloadType) {
                    case 14:
                    case 26:
                    case 34:
                    case 42:
                        setRecvBufSize(HI3518_DATAGRAM_SIZE);
                        break;
                    case 31:
                        setRecvBufSize(HI3518_DATAGRAM_SIZE);
                        break;
                    case 32:
                        setRecvBufSize(HI3518_DATAGRAM_SIZE);
                        break;

                    default:
                        if ((packet.payloadType >= 96) && (packet.payloadType <= 127)) {
                            setRecvBufSize(HI3518_DATAGRAM_SIZE);
                        }
                }
            }
        } catch (Exception e){
            Log.e(TAG, "RTP packet parsing error", e);
            return null;
        }
        return packet;
    }

    /**
     * Returns the statistics of RTP reception
     *
     * @return Statistics
     */
    public RtpStatistics getRtpReceptionStats() {
        return stats;
    }

    /**
     * Returns the DatagramConnection of RTP
     *
     * @return DatagramConnection
     */
    public UDPConnection getConnection() {
        return connection;
    }

    private int headerIndex = 0;
    private void write(byte[] data){

        try{
            File file = new File(Environment.getExternalStorageDirectory() + "/header",
                    "" + (headerIndex++)  + "-" + (((data[8] & 0xff) << 24) | ((data[9] & 0xff) << 16)
                            | ((data[10] & 0xff) << 8) | (data[11] & 0xff)) + (((data[2] & 0xff) << 8) | (data[3] & 0xff)));
            OutputStream output = new FileOutputStream(file);
            output.write(data, 0, 12);
        }catch (Exception e){
            Log.e(TAG, "failed to write header", e);
        }

    }
}

/**
 * RTP statistics
 *
 */
class RtpStatistics{

    /**
     * Number of RTP packets received
     */
    public int numPackets = 0;

    /**
     * Number of RTP bytes received
     */
    public int numBytes = 0;

    /**
     * Number of bad RTP packet received
     */
    public int numBadRtpPkts = 0;
}
