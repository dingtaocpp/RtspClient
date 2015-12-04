package edu.tfnrc.rtsp.api;

import edu.tfnrc.rtsp.RtspClient;

/**
 * Created by leip on 2015/11/26.
 */
public interface RequestListener {
    public void onDescriptor(RtspClient client, String descriptor);

    public void onError(RtspClient client, Throwable error);

    public void onFailure(RtspClient client, Request request, Throwable cause);

    public void onSuccess(RtspClient client, Request request, Response response);
}
