package edu.tfnrc.rtsp.request;

import edu.tfnrc.rtsp.header.SessionHeader;

import java.net.URISyntaxException;

/**
 * Created by leip on 2015/11/30.
 */
public class RtspPauseRequest extends RtspRequest {

    public RtspPauseRequest()
    {
        super();
    }

    public RtspPauseRequest(String messageLine) throws URISyntaxException{
        super(messageLine);
    }

    @Override
    public byte[] getBytes() throws Exception {
        getHeader(SessionHeader.NAME);
        return super.getBytes();
    }
}
