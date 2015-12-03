package edu.tfnrc.rtsp.api;

import edu.tfnrc.rtsp.header.CSeqHeader;
import edu.tfnrc.rtsp.header.RtspHeader;

/**
 * Message = ÃüÁî||×´Ì¬ + Header + "\r\n" + EntityMessage
 * Created by leip on 2015/11/26.
 */
public interface Message {

    static String RTSP_TOKEN = "RTSP/";

    static String RTSP_VERSION = "1.0";

    static String RTSP_VERSION_TOKEN = RTSP_TOKEN + RTSP_VERSION;

    /*
     * @return the Message line(the first line of the message)
     */
    public String getLine();

    /*
    * Returns a header, if exists
    *
    * @param name
    *           Name of the header to be searched
    * @return the found header class
    * @throws Exception
    * */
    public RtspHeader getHeader(String name) throws Exception;

    /*
    * Convenient method to get CSeq.
    *
    * @return the CSeqHeader
    * */
    public CSeqHeader getCSeq();

    /*
    *
    * @return all header in the message, except CSeq
    * */
    public RtspHeader[] getHeaders();

    /**
     * Adds a new header or replaces if one already exists. If header to be added
     * is a CSeq, implementation MUST keep reference of this header.
     *
     * @param header
     */
    public void addHeader(RtspHeader header);

    /*
    *
    * @return message as a byte array, ready for transmission.
    * */
    public byte[] getBytes() throws Exception;

    /*
    *
    * @return Entity part of message, if exists.
    * */
    public EntityMessage getEntityMessage();

    /**
     *
     * @param entity
     *          adds an entity part to the message.
     * @return this, for easier construction.
     */
    public Message setEntityMessage(EntityMessage entity);

}
