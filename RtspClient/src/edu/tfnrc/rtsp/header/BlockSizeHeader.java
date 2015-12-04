package edu.tfnrc.rtsp.header;

/**
 * Created by leip on 2015/11/26.
 */
public class BlockSizeHeader extends RtspBaseIntegerHeader {
    public static final String NAME = "BlockSize";

    public BlockSizeHeader(){
        super(NAME);
    }

    public BlockSizeHeader(int size){
        super(NAME, size);
    }

    public BlockSizeHeader(String header) throws Exception{
        super(NAME, header);
    }
}
