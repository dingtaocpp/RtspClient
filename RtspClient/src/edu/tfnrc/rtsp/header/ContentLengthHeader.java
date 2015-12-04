package edu.tfnrc.rtsp.header;

/**
 * Created by leip on 2015/11/26.
 */
public class ContentLengthHeader extends RtspBaseIntegerHeader {

    public static final String NAME = "Content-Length";

    public ContentLengthHeader(){
        super(NAME);
    }

    public ContentLengthHeader(int value){
        super(NAME, value);
    }

    public ContentLengthHeader(String header) throws Exception{
        super(NAME, header);
    }
}
