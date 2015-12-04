package edu.tfnrc.rtsp.header;

/**
 * Created by leip on 2015/11/26.
 */
public class CSeqHeader extends RtspBaseIntegerHeader {

    public static final String NAME = "CSeq";

    public CSeqHeader(){
        super(NAME);
    }

    public CSeqHeader(int cseq){
        super(NAME, cseq);
    }

    public CSeqHeader(String line){
        super(line);
    }
}
