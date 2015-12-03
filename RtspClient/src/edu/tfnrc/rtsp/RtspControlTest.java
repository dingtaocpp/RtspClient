package edu.tfnrc.rtsp;

import android.os.Handler;
import edu.tfnrc.rtsp.api.Request;
import edu.tfnrc.rtsp.api.RequestListener;
import edu.tfnrc.rtsp.api.Response;
import edu.tfnrc.rtsp.transport.TCPTransport;

import java.net.URI;

/**
 *
 * 测试各步骤所发送信息
 * Created by leip on 2015/12/2.
 */
public class RtspControlTest implements RequestListener{

    private RtspClient client;

    private URI uri;

    private int port;

    private String resource;

    private boolean connected = true;   //测试时为true，实际默认为false

    private int state;


    public RtspControlTest(String uri, String resource, Handler handler){

        try{

            this.uri = new URI(uri);
            this.resource = resource;

            this.client = new RtspClient();
            this.client.setTransport(new TCPTransport());

            this.client.setHandler(handler);    //debug

            this.client.setRequestListener(this);
            this.state = RtspConstants.READY;

            // the OPTIONS request is used to invoke and
            // test the connection to the RTSP server,
            // specified with the URI provided
//            this.client.options("*", this.uri); TODO:待还原
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void play(){
        if ((this.client == null) || (this.connected == false)) return;

        if(this.state == RtspConstants.READY){
            this.client.play();
        }
    }

    public void pause(){
        if ((this.client == null) || (this.connected == false)) return;

        if(this.state == RtspConstants.READY){
            this.client.pause();
        }
    }

    public void stop(){
        if ((this.client == null) || (this.connected == false)) return;

        this.client.teardown();
    }

    public boolean isConnected() {
        return this.connected;
    }

    public int getState() {
        return this.state;
    }

    public int getClientPort() {
        return this.port;
    }

    public void onError(RtspClient client, Throwable error) {
        error.printStackTrace();
    }

    public void onDescriptor(RtspClient client, String descriptor) {
    }

    public void onFailure(RtspClient client, Request request, Throwable cause){
    }

    public void onSuccess(RtspClient client, Request request, Response response){

    }

    //debug
    public void describe(){
        if ((this.client == null) || (this.connected == false)) return;

        this.client.describe(uri, resource);
    }

    public void options(){
        if ((this.client == null) || (this.connected == false)) return;

        this.client.options("*", uri);
    }

    public void setup(){
        if ((this.client == null) || (this.connected == false)) return;

        this.client.setup(uri, 8080);
    }
}
