package edu.tfnrc.rtsp.request;

import edu.tfnrc.rtsp.RtspClient;
import edu.tfnrc.rtsp.api.Response;
import edu.tfnrc.rtsp.header.SessionHeader;

import java.net.URISyntaxException;

/**
 * Created by leip on 2015/11/30.
 */
public class RtspSetupRequest extends RtspRequest {
    public RtspSetupRequest() {
    }

    public RtspSetupRequest(String line) throws URISyntaxException {
        super(line);
    }

    @Override
    public byte[] getBytes() throws Exception {
        getHeader("Transport");
        return super.getBytes();
    }

    @Override
    public void handleResponse(RtspClient client, Response response) {
        super.handleResponse(client, response);
        try{
            if(response.getStatusCode() == 200)
                client.setSession((SessionHeader) response.getHeader(SessionHeader.NAME));
        } catch (Exception e){
            client.getRequestListener().onError(client, e);
        }
    }
}
