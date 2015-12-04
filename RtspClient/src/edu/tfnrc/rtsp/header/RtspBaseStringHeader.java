package edu.tfnrc.rtsp.header;

/**
 * value is a String
 * Created by leip on 2015/11/26.
 */
public class RtspBaseStringHeader extends RtspHeader {

    public RtspBaseStringHeader(String name){
        super(name);
    }

    public RtspBaseStringHeader(String name, String header){
        super(header);

        try{
            checkName(name);
        } catch(Exception e){
            setName(name);
        }
    }
}
