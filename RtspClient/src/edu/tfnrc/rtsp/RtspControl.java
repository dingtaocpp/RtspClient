package edu.tfnrc.rtsp;

import android.util.Log;
import edu.tfnrc.rtsp.api.EntityMessage;
import edu.tfnrc.rtsp.api.Request;
import edu.tfnrc.rtsp.api.RequestListener;
import edu.tfnrc.rtsp.api.Response;
import edu.tfnrc.rtsp.message.RtspDescriptor;
import edu.tfnrc.rtsp.message.RtspMedia;
import edu.tfnrc.rtsp.transport.TCPTransport;

import java.net.URI;

/**
 * Created by leip on 2015/12/3.
 */
public class RtspControl implements RequestListener{

    private final String TAG = "RtspControl";

    private RtspClient client;

    private boolean connected = false;

    private URI uri;

    private int port;

    private String resource;

    // reference to the SDP file returned as a response
    // to a DESCRIBE request
    private RtspDescriptor rtspDescriptor;

    private int state;

    /**
     * This constructor is invoked with an uri that
     * describes the server uri and also a certain
     * resource
     */

    public RtspControl(String uri){

        int pos = uri.lastIndexOf("/");

        try{
            this.uri = new URI(uri.substring(0, pos));
            this.resource = uri.substring(pos + 1);

            this.client = new RtspClient();
            this.client.setTransport(new TCPTransport());
            this.client.setRequestListener(this);

            this.state = RtspConstants.UNDEFINED;

            this.client.options("*", this.uri);
        }catch (Exception e){
            if(this.client != null)
                onError(this.client, e);
            else
                e.printStackTrace();
        }
    }

    public RtspControl(String uri, String resource){

        try{
            this.uri = new URI(uri);
            this.resource = resource;

            Log.d(TAG, "new RtspClient");

            this.client = new RtspClient();
            this.client.setTransport(new TCPTransport());
            this.client.setRequestListener(this);

            this.state = RtspConstants.UNDEFINED;

            this.client.options("*", this.uri);
        } catch (Exception e){
            if(this.client != null){
                Log.e(TAG, "client is null");
                onError(this.client, e);
            }else {
                e.printStackTrace();
            }
        }
    }

    public void play() {

        if ((this.client == null) || !this.connected) return;

        if (this.state == RtspConstants.READY) {
            this.client.play();
        }
    }

    public void pause() {

        if ((this.client == null) || !this.connected) return;

        if (this.state == RtspConstants.PLAYING) {
            this.client.pause();
        }
    }

    public void stop() {

        if ((this.client == null) || !this.connected) return;

        // send TEARDOWN request
        this.client.teardown();
    }

    public boolean isConnected() {
        return this.connected;
    }

    public int getState() {
        return this.state;
    }

    public RtspClient getClient(){
        return this.client;
    }

    public int getClientPort() {
        return this.port;
    }

    public RtspDescriptor getDescriptor() {
        return this.rtspDescriptor;
    }

    //TODO:parameters are never used
    public void onError(RtspClient client, Throwable error) {

        if ((this.client != null) && this.connected) {
            this.client.teardown();
        }

        this.state = RtspConstants.UNDEFINED;
        this.connected = false;

        this.client = null;

    }

    // register SDP file
    public void onDescriptor(RtspClient client, String descriptor) {
        this.rtspDescriptor = new RtspDescriptor(descriptor);
    }

    public void onFailure(RtspClient client, Request request, Throwable cause) {

        if ((this.client != null) && (this.connected == true)) {
            this.client.teardown();
        }

        this.state = RtspConstants.UNDEFINED;
        this.connected = false;

        this.client = null;

    }

    public void onSuccess(RtspClient client, Request request, Response response){

        try {
            Log.d(TAG, "onSuccess");

            if((this.client != null) && (response.getStatusCode() == 200)) {

                Log.d(TAG, "state code= 200");

                Request.Method method = request.getMethod();
                if (method == Request.Method.OPTIONS) {

                    this.connected = true;

                    this.client.describe(this.uri, this.resource);
                } else if (method == Request.Method.DESCRIBE) {
                    //set state to INIT
                    this.state = RtspConstants.INIT;

                    /*
					 * onSuccess is called AFTER onDescriptor method;
					 * this implies, that a media resource is present
					 * with a certain client port specified by the RTSP
					 * server
					 */
                    RtspMedia video = this.rtspDescriptor.getFirstVideo();
                    if (video != null) {
                        this.port = Integer.valueOf(video.getTransportPort());

                        //debug
                        Log.d(TAG, "video port= " + this.port);

                        //send SETUP request
                        this.client.setup(this.uri, this.port, this.resource);
                    }
                } else if (method == Request.Method.SETUP) {

                    this.state = RtspConstants.READY;

                } else if (method == Request.Method.PLAY) {

                    this.state = RtspConstants.PLAYING;

                } else if (method == Request.Method.PAUSE) {

                    this.state = RtspConstants.READY;
                } else if (method == Request.Method.TEARDOWN) {

                    this.connected = false;

                    this.state = RtspConstants.UNDEFINED;
                }

            } else {    //StateCode is not 200

            }
        } catch (Exception e){
            onError(this.client, e);
        }
    }

    //debug
    public void describe(){
        if ((this.client == null) || !this.connected) return;

        this.client.describe(uri, resource);
    }

    public void options(){
        if ((this.client == null) || !this.connected) return;

        this.client.options("*", uri);
    }

    public void setup(){
        if ((this.client == null) || !this.connected) return;

        this.client.setup(uri, 8080);
    }
}
