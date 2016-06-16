package edu.tfnrc.rtsp.api;

/**
 * S->C回应消息
 *
 * Created by leip on 2015/11/26.
 */
public interface Response extends Message {

    public void setLine(int statusCode, String statusPhrase);

    public int getStatusCode();

    public String getStatusText();
}
