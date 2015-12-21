package edu.tfnrc.rtp.media.format;

/**
 * Abstract format class
 *
 * Created by leip on 2015/12/7.
 */
public abstract class Format {

    //Codec
    private String codec;

    //Payload Type
    private int payload;
    
    public Format(String codec, int payload){
        this.codec = codec;
        this.payload = payload;
    }

    public String getCodec() {
        return codec;
    }

    public int getPayload() {
        return payload;
    }
}
