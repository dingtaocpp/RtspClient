package edu.tfnrc.rtsp.header;

/**
 * Created by leip on 2015/11/26.
 */
public class SessionHeader extends RtspBaseStringHeader{

    public static final String NAME = "Session";

    public SessionHeader(){
        super(NAME);
    }

    public SessionHeader(String header){
        super(NAME, header);
    }
}
