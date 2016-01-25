package edu.tfnrc.rtp;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import edu.tfnrc.rtp.codec.Codec;
import edu.tfnrc.rtp.codec.h264.NativeH264Decoder;
import edu.tfnrc.rtp.media.format.Format;
import edu.tfnrc.rtp.media.format.H264VideoFormat;
import edu.tfnrc.rtp.media.format.VideoFormat;
import edu.tfnrc.rtp.media.video.VideoSurfaceView;
import edu.tfnrc.rtp.stream.OutputToFile;
import edu.tfnrc.rtp.stream.ProcessorOutputStream;
import edu.tfnrc.rtp.util.Buffer;
import edu.tfnrc.rtp.util.UDPConnection;
import edu.tfnrc.rtsp.RtspConstants;
import edu.tfnrc.rtsp.RtspControl;
import edu.tfnrc.rtsp.VideoSize;
import edu.tfnrc.rtsp.message.RtspDescriptor;
import edu.tfnrc.rtsp.message.RtspMedia;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.List;

/**
 * Video RTP renderer.Control the video to start, play, stop, and close.
 * Supports only H.264 format.
 * Controls the Rtp and Rtsp.
 * <p>
 * Created by leip on 2015/12/21.
 */
public class RtpVideoRenderer {

    //debug
    private static final String TAG = "RtpVideoRenderer";

    static{
        System.loadLibrary("decoder");
    }

    /**
     * List of supported video codecs
     * */
//    public static MediaCodec[] supportedMediaCodecs;

    /**
     * Selected video codec
     */
//    private VideoCodec selectedVideoCodec = null;
    /**
     * Video format
     */
    private VideoFormat videoFormat;

    /**
    *  Video configuration with numbers of parameters
    * */
//    private VideConfig videConfig;

    /**
     * Local RTP port
     */
    private int localRtpPort;

    /**
     * RTP receiver session
     */
    private MediaRtpReceiver rtpReceiver = null;

    /**
     * RTP media output
     */
    private ProcessorOutputStream rtpOutput = null;

    /**
     * Is player opened
     */
    private boolean opened = false;

    /**
     * Is player started
     */
    private boolean started = false;

    /**
     * Video start time
     */
    private long videoStartTime = 0L;

    /**
     * Video surface
     */
    private VideoSurfaceView surface = null;

    /**
     * Media event listeners
     */
//    private Vector<IMediaEventListener> listeners = new Vector<IMediaEventListener>();

    /**
     * Temporary connection to reserve the port
     */
    private UDPConnection temporaryConnection = null;

    /**
     * RTSP Control
     */
    private RtspControl rtspControl;

    /**
     * Constructor Force a RTSP Server Uri
     *
     * @throws Exception
     */
    public RtpVideoRenderer(String uri/*, Handler handler*/) throws Exception {

        Log.d(TAG, "Creating renderer");
        /*
         * The RtspControl opens a connection to an RtspServer, that
         * is determined by the URI provided.
         */
        rtspControl = new RtspControl(uri);

        Log.d(TAG, "RtspControl created. State code is " + rtspControl.getState());

        /*
         * wait unit the rtspControl has achieved status READY; in this
         * state, an SDP file is present and is ready to get evaluated
         */
        while (rtspControl.getState() != RtspConstants.READY) {
            ; // blocking
        }

        /*
         * Set the local RTP port: this is the (socket)
         * port, the RtspVideoRenderer is listening to
         * (UDP) RTP packets.
         */
        Log.d(TAG, "Rtsp READY");

        // localRtpPort = NetworkRessourceManager.generateLocalRtpPort();
        localRtpPort = rtspControl.getClientPort();
        reservePort(localRtpPort);

        Log.i(TAG, "localRtpPort: " + localRtpPort);
        /*
         * The media resources associated with the SDP descriptor are
         * evaluated and the respective video encoding determined
         */

        RtspDescriptor rtspDescriptor = rtspControl.getDescriptor();
        List<RtspMedia> mediaList = rtspDescriptor.getMediaList();

        if (mediaList.size() == 0) throw new Exception("The session description contains no media resource.");
        RtspMedia videoResource = null;

        for (RtspMedia mediaItem : mediaList) {

            if (mediaItem.getMediaType().equals(RtspConstants.SDP_VIDEO_TYPE)) {
                videoResource = mediaItem;
                break;
            }
        }
        if (videoResource == null) throw new Exception("The session description contains no video resource.");

        String codec = videoResource.getEncoding();
        if (codec == null) throw new Exception("No encoding provided for video resource.");

        if (codec.toLowerCase().contains("h264")) {
            setMediaCodec(RtspConstants.RTP_H264_PAYLOADTYPE);
        }

    }

    /**
     * Set the surface to render video
     *
     * @param surface Video surface
     */
    public void setVideoSurface(VideoSurfaceView surface) {
        this.surface = surface;
    }

    /**
     * Return the video start time
     *
     * @return Milliseconds
     */
    public long getVideoStartTime() {
        return videoStartTime;
    }

    /**
     * Returns the local RTP port
     *
     * @return Port
     */
    public int getLocalRtpPort() {
        return localRtpPort;
    }

    /**
     * Returns the local RTSP Control
     *
     * @return rtspControl
     */
    public RtspControl getRtspControl(){
        return rtspControl;
    }

    /**
     * Reserve a port.
     *
     * @param port the port to reserve
     */
    private void reservePort(int port) {

        if (temporaryConnection != null) return;
        try {
            temporaryConnection = new UDPConnection();
            temporaryConnection.open(port);

        } catch (Exception e) {
            temporaryConnection = null;
            Log.e(TAG, "failed to open port " + port, e);
        }

    }

    /**
     * Release the reserved port; this method
     * is invoked while preparing the RTP layer
     */
    private void releasePort() {

        if (temporaryConnection == null) return;
        try {
            temporaryConnection.close();

        } catch (IOException e) {
            temporaryConnection = null;
        }
    }

    /**
     * Is player started
     *
     * @return Boolean
     */
    public boolean isStarted() {
        return started;
    }


    /**
     * Is player opened
     *
     * @return Boolean
     */
    public boolean isOpened() {
        return opened;
    }

    /**
     * Set media codec, video format
     *
     * @param payloadType Video codec type
     */
    public void setMediaCodec(int payloadType) {

        if (payloadType == RtspConstants.RTP_H264_PAYLOADTYPE) {
//            selectedVideoCodec = new Codec();   //TODO: VideoCodec类，设置参数
            videoFormat = new H264VideoFormat();
        }
    }

    /**
     * Open the renderer
     */
    public void open() {

        if (opened) return; //already opened


        try {
            //Init the video decoder
            int result = 0;
            //TODO:native method of decoder
                result = NativeH264Decoder.initDecoder();

            if (result != 0) {
                Log.e(TAG, "Decoder init failed with error code: " + result);

                return;
            }
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Decoder error: " + e.getMessage());
            return;
        }

        try{
            Log.d(TAG, "init RTP layer");
            //init RTP layer
            releasePort();

            rtpOutput = new MediaRtpOutput();
//            rtpOutput = new OutputToFile("rtpoutput");
            rtpOutput.open();

            rtpReceiver = new MediaRtpReceiver(localRtpPort);
            rtpReceiver.prepareSession(rtpOutput, videoFormat);
        } catch (Exception e){
            Log.e(TAG, "RTP error: " + e.getMessage());
            return;
        }

        opened = true;
    }

    /**
     * Close the renderer
     */
    public void close() {

        if(!opened) return;

        //Send TEARDOWN request to RTSP server
        if(!rtspControl.isConnected())
            rtspControl.stop();

        //Close the RTP layer
        if (rtpReceiver != null) rtpReceiver.stopSession();

        if (rtpOutput != null) rtpOutput.close();

        // TODO: close the video decoder
      closeVideoDecoder();

        opened = false;
    }

    public void closeVideoDecoder(){
        NativeH264Decoder.DeinitDecoder();
    }

    /**
     * Start the RTP layer (i.e listen to the reserved local
     * port for RTP packets), and send a PLAY request to the
     * RTSP server
     */
    public void start(){

        if(!opened || started)
            return;

        //Start RTP layer
        rtpReceiver.startSession();

        //Send PLAY request to RTSP Server
        rtspControl.play();

        //Wait until the rtspControl has achieved status PLAYING
        while(rtspControl.getState() != RtspConstants.PLAYING){
            ;
        }

        //Start the renderer
        videoStartTime = SystemClock.uptimeMillis();
        started = true;

        Log.d(TAG, "start renderer successfully");

    }

    /**
     * Stop the renderer
     */
    public void stop(){
        if(!started) return;

        //Send TEARDOWN request to RTSP server
        rtspControl.stop();

        // Stop RTP layer
        if (rtpReceiver != null) rtpReceiver.stopSession();

        if (rtpOutput != null) rtpOutput.close();

        //Force black screen
        surface.clearImage();

        // Close the video decoder
        closeVideoDecoder();

        // Renderer is stopped
        started = false;
        videoStartTime = 0L;
    }

    /**
     * TODO: Media RTP output
     */
    private class MediaRtpOutput implements ProcessorOutputStream{

        /**
         * Video frame
         */
        private int decodedFrame[];

        /**
         * Bitmap frame
         */
        private Bitmap rgbFrame;

        VideoSize videoSize = RtspConstants.getVideoSize();
        public MediaRtpOutput(){
            decodedFrame = new int[videoSize.getWidth() * videoSize.getHeight()];
            rgbFrame     = Bitmap.createBitmap(videoSize.getWidth(), videoSize.getHeight(), Bitmap.Config.ARGB_8888);
        }
        @Override
        public void open() throws Exception {
            surface.setAspectRatio(1280, 720);
        }

        @Override
        public void write(Buffer buffer) throws Exception {

            if (NativeH264Decoder.DecodeAndConvert((byte[])buffer.getData(), decodedFrame) == 1) {
                rgbFrame.setPixels(decodedFrame, 0, videoSize.getWidth(), 0, 0,
                        videoSize.getWidth(), videoSize.getHeight());

                if (surface != null) {
                    surface.setImage(rgbFrame);
                }
            } else {
                Log.e(TAG, "MediaRtpOutput.writeSample: cannot decode sample >len:" + buffer.getLength());
            }
        }

        @Override
        public void close() {
        }

    }

}
