package edu.tfnrc.rtsp.header;

import edu.tfnrc.rtsp.api.Message;

/**
 * Created by leip on 2015/11/26.
 */
public class RtspContent {

    private String type;

    private String encoding;    //TODO: ´ý¸Ä¶¯

    private byte[] content;

    public void setDescription(Message message) throws Exception {

        type = message.getHeader(ContentTypeHeader.NAME).getRawValue();
        try{
            encoding = message.getHeader(ContentEncodingHeader.NAME).getRawValue();

        } catch (Exception e){

        }
    }

    public String getType(){
        return type;
    }

    public void setType(String type){
        this.type = type;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public byte[] getBytes(){
        return content;
    }

    public void setBytes(byte[] content){
        this.content = content;
    }
}
