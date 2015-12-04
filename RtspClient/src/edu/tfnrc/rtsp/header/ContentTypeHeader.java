package edu.tfnrc.rtsp.header;

/**
 * Created by leip on 2015/11/26.
 */
public class ContentTypeHeader extends RtspBaseStringHeader {

    public static final String NAME = "Content-type";

    public ContentTypeHeader(){
        super(NAME);
    }

    public ContentTypeHeader(String header){
        super(NAME, header);
    }
}
