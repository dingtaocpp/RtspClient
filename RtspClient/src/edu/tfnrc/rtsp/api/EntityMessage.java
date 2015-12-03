package edu.tfnrc.rtsp.api;

import edu.tfnrc.rtsp.header.RtspContent;

/**
 * Created by leip on 2015/11/26.
 */
public interface EntityMessage {

    public RtspContent getContent();

    public void setContent(RtspContent content);

    public Message getMessage();
    
    public byte[] getBytes() throws Exception;

    public boolean isEntity();
}
