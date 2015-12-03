package edu.tfnrc.rtsp.message;

import edu.tfnrc.rtsp.api.Message;

/**
 * Created by leip on 2015/11/27.
 */
public class MessageBuffer {
    /*
    * buffer for received data
    * */
    private byte[] data;

    /*
    * offset for starting useful area
    * */
    private int offset;

    /*
    * length of useful portion
    * */
    private int length;

    /*
    * used(read) buffer
    * */
    private int used;

    /*
    * {@link Message} create during last parsing
    * */
    private Message message;

    /*
    * Adds more data to buffer and ensures the sequence [data, newData] is
	* contiguous.
	*
	* @param newData data to be added to the buffer.
    * */
    public void addData(byte[] newData, int newLength){
        if(data == null){
            data = newData;
            length = newLength;
            offset = 0;
        } else {
            //buffer seems to be small
            if((data.length - offset - length) /*=block data length*/
                    < newLength){
                //delete the part before offset
                if(offset >= length && (data.length - length) >= newLength){
                    System.arraycopy(data, offset, data, 0, length);
                    offset = 0;
                } else { //create a new buffer
                    byte[] temp = new byte[data.length + newLength];
                    System.arraycopy(data, offset, temp, 0,length);
                    offset = 0;
                    data = temp;
                }
            }
            //there is enough room
            System.arraycopy(newData, 0, data, offset + length, newLength);
            length += newLength;
        }
    }
    /*
    * Discard used portions of the buffer
    * */
    public void discardData(){
        offset += used;
        length -= used;
        used = 0;
    }

    public byte[] getData(){
        return data;
    }

    public int getOffset(){
        return offset;
    }

    public int getLength() {
        return length;
    }

    public void setMessage(Message message){
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    public void setused(int used) {
        this.used = used;
    }
}
