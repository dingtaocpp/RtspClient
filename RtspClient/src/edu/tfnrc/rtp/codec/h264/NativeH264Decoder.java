package edu.tfnrc.rtp.codec.h264;

/**
 * Created by leip on 2016/1/11.
 */
public class NativeH264Decoder {

    public static native int initDecoder();

    public static native int DeinitDecoder();

    public static synchronized native int DecodeAndConvert(byte abyte0[], int ai[]);

    public static native int getVideoWidth();

    public static native int getVideoHeight();

    public static native int findDecoder(int codecId);
}
