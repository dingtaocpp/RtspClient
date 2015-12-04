package edu.tfnrc.rtsp.transport;

import edu.tfnrc.rtsp.api.Message;
import edu.tfnrc.rtsp.api.Transport;
import edu.tfnrc.rtsp.api.TransportListener;

/**
 * Created by leip on 2015/12/2.
 */
public class TCPTransportListener implements TransportListener{

    private final TransportListener behaviour;

    public TCPTransportListener(TransportListener theBehaviour){
        behaviour = theBehaviour;
    }

    //进行连接
    public void connected(Transport t){
        if(behaviour != null)
        {
            try{
                behaviour.connected(t);
            }catch(Throwable error){
                behaviour.error(t, error);
            }
        }
    }

    public void dataReceived(Transport t, byte[] data, int size){

        if(behaviour != null){
            try{
                behaviour.dataReceived(t, data, size);
            } catch(Throwable error){
                behaviour.error(t, error);
            }
        }
    }

    @Override
    public void dataSent(Transport t) {

        if(behaviour != null){
            try{
                behaviour.dataSent(t);
            }catch (Throwable error){
                behaviour.error(t, error);
            }
        }
    }

    public void error(Transport t, Throwable error){
        if(behaviour != null)
            behaviour.error(t, error);
    }

    public void error(Transport t, Message message, Throwable error){
        if(behaviour != null)
            behaviour.error(t, message, error);
    }

    public void remoteDisconnection(Transport t) {
        if (behaviour != null)
            try {
                behaviour.remoteDisconnection(t);

            } catch(Throwable error) {
                behaviour.error(t, error);
            }

    }


}
