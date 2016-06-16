package edu.tfnrc.rtsp.api;

import java.io.IOException;
import java.net.URI;

/**
 * 定义连接协议（TCP，UDP）。
 * 实现命令队列应对连接繁忙
 * Created by leip on 2015/11/26.
 */
public interface Transport {
    public void connect(URI to) throws IOException;

    public void disconnect();

    public void sendMessage(Message message) throws Exception;

    public void setTransportListener(TransportListener listener);

    public void setUserData(Object data);

    public boolean isConnected();
}
