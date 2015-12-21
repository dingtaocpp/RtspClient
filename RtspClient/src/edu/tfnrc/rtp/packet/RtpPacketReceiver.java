package edu.tfnrc.rtp.packet;

import android.util.Log;
import edu.tfnrc.rtp.util.UDPConnection;

import java.io.IOException;

/**
 * Parse the data to RTP packets
 *
 * Created by leip on 2015/12/8.
 */
public class RtpPacketReceiver {

    //debug
    private static final String TAG = "RtpPacketReceiver";

    /**
    * Max datagram packet size.
    * The size may be variable.
    * */
    private static int DEFAULT_DATAGRAM_SIZE = 4096;

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
    private int bufferSize = DEFAULT_DATAGRAM_SIZE;

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
            packet.timestamp = (((data[4] & 0xff) << 24) | ((data[5] & 0xff) << 16)
                                | ((data[6] & 0xff) << 8) | (data[7] & 0xff));

            //Read SSRC
            packet.ssrc = (((data[8] & 0xff) << 24) | ((data[9] & 0xff) << 16)
                    | ((data[10] & 0xff) << 8) | (data[11] & 0xff));

            //Read media data after 12 byte header
            packet.payloadOffset = 12;
            packet.payloadLength = packet.length - packet.payloadOffset;
            packet.data = new byte[packet.payloadLength];
            System.arraycopy(data, packet.payloadOffset, packet.data, 0, packet.payloadLength);

            // Update the buffer size
            if (!recvBufSizeSet) {
                recvBufSizeSet = true;
                switch (packet.payloadType) {
                    case 14:
                    case 26:
                    case 34:
                    case 42:
                        setRecvBufSize(64000);
                        break;
                    case 31:
                        setRecvBufSize(0x1f400);
                        break;
                    case 32:
                        setRecvBufSize(0x1f400);
                        break;

                    default:
                        if ((packet.payloadType >= 96) && (packet.payloadType <= 127)) {
                            setRecvBufSize(64000);
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
