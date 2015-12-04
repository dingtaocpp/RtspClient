package edu.tfnrc.rtsp.response;

import edu.tfnrc.rtsp.api.Response;
import edu.tfnrc.rtsp.message.RtspMessage;

/**
 * Created by leip on 2015/11/30.
 */
public class RtspResponse extends RtspMessage implements Response{

    private int status;
    private String text;

    public RtspResponse(){
    }

    public RtspResponse(String line){

        setLine(line);
        line = line.substring(line.indexOf(' ') + 1);

        status = Integer.parseInt(line.substring(0, line.indexOf(' ')));
        text = line.substring(line.indexOf(' ') + 1);
    }

    public int getStatusCode() {
        return status;
    }

    public String getStatusText() {
        return text;
    }

    public void setLine(int statusCode, String statusText){

        status = statusCode;
        text = statusText;
        super.setLine(RTSP_VERSION_TOKEN + ' ' + status + ' ' + text);
    }
}
