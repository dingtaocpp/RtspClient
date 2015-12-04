package edu.tfnrc.rtsp.header;

/**
 * value为数字的信息头
 * Created by leip on 2015/11/26.
 */
public class RtspBaseIntegerHeader extends RtspHeader {

    private int ivalue;

    public RtspBaseIntegerHeader(String name){
        super(name);

        String text = getRawValue();
        if(text != null)
            ivalue = Integer.parseInt(text);
    }

    public RtspBaseIntegerHeader(String name, int ivalue){
        super(name);
        setValue(ivalue);
    }

    public RtspBaseIntegerHeader(String name, String header)throws Exception {
        super(header);

        checkName(name);
        ivalue = Integer.parseInt(getRawValue());
    }

    public final void setValue(int value){
        ivalue = value;
        setRawValue(String.valueOf(value));
    }

    public final int getValue(){
        return ivalue;
    }
}
