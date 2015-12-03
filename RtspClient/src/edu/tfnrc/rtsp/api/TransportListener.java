package edu.tfnrc.rtsp.api;

/**
 * Listener for transport events. Implementations of {@link Transport}, when
 * calling a listener method, must catch all errors and submit them to the
 * error() method.
 *
 * Created by leip on 2015/11/26.
 */
public interface TransportListener {

    public void connected(Transport t) throws Throwable;

    public void error(Transport t, Throwable error);

    public void error(Transport t, Message message, Throwable error);

    public void remoteDisconnection(Transport t) throws Throwable;

    public void dataReceived(Transport t, byte[] data, int size) throws Throwable;

    public void dataSent(Transport t) throws Throwable;
}
