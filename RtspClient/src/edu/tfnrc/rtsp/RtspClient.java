package edu.tfnrc.rtsp;

import android.os.Handler;
import android.util.Log;
import edu.tfnrc.rtsp.api.*;
import edu.tfnrc.rtsp.header.RtspHeader;
import edu.tfnrc.rtsp.header.SessionHeader;
import edu.tfnrc.rtsp.header.TransportHeader;
import edu.tfnrc.rtsp.message.MessageBuffer;
import edu.tfnrc.rtsp.message.RtspMessageFactory;
import edu.tfnrc.rtsp.request.RtspOptionsRequest;
import edu.tfnrc.rtsp.request.RtspRequest;
import edu.tfnrc.rtsp.response.RtspResponse;

import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by leip on 2015/11/26.
 */
public class RtspClient implements TransportListener {

    //Debug
    private Handler mhandler;
    //Debug

    private static final String TAG = "RtspClient";

    private Transport transport;

    private MessageFactory messageFactory;

    private MessageBuffer messageBuffer;

    private volatile int cseq;

    private SessionHeader session;

    /**
     * URI kept from last setup.
     */
    private URI uri;

    private Map<Integer, RtspRequest> outstanding;

    private RequestListener clientListener;

    public RtspClient(){

        cseq = 0;

        messageFactory = new RtspMessageFactory();
        messageBuffer = new MessageBuffer();

        outstanding = new HashMap<Integer, RtspRequest>();
    }

    public Transport getTransport() {
        return transport;
    }

    public void setSession(SessionHeader session) {
        this.session = session;
    }

    public MessageFactory getMessageFactory() {
        return messageFactory;
    }

    public URI getUri() {
        return uri;
    }

    private int nextCSeq(){
        return cseq++;
    }
    public void options(String uri, URI endpoint){
        Log.d(TAG, "options");
        Log.i(TAG, "uri= " + endpoint.toString());

        try{
            RtspOptionsRequest message = (RtspOptionsRequest)messageFactory.outgoingRequest(uri, Request.Method.OPTIONS, nextCSeq());
            send(message, endpoint);

            Log.i(TAG, message.toString());
        } catch (Exception e){
            Log.e(TAG, e.toString());
            if(clientListener != null)
                clientListener.onError(this, e);
        }
    }

    public void play(){
        Log.d(TAG, "play");

        try{
            RtspRequest message = (RtspRequest)messageFactory.outgoingRequest(uri.toString(), Request.Method.PLAY, nextCSeq(), session);
            send(message);

            Log.i(TAG, message.toString());
        }catch (Exception e){
            if(clientListener != null)
                clientListener.onError(this, e);
        }
    }

    public void pause() {
        Log.d(TAG, "pause");


        try{
            RtspRequest message = (RtspRequest)messageFactory.outgoingRequest(uri.toString(), Request.Method.PAUSE, nextCSeq(), session);
            send(message);

        }catch (Exception e){

            if(clientListener != null)
                clientListener.onError(this, e);
        }
    }

    /*TODO: record方法*/
    public void record() throws IOException{
        throw new UnsupportedOperationException("Recording is not supported in current version");
    }

    public void setRequestListener(RequestListener listener){
        clientListener = listener;
    }

    public RequestListener getRequestListener(){
        return clientListener;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
        transport.setTransportListener(this);
    }

    public void describe(URI uri, String resource){
        Log.d(TAG, "decribe");

        this.uri = uri;
        String finalURI = uri.toString();
        if((resource != null) && !(resource.equals("*")))
            finalURI += '/' + resource;

        try{
            RtspRequest message = (RtspRequest)messageFactory.outgoingRequest(finalURI, Request.Method.DESCRIBE, nextCSeq(), new RtspHeader("Accept", "application/sdp"));
            send(message);

            Log.i(TAG, message.toString());
        }catch (Exception e){
            if(clientListener != null)
                clientListener.onError(this, e);
        }
    }

    public void setup(URI uri, int localPort){
        Log.d(TAG, "setup");

        this.uri = uri;

        RtspRequest message = null;
        TransportHeader header = null;
        try{
            String portParam = "client_port=" + localPort + "-" + (1 + localPort);
            header = new TransportHeader(TransportHeader.LowerTransport.DEFAULT, "unicast", portParam);

            message = (RtspRequest)getSetup(uri.toString(), localPort, header, session);
            send(message);

        } catch (Exception e){
            if(clientListener != null)
                clientListener.onError(this, e);
        }
    }

    public void setup(URI uri, int localPort, String resource){

        this.uri = uri;
        try{
            String portParam = "client_port=" + localPort + "-" + (1 + localPort);
            String finalURI = uri.toString();

            if((resource != null) && (resource.equals("*") == false))
                finalURI += '/' + resource;

            send(getSetup(finalURI, localPort, new TransportHeader(TransportHeader.LowerTransport.DEFAULT, "unicast", portParam), session));
        }catch (Exception e){
            if(clientListener != null)
                clientListener.onError(this, e);
        }
    }

    public void teardown(){     //TODO:无信息
        Log.d(TAG, "teardown");

        if(session == null) {
            return;
        }
        try{
            RtspRequest message = (RtspRequest)messageFactory.outgoingRequest(uri.toString(), Request.Method.TEARDOWN, nextCSeq(), session, new RtspHeader("Connection", "close"));
            send(message);

            Log.i(TAG, message.toString());
        }catch (Exception e){

            Log.e(TAG, "teardown error");
            if(clientListener != null)
                clientListener.onError(this, e);
        }
    }

    public void dataReceived(Transport t, byte[] data, int size) throws Throwable{

        messageBuffer.addData(data, size);
        while(messageBuffer.getLength() > 0){
            try{
                messageFactory.incomingMessage(messageBuffer);
                messageBuffer.discardData();
                Message message = messageBuffer.getMessage();
                if(message instanceof RtspRequest)
                    send(messageFactory.outgoingResponse(405, "Method Not Allowd",
                            message.getCSeq().getValue()));
                else{
                    RtspRequest request = null;
                    synchronized (outstanding){
                        request = outstanding.remove(message.getCSeq().getValue());
                    }
                    if(request == null) {

                        return;
                    }
                    Response response = (Response) message;
                    request.handleResponse(this, response);

                    //debug
                    print(response);

                    clientListener.onSuccess(this, request, response);
                }
            }catch (Exception e){
//                messageBuffer.discardData();
                if(clientListener != null)
                    clientListener.onError(this, e.getCause());
            }
        }

    }


    public void dataSent(Transport t) throws Throwable
    {
        Log.d(TAG, "data sent");
    }

    public void connected(Transport t)throws Throwable{

    }

    public void error(Transport t, Throwable error) {
        clientListener.onError(this, error);
    }


    public void error(Transport t, Message message, Throwable error)
    {
        clientListener.onFailure(this, (RtspRequest) message, error);
    }

    public void remoteDisconnection(Transport t) throws Throwable
    {
        synchronized(outstanding)
        {
            for(Map.Entry<Integer, RtspRequest> request : outstanding.entrySet())
                clientListener.onFailure(this, request.getValue(),
                        new SocketException("Socket has been closed"));
        }
    }

    private Request getSetup(String uri, int localPort, RtspHeader... headers) throws URISyntaxException {
        return getMessageFactory().outgoingRequest(uri, RtspRequest.Method.SETUP, nextCSeq(),
                headers);
    }

    public void send(Message message) throws Exception {
        send(message, uri);
    }

    private void send(Message message, URI endpoint) throws Exception{
        Log.d(TAG, "start send");

        if(!transport.isConnected())
            transport.connect(endpoint);
        if(message instanceof RtspRequest){
            RtspRequest request = (RtspRequest) message;
            synchronized(outstanding)
            {
                outstanding.put(message.getCSeq().getValue(), request);
            }
            try{
                transport.sendMessage(message);
            }catch (IOException e){
                clientListener.onFailure(this, request, e);
            }
        } else
            transport.sendMessage(message);

        print(message);
    }

    /*
    * 用于debug1：打印所得信息
    * */
    public void print(Message message){
        String messageString = message.toString();
        Log.i(TAG, "print message: " + messageString);
        mhandler.obtainMessage(message instanceof RtspResponse? 1 : 0 , 0, 0, messageString)
                .sendToTarget();

    }

    public void setHandler(Handler handler){
        mhandler = handler;
    }

}
