package edu.tfnrc.rtsp.request;

import edu.tfnrc.rtsp.RtspClient;
import edu.tfnrc.rtsp.api.Message;
import edu.tfnrc.rtsp.api.Request;
import edu.tfnrc.rtsp.api.Response;
import edu.tfnrc.rtsp.message.RtspMessage;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by leip on 2015/11/30.
 */
public class RtspRequest extends RtspMessage implements Request{

    private Method method;

    private String uri;

    public RtspRequest(){

    }

    public RtspRequest(String messageLine) throws URISyntaxException{
        String[] parts = messageLine.split(" ");
        setLine(Method.valueOf(parts[0]), parts[1]);
    }

    public void setLine(Method method, String uri) throws URISyntaxException{

        this.method = method;
        this.uri = new URI(uri).toString();
        super.setLine(method.toString() + ' ' + uri + ' ' + RTSP_VERSION_TOKEN);
    }

    @Override
    public Method getMethod() {
        return method;
    }

    protected void setURI(String uri) {
        this.uri = uri;
    }

    protected void setMethod(Method method) {
        this.method = method;
    }

    public String getURI(){
        return uri;
    }

    /*
    * 处理回复共通步骤，除非请求关闭或回复关闭连接，正常情况不做任何处理
    * 之后交给子类处理
    * */
    public void handleResponse(RtspClient client, Response response){
        if(testForClose(client, this) || testForClose(client, response))
            client.getTransport().disconnect();
    }

    private boolean testForClose(RtspClient client, Message message){
        try{
            return message.getHeader("Connection").getRawValue().equalsIgnoreCase("close");
        } catch(Exception e){
            // this is an expected exception in case of no
            // connection close in the response message

        }
        return false;
    }

}
