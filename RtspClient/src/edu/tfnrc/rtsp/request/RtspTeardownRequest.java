package edu.tfnrc.rtsp.request;

import android.util.Log;
import edu.tfnrc.rtsp.RtspClient;
import edu.tfnrc.rtsp.api.Response;
import edu.tfnrc.rtsp.header.SessionHeader;

import java.net.URISyntaxException;

/**
 * Created by leip on 2015/11/30.
 */
public class RtspTeardownRequest extends RtspRequest {

    public RtspTeardownRequest() {
        super();

        Log.d("RtspClient", "Teardown Constuct");

    }

    public RtspTeardownRequest(String messageLine) throws URISyntaxException {
        super(messageLine);
    }

    @Override
    public byte[] getBytes() throws Exception {
        getHeader(SessionHeader.NAME);
        return super.getBytes();
    }

    @Override
    public void handleResponse(RtspClient client, Response response) {
        super.handleResponse(client, response);

        if(response.getStatusCode() == 200)
            client.setSession(null);
        client.getTransport().disconnect();
    }
}
