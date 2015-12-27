package edu.tfnrc.rtsp.message;

import java.security.spec.ECField;

/**
 * 媒体相关设置
 * Created by leip on 2015/11/27.
 */
public class RtspMedia {

    private String mediaType;
    private String mediaFormat;

    private String transportPort;
    private String transportProtocol;

    private String encoding;
    private String clockrate;

    private String framerate;

    private static String SDP_CONTROL   = "a=control:";
    private static String SDP_RANGE     = "a=range:";
    private static String SDP_LENGTH    = "a=length:";
    private static String SDP_RTPMAP    = "a=rtpmap:";     //TODO:原名:SDP_RTMAP
    private static String SDP_FRAMERATE = "a=framerate:";
//    private static String SDP_RECVONLY  = "a=recvonly";

    public RtspMedia(String line){      //注意line格式，顺序
        String[] tokens = line.substring(2).split(" ");

        mediaType = tokens[0];
        mediaFormat = tokens[3];

        transportPort = tokens[1];
        transportProtocol = tokens[2];
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getFrameRate() {
        return framerate;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getTransportPort() {
        return transportPort;
    }

    public String getClockrate() {
        return clockrate;
    }

    //根据line信息设置媒体各属性
    public void setAttribute(String line) throws Exception {

        /*if(line.startsWith(SDP_RECVONLY)){
            return;
        }else*/
        if(line.startsWith(SDP_CONTROL)){
                return;
        }else if(line.startsWith(SDP_RANGE)){
                return;
        }else if(line.startsWith(SDP_LENGTH)){
                return;
        }else if(line.startsWith(SDP_FRAMERATE)){
            framerate = line.substring(SDP_FRAMERATE.length());
        }else if(line.startsWith(SDP_RTPMAP)){
            String[] tokens = line.substring(SDP_RTPMAP.length()).split(" ");

            String payloadType = tokens[0];
            if(payloadType.equals(mediaFormat) == false)
                throw new Exception("Corrupted Session Description - Payload Type");

            if(tokens[1].contains("/")){
                String[] subtokens = tokens[1].split("/");
                encoding = subtokens[0];
                clockrate = subtokens[1];
            }else {
                encoding = tokens[1];
            }
            //TODO: 处理"a=fmtp"
        }else   /*不正确的SDP描述*/
            throw new Exception("Uncorrect SDP Description");
    }

    public String toString(){
        return mediaType + " " + transportPort + " " + mediaFormat + " " +
                encoding + "/" + clockrate;
    }
}
