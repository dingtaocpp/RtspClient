package edu.tfnrc.rtsp.api;

import edu.tfnrc.rtsp.RtspClient;

import java.net.URISyntaxException;

/**
 * C->S发送的请求消息
 * Created by leip on 2015/11/26.
 */
public interface Request extends Message {

    //根据服务端定义命令位置相同
    enum Method{
        DESCRIBE, ANNOUNCE, GETPARAMETERS, OPTIONS, PAUSE, PLAY,
        RECORD, REDIRECT, SETUP, SERPARAMETERS, TEARDOWN
    };      //default包权限，

    public void setLine(Method method, String uri) throws URISyntaxException;

    public Method getMethod();

    public String getURI();

    public void handleResponse(RtspClient client, Response response);

}
