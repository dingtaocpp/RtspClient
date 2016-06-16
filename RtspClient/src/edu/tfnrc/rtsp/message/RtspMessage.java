package edu.tfnrc.rtsp.message;

import android.util.Log;
import edu.tfnrc.rtsp.RtspConstants;
import edu.tfnrc.rtsp.api.EntityMessage;
import edu.tfnrc.rtsp.api.Message;
import edu.tfnrc.rtsp.header.CSeqHeader;
import edu.tfnrc.rtsp.header.RtspHeader;
import edu.tfnrc.rtsp.header.TransportHeader;

import java.util.ArrayList;
import java.util.List;

/**
 * Rtsp完整消息
 *
 * Created by leip on 2015/11/27.
 */
public abstract class RtspMessage implements Message{
    //Rtsp完整消息由以下4部分组成
    private String line;

    private List<RtspHeader> headers;

    private CSeqHeader cseq;

    private EntityMessage entity;

    public RtspMessage(){
        headers = new ArrayList<RtspHeader>();
    }

    public RtspHeader getHeader(final String name) throws Exception {
        int index = headers.indexOf(new Object() {

            public boolean equals(Object obj){
                return name.equalsIgnoreCase(((RtspHeader)obj).getName());
            }
        });


        if(index == -1) throw new Exception("[Missing Header] " + name);
        return headers.get(index);
    }

    //该实例转换为字节数据，以待发送
    public byte[] getBytes() throws Exception {
        //未抛出异常则说明存在CSeqHeader
        getHeader(CSeqHeader.NAME);
        //添加User-Agent头信息
        //TODO: 初步判断，服务端暂未识别
        addHeader(new RtspHeader("User-Agent", "RtspClient"));
        //获取Entity之前的信息部分
        byte[] message = toString().getBytes();
        if(getEntityMessage() != null) {
            byte[] body = entity.getBytes();
            byte[] full = new byte[message.length + body.length];

            System.arraycopy(message, 0, full, 0, message.length);
            System.arraycopy(body, 0, full, message.length, body.length);

            message = full;
        }
        return message;
    }

    /*存储的List转为Array*/
    public RtspHeader[] getHeaders() {
        return headers.toArray(new RtspHeader[headers.size()]);
    }

    public CSeqHeader getCSeq() {
        return cseq;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line){
        this.line = line;
    }

    public void addHeader(RtspHeader header){

        if(header == null) return;
        if(header instanceof CSeqHeader)
            cseq = (CSeqHeader) header;
        int index = headers.indexOf(header);

        //同名用新的头信息
        if(index > -1)
            headers.remove(index);
        else
            index = headers.size();

        headers.add(index, header);
    }

    @Override
    public EntityMessage getEntityMessage() {
        return entity;
    }

    public Message setEntityMessage(EntityMessage entity){
        this.entity = entity;
        return this;
    }

    public String toString(){

        StringBuffer buffer = new StringBuffer();
        buffer.append(getLine()).append("\r\n");

        for(RtspHeader header : headers)
            buffer.append(header)   /*autouse header.toString()*/
                    .append("\r\n");

        buffer.append("\r\n");
        return buffer.toString();
    }
}
