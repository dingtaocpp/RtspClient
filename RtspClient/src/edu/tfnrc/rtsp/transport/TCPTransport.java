package edu.tfnrc.rtsp.transport;

import android.util.Log;
import edu.tfnrc.rtsp.api.Message;
import edu.tfnrc.rtsp.api.Transport;
import edu.tfnrc.rtsp.api.TransportListener;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;

/**
 * Created by leip on 2015/12/2.
 */

class TCPTransportThread extends Thread{

    private static final int TCP_BUFFER_SIZE = 2048;

    private final TCPTransport transport;

    private volatile TCPTransportListener listener;

    public TCPTransportThread(TCPTransport transport, TransportListener listener){
        this.transport = transport;
        this.listener = new TCPTransportListener(listener);
    }

    public TCPTransportListener getListener() {
        return listener;
    }

    public void setListener(TransportListener listener) {
        this.listener = new TCPTransportListener(listener);
    }

    public void run(){

        listener.connected(transport);

        byte[] buffer = new byte[TCP_BUFFER_SIZE];

        int read = -1;
        while(transport.isConnected()){

            try{
                read = transport.receive(buffer);
                if(read == -1){
                    transport.setConnected(false);
                    listener.remoteDisconnection(transport);
                }else
                    listener.dataReceived(transport, buffer, read);
            }catch (IOException e){
                listener.error(transport, e);
            }
        }
    }
}

public class TCPTransport implements Transport{

    private final String TAG = "TCPTransport";

    private Socket socket;

    private TCPTransportThread thread;
    private TransportListener transportListener;

    private volatile boolean connected;

    public TCPTransport(){
    }

    public void connect(URI to) throws IOException{
        if(connected)
            throw new IllegalStateException("Socket is still open. Close it first.");

        int port = to.getPort();


        if(port == -1) port = 6880;

        socket = new Socket(to.getHost(), port);
        setConnected(true);
        thread = new TCPTransportThread(this, transportListener);
        thread.start();
    }

    public void setConnected(boolean connected){
        this.connected = connected;
    }

    public void disconnect(){
        setConnected(false);
        try{
            socket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public boolean isConnected(){
        return connected;
    }

    public synchronized void sendMessage(Message message) throws Exception{

        socket.getOutputStream().write(message.getBytes());
        thread.getListener().dataSent(this);
    }

    public void setTransportListener(TransportListener listener){
        transportListener = listener;
        if(thread != null)
            thread.setListener(listener);
    }

    public void setUserData(Object data){}

    int receive(byte[] data) throws IOException{
        return socket.getInputStream().read(data);
    }
}
