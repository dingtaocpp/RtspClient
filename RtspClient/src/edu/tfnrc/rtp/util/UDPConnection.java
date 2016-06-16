package edu.tfnrc.rtp.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * UDP connection for RTP
 *
 * Created by leip on 2015/12/8.
 */
public class UDPConnection {

    public static int DEFAULT_DATAGRAM_SIZE = 4096;

    private DatagramSocket socket = null;

    public UDPConnection(){

    }

    /**
     * Open the datagram connection
     *
     * @throws IOException
     */
    public void open() throws IOException{
        socket = new DatagramSocket();
    }

    /**
     * Open the datagram connection
     *
     * @param port Local port
     * @throws IOException
     */
    public void open(int port) throws IOException{
        socket = new DatagramSocket(port);
    }

    /**
     * Close the datagram connection
     *
     * @throws IOException
     */
    public void close() throws IOException {
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }

    /*
    * Set size of received data buffer
    * */
    public void setReceivedBufferSize(int size) throws SocketException{
        this.socket.setReceiveBufferSize(size);
    }

    /**
     * Receive data with a specific buffer size
     *
     * @param bufferSize Buffer size
     * @return Byte array
     * @throws IOException
     */
    public byte[] receive(int bufferSize) throws IOException{
        if(socket != null){
            byte[] buf = new byte[bufferSize];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
        /*TODO:    should be copied???
            int packetLength = packet.getLength();
            byte[] bytes =  packet.getData();
            byte[] data = new byte[packetLength];
            System.arraycopy(bytes, 0, data, 0, packetLength);*/
            return packet.getData();
        } else{
            throw new IOException("Connection not openned");
        }
    }

    /**
     * Receive data
     *
     * @return Byte array
     * @throws IOException
     */
    public byte[] receive() throws IOException {
        return receive(DEFAULT_DATAGRAM_SIZE);
    }

    /**
     * Returns the local address
     *
     * @return Address
     * @throws IOException
     */
    public String getLocalAddress() throws IOException {
        if (socket != null) {
            return socket.getLocalAddress().getHostAddress();
        } else {
            throw new IOException("Connection not openned");
        }
    }

    /**
     * Returns the local port
     *
     * @return Port
     * @throws IOException
     */
    public int getLocalPort() throws IOException {
        if (socket != null) {
            return socket.getLocalPort();
        } else {
            throw new IOException("Connection not openned");
        }
    }

    /**
     * Send data
     *
     * @param remoteAddr Remote address
     * @param remotePort Remote port
     * @param data Data as byte array
     * @throws IOException
     */
    public void send(String remoteAddr, int remotePort, byte[] data) throws IOException {
        if (data == null) {
            return;
        }

        if (socket != null) {
            InetAddress address = InetAddress.getByName(remoteAddr);
            DatagramPacket packet = new DatagramPacket(data, data.length, address, remotePort);
            socket.send(packet);
        } else {
            throw new IOException("Connection not openned");
        }
    }
}
