package edu.tfnrc.rtsp.header;

/**
 * Created by leip on 2015/11/26.
 */
public class AllowHeader extends RtspBaseStringHeader {

    public static final String NAME = "Allow";

    public AllowHeader(){
        super(NAME);
    }

    public AllowHeader(String header){
        super(NAME, header);

    }
}
