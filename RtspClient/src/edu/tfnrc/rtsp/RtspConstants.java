package edu.tfnrc.rtsp;

/**
 * Rtsp相关常数声明存储
 * 需与服务端对应
 *
 * Created by leip on 2015/11/26.
 */
public class RtspConstants {

    // rtsp states
    public static int INIT 		= 0;
    public static int READY 	= 1;
    public static int PLAYING 	= 2;
    public static int UNDEFINED = 3;

    // rtsp message types
    public static int OPTIONS 	= 3;
    public static int DESCRIBE 	= 4;
    public static int SETUP 	= 5;
    public static int PLAY 		= 6;
    public static int PAUSE 	= 7;
    public static int TEARDOWN 	= 8;

    public static String SDP_AUDIO_TYPE = "audio";
    public static String SDP_VIDEO_TYPE = "video";

    public static int RTP_H264_PAYLOADTYPE = 96; // dynamic range
    public static int RTP_H265_PAYLOADTYPE = 98; // dynamic range

    public static String H264 = "H264/90000";
//    public static String H265 = "H265/";

    public static enum VideoEncoder {
        H264_ENCODER,
        H265_ENCODER
    };

    public static String WIDTH = "1280";
    public static String HEIGHT = "720";
    //默认720P
    public static VideoSize videoSize = new VideoSize();

    public static VideoSize getVideoSize(){
        return videoSize;
    }
}
