package edu.tfnrc.rtsp.header;

/**
 * Created by leip on 2015/11/26.
 */
public class AcceptHeader extends RtspBaseStringHeader {

    public static final String NAME = "Accept";

    public AcceptHeader(){
        super(NAME);
    }

    public AcceptHeader(String header){
        super(NAME, header);

    }
}
