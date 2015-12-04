package edu.tfnrc.rtsp.message;

import edu.tfnrc.rtsp.api.EntityMessage;
import edu.tfnrc.rtsp.api.Message;
import edu.tfnrc.rtsp.header.ContentEncodingHeader;
import edu.tfnrc.rtsp.header.ContentLengthHeader;
import edu.tfnrc.rtsp.header.ContentTypeHeader;
import edu.tfnrc.rtsp.header.RtspContent;

/**
 * Created by leip on 2015/11/27.
 */
public class RtspEntityMessage implements EntityMessage {

    private RtspContent content;

    private final Message message;

    public RtspEntityMessage(Message message){
        this.message = message;
    }

    public RtspEntityMessage(Message message, RtspContent body){
        this(message);
        setContent(body);
    }

    @Override
    public Message getMessage() {
        return message;
    }

    @Override
    public byte[] getBytes() throws Exception{
        message.getHeader(ContentTypeHeader.NAME);
        message.getHeader(ContentLengthHeader.NAME);

        return content.getBytes();
    }

    @Override
    public RtspContent getContent(){
        return content;
    }

    @Override
    public void setContent(RtspContent content) {
        if(content == null) throw new NullPointerException();
        this.content = content;

        message.addHeader(new ContentTypeHeader(content.getType()));
        if(content.getEncoding() != null)   //TODO:EncodingHeaderŒ Ã‚
            message.addHeader(new ContentEncodingHeader(content.getEncoding()));
        message.addHeader(new ContentLengthHeader(content.getBytes().length));
    }

    @Override
    public boolean isEntity() {
        return content != null;
    }
}
