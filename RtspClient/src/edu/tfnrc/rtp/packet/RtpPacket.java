package edu.tfnrc.rtp.packet;

/**
 * Created by leip on 2015/12/8.
 */
public class RtpPacket {

    public byte[] data;

    public int length;

    public int offset;

    //Received at
    public long receivedAt;

    //Rtp header information
    public int marker;
    public int payloadType;
    public int seqnum;
    public long timestamp;
    public int ssrc;
    public int payloadOffset;
    public int payloadLength;

    //Constructor
    public RtpPacket(){

    }

    public RtpPacket(RtpPacket packet){
        data = packet.data;
        length = packet.length;
        offset = packet.offset;
        receivedAt = packet.receivedAt;

    }

}
