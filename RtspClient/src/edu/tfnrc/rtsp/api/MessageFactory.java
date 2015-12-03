package edu.tfnrc.rtsp.api;

import edu.tfnrc.rtsp.header.RtspContent;
import edu.tfnrc.rtsp.header.RtspHeader;
import edu.tfnrc.rtsp.message.MessageBuffer;

import java.net.URISyntaxException;

/**
 * Created by leip on 2015/11/26.
 */
public interface MessageFactory {

    //TODO: RtspContent, code 等使用需注意Encoding头关键字

    public void incomingMessage(MessageBuffer message) throws Exception;

    public Request outgoingRequest(String uri, Request.Method method, int cseq, RtspHeader...extras)
            throws URISyntaxException;

    public Request outgoingRequest(RtspContent body, String uri, Request.Method method, int cseq, RtspHeader...extras)
            throws URISyntaxException;

    public Response outgoingResponse(int code, String message, int cseq, RtspHeader...extras);

    public Response outgoingResponse(RtspContent body, int code, String text, int cseq, RtspHeader...extras);

}
