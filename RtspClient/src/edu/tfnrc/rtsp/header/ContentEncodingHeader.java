package edu.tfnrc.rtsp.header;

/**
 * TODO:服务端暂时无法识别
 *
 * Created by leip on 2015/11/26.
 */
public class ContentEncodingHeader extends RtspBaseStringHeader{

    public static final String NAME = "Content-Encoding";   //TODO: 未找到发送端相应头关键字

    public ContentEncodingHeader(){
        super(NAME);            //value未设置
    }

    public ContentEncodingHeader(String header){
        super(NAME, header);
    }
}
