package edu.tfnrc.rtp;

import android.util.Log;
import edu.tfnrc.rtp.codec.Codec;
import edu.tfnrc.rtp.stream.ProcessorInputStream;
import edu.tfnrc.rtp.stream.ProcessorOutputStream;
import edu.tfnrc.rtp.util.Buffer;

/**
 * In this thread Processor receive RTP packets, then assemble
 * them to complete frames and write the frames.
 *
 * Created by leip on 2015/12/16.
 */
public class Processor extends Thread {

    //debug
    private static final String TAG = "Processor";

    /**
     * Processor input stream
     */
    private ProcessorInputStream inputStream;

    /**
     * Processor output stream
     */
    private ProcessorOutputStream outputStream;

    /**
     * Codec
     */
    private Codec codec;

    /**
     * Processor status flag
     */
    private boolean interrupted = false;

    /**
     * Constructor
     *
     * @param inputStream Input stream
     * @param outputStream Output stream
     * @param codec List of codecs
     */
    public Processor(ProcessorInputStream inputStream, ProcessorOutputStream outputStream, Codec codec){
        super();

        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.codec = codec;

        Log.d(TAG, "Media processor created");
    }

    /**
    * Start processing
    * */
    public void startProcessing(){
        Log.d(TAG, "Start media processor");

        interrupted = false;
        start();
    }

    /**
    * Stop processing
    * */
    public void stopProcessing(){

        Log.d(TAG, "Stop media processing");

        interrupted = true;

        outputStream.close();
        inputStream.close();
    }

    /**
    * Background running
    * */
    public void run(){
        try{
            Log.d(TAG, "Processing is started");

            Buffer outputBuffer = null;
            //read packets and deal with them
            while(!interrupted){
                //read data from the input stream
                Buffer inBuffer = inputStream.read();
                if(inBuffer == null){
                    interrupted = true;
                    Log.d(TAG, "Processing terminated: null data received");
                    break;
                }
                if(outputBuffer == null || !outputBuffer.isDiscard()){
                    outputBuffer = new Buffer();
                }

                int result = codec.process(inBuffer, outputBuffer);
                if(result == Codec.BUFFER_PROCESSED_OK){

                    //output buffer written to the surface view
                    outputStream.write(outputBuffer);
                }else if(result == Codec.OUTPUT_BUFFER_NOT_FILLED) {
                    //continue to receive packet
                    continue;

                } else {

                    interrupted = true;
                    Log.e(TAG, "Codec processing error: " + result);
                    break;
                }

            }
        } catch (Exception e){
            if(!interrupted){
                Log.e(TAG, "Processor error", e);
            } else {
                Log.e(TAG, "Processor processing has been terminated", e);
            }

        }
    }
}
