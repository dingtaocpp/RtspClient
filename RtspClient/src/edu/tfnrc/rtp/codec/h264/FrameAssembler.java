package edu.tfnrc.rtp.codec.h264;

import edu.tfnrc.rtp.codec.Codec;
import edu.tfnrc.rtp.media.format.Format;
import edu.tfnrc.rtp.util.Buffer;

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
    private static int MAX_H264P_FRAME_SIZE = 8192;//TODO: to be tested



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

        private static final int DEAFAULT_CAPACITY = 8;

        //a buffer array
        private Buffer[] buffers = null;

        //numbers of available elements in array
        private volatile int size = 0;

        //capacity to contain the Buffers,which can expand by twice
        private int capacity = DEAFAULT_CAPACITY;

        //the marker Buffer's SCeq
        private int endPos = Integer.MAX_VALUE - 2;

        //Frame data saved from input buffer with same timestamp
        private byte[] data = null;

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
        }

        /**
         * Add the buffer (which contains a fragment) to the array and sort by sequence number.
         */
        public void put(Buffer buffer){
            if(buffer == null) return;

            int index =(int) buffer.getSequenceNumber();
            if(index > endPos) return;

            //The first buffer is put in,init the timeStamp and format
            if(size == 0) {
                timeStamp = buffer.getTimeStamp();
                format = buffer.getFormat();
            }

            //judge if the array is expanded
            boolean expanded = false;
            while(index >= capacity){
                capacity <<= 1;
                expanded = true;
            }
            if(expanded){
                Buffer[] bftemp = this.buffers;
                this.buffers = new Buffer[capacity];
                System.arraycopy(bftemp, 0, this.buffers, 0, bftemp.length);
            }

            //put buffer into array and update
            if(buffers[index] == null) {
                buffers[index] = buffer;
                size++;
                dataLength += buffer.getLength();

                if(buffer.isRTPMarkerSet()){
                    //TODO: if there are several true marker(mostly impossible)
                    endPos = index;
                }
            }
            assembleBytes();
        }

        public boolean isCompleted(){

            return size == endPos + 1;
        }

        //assemble the buffer array to the data
        private void assembleBytes(){

            if(isCompleted() && data == null) {
                data = new byte[dataLength];
                for (int i = 0, offset = 0; i < size; ++i) {
                    System.arraycopy((byte[]) buffers[i].getData(), buffers[i].getOffset(), data, offset, buffers[i].getLength());
                    offset += buffers[i].getLength();
                }
            }
        }

        //copy the assembled frame to output buffer
        public void copyToBuffer(Buffer output){
            if(!isCompleted())
                throw new IllegalStateException();

            if(data.length <= 0)
                throw new IllegalStateException();

            if(data.length <= MAX_H264P_FRAME_SIZE){
                output.setData(data);
                output.setLength(data.length);
                output.setOffset(0);
                output.setTimeStamp(timeStamp);
                output.setFormat(format);
                output.setFlags(Buffer.FLAG_RTP_MARKER | Buffer.FLAG_RTP_TIME);
            }
            //set data to null
            data = null;
        }

        public byte[] getData(){
            return data;
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
