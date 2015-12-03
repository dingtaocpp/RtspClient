package edu.tfnrc.rtsp.request;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by leip on 2015/11/30.
 */
public class RtspOptionsRequest extends RtspRequest {
    public RtspOptionsRequest(){
        super();
    }

    public RtspOptionsRequest(String line) throws URISyntaxException{
        super(line);
    }

    @Override
    public void setLine(Method method, String uri) throws URISyntaxException {

        setMethod(method);
        setURI("*".equals(uri) ? uri : new URI(uri).toString());

        super.setLine(method.toString() + ' ' + uri + ' ' + RTSP_VERSION_TOKEN);
    }
}
