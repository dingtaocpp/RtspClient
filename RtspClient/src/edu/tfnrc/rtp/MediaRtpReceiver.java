package edu.tfnrc.rtp;

import android.util.Log;
import edu.tfnrc.rtp.codec.Codec;
import edu.tfnrc.rtp.codec.h264.FrameAssembler;
import edu.tfnrc.rtp.media.format.Format;
import edu.tfnrc.rtp.stream.OutputToFile;
import edu.tfnrc.rtp.stream.ProcessorOutputStream;
import edu.tfnrc.rtp.stream.RtpInputStream;
import edu.tfnrc.rtsp.RtspConstants;

/**
 * Created by leip on 2015/12/7.
 */
public class MediaRtpReceiver {

    //debug logger tag
    private static final String TAG = "MediaRtpReceiver";

    //Media processor
    Processor processor = null;

    //Local port number(RTP listening port)
    private int localPort;

    //RTP input stream
    private RtpInputStream inputStream = null;

    //TODO: finally should be RtpOutputStream
    private ProcessorOutputStream outputStream = null;

    /**
    * Constructor
    *
    * @param localPort local port number to listen for rtp
    * */
    public MediaRtpReceiver(int localPort){
        this.localPort = localPort;
    }

    public void prepareSession(ProcessorOutputStream output, Format format) {

        try{
            inputStream = new RtpInputStream(localPort, format);
            inputStream.open();

            Log.d(TAG, "Input stream: " + inputStream.getClass().getName());

            //output stream
            outputStream = output;
            outputStream.open();

            Log.d(TAG, "Output stream: " + outputStream.getClass().getName());

            Codec codec = new edu.tfnrc.rtp.codec.h264.FrameAssembler();
            //TODO:"h264" is to be tested, 'else{...}' case is for h265
           /* if(format.getPayload() == RtspConstants.RTP_H264_PAYLOADTYPE) {
                codec = new edu.tfnrc.rtp.codec.h264.FrameAssembler();
            }*/
            //new processor
            processor = new Processor(inputStream, output, codec);

            Log.d(TAG, "Processor has been prepared with success");

        } catch (Exception e){
            Log.e(TAG, e.toString());
            Log.e(TAG, "Can't prepare resources correctly");
        }

    }

    /**
    * Start the RTP session
    * */
    public void startSession(){
        Log.i(TAG, "Start the session");
        if(processor != null)
            processor.startProcessing();
    }

    /**
     * Stop the RTP session
     */
    public void stopSession(){
        Log.i(TAG, "Stop the session");
        processor.stopProcessing();
    }

    /**
     * Returns the RTP input stream
     *
     * @return RTP input stream
     */
    public RtpInputStream getInputStream() {
        return inputStream;
    }
}
