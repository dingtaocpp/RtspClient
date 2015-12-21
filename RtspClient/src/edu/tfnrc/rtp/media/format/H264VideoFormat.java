package edu.tfnrc.rtp.media.format;

/**
 * Created by leip on 2015/12/21.
 */
public class H264VideoFormat extends VideoFormat {
    /**
     * Encoding name
     */
    public static final String ENCODING = "h264";

    /**
     * Payload type
     */
    public static final int PAYLOAD = 96;

    /**
     * Constructor
     */
    public H264VideoFormat() {
        super(ENCODING, PAYLOAD);
    }
}
