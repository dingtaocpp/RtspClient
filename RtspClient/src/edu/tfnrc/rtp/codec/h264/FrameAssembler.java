package edu.tfnrc.rtp.codec.h264;

import android.util.Log;
import edu.tfnrc.rtp.codec.Codec;
import edu.tfnrc.rtp.media.format.Format;
import edu.tfnrc.rtp.stream.OutputToFile;
import edu.tfnrc.rtp.util.Buffer;

import java.io.IOException;

/**
 * Assemble buffer to frames
 *
 * Created by leip on 2015/12/9.
 */
public class FrameAssembler extends Codec{

    //debug
    private static final String TAG = "FrameAssembler";

    /**
     * Collection of frames.
     * Allows the construction of several frames if incoming packets are out of order
     */
    FrameCollection frameCollection = new FrameCollection();

    /**
     * Max frame size to give for next module, as some decoder have frame size limits
     */
    private static int MAX_H264P_FRAME_SIZE = 50720;//TODO: to be tested



    public FrameAssembler(){

    }

    /**
    * Performs the media processing
     *
     * @param input The buffer that contains the media data to be assembled
     * @param output The buffer in which to store the assembled media data
     * @return Processing result code
    * */
    public int process(Buffer input, Buffer output){
        if(!input.isDiscard()) {
            frameCollection.put(input);

            if(frameCollection.getLastActiveFrame().isCompleted()){
                frameCollection.getLastActiveFrame().copyToBuffer(output);

                output.setDiscard(false);
                frameCollection.removeOlderThan(input.getTimeStamp());
                return BUFFER_PROCESSED_OK;
            }
        }
        output.setDiscard(true);
        return OUTPUT_BUFFER_NOT_FILLED;

    }


    /**
     * Used to assemble fragments with the same timestamp into a single frame.
     */
    static class Frame{

        private static final int DEAFAULT_CAPACITY = 16;

        //output to file for debug
        private static OutputToFile output = new OutputToFile("frameOutput");

        //a buffer array
        private Buffer[] buffers = null;

        //numbers of available elements in array
        private int size = 0;

        //save least seqnumber
        private int minSeqnum = Integer.MAX_VALUE;

        //capacity to contain the Buffers,which can expand by twice
        private int capacity = DEAFAULT_CAPACITY;

        //the marker Buffer's SCeq
        private int endPos = Integer.MAX_VALUE - 2;

        //Frame data saved from input buffer with same timestamp
//        private byte[] data = null;

        //Sum of buffers' data length, for output data
        int dataLength = 0;

        private long timeStamp = -1;

        private Format format = null;


        public Frame(){
            buffers = new Buffer[DEAFAULT_CAPACITY];
        }

        public Frame(long timeStamp){
            this.timeStamp = timeStamp;
            buffers = new Buffer[DEAFAULT_CAPACITY];
            try{
                output.open();
            }catch (Exception e){
                Log.e(TAG, "output open fail", e);
            }
        }

        /**
         * Add the buffer (which contains a fragment) to the array and sort by sequence number.
         */
        public synchronized void put(Buffer buffer){
            if(buffer == null) return;


            int seqnum = (int) buffer.getSequenceNumber();
            if(seqnum > endPos) return;
            if(isCompleted()) return;

            //The first buffer is put in,init the timeStamp and format
            if(size == 0) {
                timeStamp = buffer.getTimeStamp();
                format = buffer.getFormat();
            }

            minSeqnum = Math.min(minSeqnum, seqnum);

            //judge if the array is expanded
            boolean expanded = false;
            while(seqnum - minSeqnum >= capacity){
                capacity <<= 1;
                expanded = true;
            }
            if(expanded){
                Buffer[] tempbfs = this.buffers;
                this.buffers = new Buffer[capacity];
                System.arraycopy(tempbfs, 0, this.buffers, 0, tempbfs.length);
            }


                //put buffer into array and update
                if (buffers[size] == null) {
                    buffers[size] = buffer;
                    size++;
                    dataLength += buffer.getLength();

                    //output to file for debug
//                try {
//                    output.write(buffer);
//                } catch (Exception e){
//                    Log.e(TAG, "failed to output", e);
//                }

                    Log.i(TAG, "TimeStamp: " + timeStamp + "\tsize: " + size
                            + "\tSeqnum: " + seqnum);
//                    for(int i = 0; i < size; ++i)
//                        Log.i(TAG, "buffers[" + i + "].seqnum: " + buffers[i].getSequenceNumber());

                    if (buffer.isRTPMarkerSet()) {
                        //TODO: if there are several true marker(almost impossible)
                        endPos = seqnum;
//                        Log.d(TAG, "endPos: " + endPos);
                    }
                }

//            assembleBytes();
        }

        public boolean isCompleted(){

            return size == endPos - minSeqnum + 1;
        }

        //assemble the buffer array to the data
        private byte[] assembleBytes(){

                Log.d(TAG, "start assembling");

                sortBuffers();
                byte[] data = new byte[dataLength];
                for (int i = 0, offset = 0; i < size; ++i) {
                    Log.i(TAG, "seqnum: " + buffers[i].getSequenceNumber());
                    System.arraycopy((byte[]) buffers[i].getData(), buffers[i].getOffset(), data, offset, buffers[i].getLength());
                    offset += buffers[i].getLength();
                }
            return data;
        }

        //sort the buffers as the seqnumber, assuming the buffers are completed
        private void sortBuffers(){
            Buffer[] temp = new Buffer[size];
            int pos = 0;
            System.arraycopy(buffers, 0, temp, 0, size);
            for(int i = 0; i < this.size; ++i){
                pos = (int) (buffers[i].getSequenceNumber() - minSeqnum);
                buffers[pos] = temp[i];
                }
        }


        //copy the assembled frame to output buffer
        public void copyToBuffer(Buffer output){
            if(!isCompleted())
                throw new IllegalStateException();

            byte[] data = assembleBytes();
            if(data.length <= 0)
                throw new IllegalStateException();

//            if(data.length <= MAX_H264P_FRAME_SIZE){


                output.setData(data);
                output.setLength(data.length);
                output.setOffset(0);
                output.setTimeStamp(timeStamp);
                output.setSequenceNumber(0L);
                output.setFormat(format);
                output.setFlags(Buffer.FLAG_RTP_MARKER | Buffer.FLAG_RTP_TIME);
//            }
        }



        public long getTimeStamp() {
            return timeStamp;
        }
    }

    static class FrameCollection{
        final static int NUMBER_OF_FRAMES = 8;



        private Frame[] frames = new Frame[NUMBER_OF_FRAMES];
        private int activeFrame = 0;
        private int numberOfFrames = 0;

        /**
        * Add the buffer to the correct frame
        * */
        public void put(Buffer buffer){

            activeFrame = getFrame(buffer.getTimeStamp());
            if(activeFrame == -1) return;
            frames[activeFrame].put(buffer);
        }

        /**
        * Get the last active frame
        * */
        public Frame getLastActiveFrame(){
            return frames[activeFrame];
        }

        /**
         * Get the assembler used for given timestamp.
         * If the timestamp doesn't exist, create a new frame.
         *
         * @param timeStamp
         * @return frame number position in the collection
         * */
        public int getFrame(long timeStamp){
            int spot = -1;

            /**
            * find the spot frame just after or the same as @param timeStamp
            * */
            for(int i = 0; i < numberOfFrames; ++i){
                if(timeStamp <= frames[i].getTimeStamp()){
                    spot = i;
                    break;
                }
            }
            if(spot == -1){
                spot = numberOfFrames;
            }
            //find the correct timeStamp(spot != -1 && spot != numberOfFrames)
            else if(frames[spot].getTimeStamp() == timeStamp){
                return spot;
            }
            //there's no existing frame with @param timeStamp
            //we should create a new frame
            if(numberOfFrames < NUMBER_OF_FRAMES){
                /*if there's enough space to create a new frame*/

                //The right frames of spot move to the right to make room for new frame
                for(int i = numberOfFrames; i > spot; --i){
                    frames[i] = frames[i-1];
                }
                //Create a new Frame
                frames[spot] = new Frame(timeStamp);
                numberOfFrames ++;
            } else{
                //Not enough space, we destroy the oldest frame
                for(int i = 1; i < spot; ++i){
                    frames[i-1] = frames[i];
                }
                if(spot != 0)
                    --spot;
                else
                    return -1;  //no space for oldest timeStamp
                frames[spot] = new Frame(timeStamp);

            }
            return spot;
        }

        /**
         * Remove oldest FrameAssembler than given timeStamp
         * (if given timeStamp has been rendered, then oldest ones are no more of use)
         * This also removes given timeStamp.
         *
         * @param timeStamp
         */
        public void removeOlderThan(long timeStamp){
            //spot means the leftest frame to be preserved
            int spot = numberOfFrames;
            for(int i = 0; i < numberOfFrames; ++i){
                if(timeStamp < frames[i].getTimeStamp()){
                    spot = i;
                    break;
                }
            }

            //remove all frames with older timeStamp to the left
            numberOfFrames -= spot;
            for(int i = 0; i < numberOfFrames; ++i){
                frames[i] = frames[i + spot];
            }
        }

    }



}
