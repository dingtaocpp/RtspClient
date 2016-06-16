package edu.tfnrc.rtsp.message;

import android.util.Log;
import edu.tfnrc.rtsp.response.RtspResponse;
import edu.tfnrc.rtsp.api.Message;
import edu.tfnrc.rtsp.api.MessageFactory;
import edu.tfnrc.rtsp.api.Response;
import edu.tfnrc.rtsp.header.*;
import edu.tfnrc.rtsp.request.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by leip on 2015/11/30.
 */
public class RtspMessageFactory implements MessageFactory{

    //Header缓存池
    private static Map<String, Constructor<? extends RtspHeader>> headerMap;
    //Request缓存池
    private static Map<RtspRequest.Method, Class<? extends RtspRequest>> requestMap;

    static {
        headerMap = new HashMap<String, Constructor<? extends RtspHeader>>();
        requestMap = new HashMap<RtspRequest.Method, Class<? extends RtspRequest>>();

        //提前放入头信息类,请求类
        try{
            putHeader(ContentEncodingHeader.class); //TODO: Encoding问题
            putHeader(ContentLengthHeader.class);
            putHeader(ContentTypeHeader.class);
            putHeader(CSeqHeader.class);
            putHeader(SessionHeader.class);
            putHeader(TransportHeader.class);

            requestMap.put(RtspRequest.Method.OPTIONS, RtspOptionsRequest.class);
            requestMap.put(RtspRequest.Method.SETUP, 	RtspSetupRequest.class);
            requestMap.put(RtspRequest.Method.TEARDOWN, RtspTeardownRequest.class);
            requestMap.put(RtspRequest.Method.DESCRIBE, RtspDescribeRequest.class);
            requestMap.put(RtspRequest.Method.PLAY, 	RtspPlayRequest.class);
            requestMap.put(RtspRequest.Method.PAUSE, 	RtspPauseRequest.class);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
        private static void putHeader(Class<? extends RtspHeader> cls) throws Exception{
        headerMap.put(cls.getDeclaredField("NAME").get(null).toString().toLowerCase(), cls.getConstructor(String.class));

    }

    /*
    * This method handles RTSP server responses
    * */
    public void incomingMessage(MessageBuffer buffer) throws Exception{

        //read a message bytes data in buffer
        ByteArrayInputStream in = new ByteArrayInputStream(buffer.getData(), buffer.getOffset(), buffer.getLength());

        int initial = in.available();
        Message message = null;

        try{
            String line = readLine(in);
            if(line.startsWith(Message.RTSP_TOKEN)) {
                message = new RtspResponse(line);
            } else {
                RtspRequest.Method method = null;
                try{
                    method = RtspRequest.Method.valueOf(line.substring(0, line.indexOf(' ')));
                } catch (IllegalArgumentException ilae){

                }
                //从Map中取出相应类型的Request信息
                Class<? extends RtspRequest> cls = requestMap.get(method);
                if(cls != null)
                    message = cls.getConstructor(String.class).newInstance(line);
                else
                    message = new RtspRequest(line);
            }

            while(true){
                line = readLine(in);
                if(in == null)
                    throw new Exception();
                if(line.length() == 0)
                    break;
                Constructor<? extends RtspHeader> c = headerMap.get(line.substring(0,
                        line.indexOf(':')).toLowerCase());
                if(c != null)
                    message.addHeader(c.newInstance(line));
                else
                    message.addHeader(new RtspHeader(line));
            }
            buffer.setMessage(message);

            try{
                int length = ((ContentLengthHeader) message
                        .getHeader(ContentLengthHeader.NAME)).getValue();
                if(in.available() < length)
                    throw new Exception();
                RtspContent content = new RtspContent();
                content.setDescription(message);
                byte[] data = new byte[length];
                in.read(data);
                content.setBytes(data);
                message.setEntityMessage(new RtspEntityMessage(message, content));
            } catch(Exception e)
            {   e.printStackTrace();
            }
        } catch (Exception e){
            throw new Exception(e);
        } finally
        {
            //TODO:读取后更新buffer状态
            buffer.setused(initial - in.available());
            //buffer.discardData();

            try{
                in.close();
            }catch (IOException e)
            { e.printStackTrace();
            }
        }
    }

    public RtspRequest outgoingRequest(String uri, RtspRequest.Method method, int cseq, RtspHeader... extras) throws URISyntaxException{

        Class<? extends RtspRequest> cls = requestMap.get(method);
        RtspRequest message = null;

        try{
            message = cls != null ? cls.newInstance() : new RtspRequest();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        message.setLine(method, uri);
        fillMessage(message, cseq, extras);

        return message;
    }

    public RtspRequest outgoingRequest(RtspContent body, String uri, RtspRequest.Method method, int cseq, RtspHeader... extras) throws URISyntaxException{
        Message message = outgoingRequest(uri, method, cseq, extras);
        return (RtspRequest) message.setEntityMessage(new RtspEntityMessage(message, body));
    }

    public Response outgoingResponse(int code, String text, int cseq, RtspHeader... extras){

        RtspResponse message = new RtspResponse();
        message.setLine(code, text);

        fillMessage(message, cseq, extras);
        return message;
    }

    public Response outgoingResponse(RtspContent body, int code, String text, int cseq, RtspHeader... extras){

        Message message = outgoingResponse(code, text, cseq, extras);
        return (Response) message.setEntityMessage(new RtspEntityMessage(message, body));
    }

    private void fillMessage(Message message, int cseq, RtspHeader[] extras){

        message.addHeader(new CSeqHeader(cseq));
        for(RtspHeader h : extras){
            message.addHeader(h);
        }
    }

    private String readLine(InputStream in) throws IOException{

        int ch = 0;

        StringBuilder b = new StringBuilder();
        for(ch = in.read(); ch != -1 && ch != 0x0d && ch != 0x0a; ch = in.read())
            b.append((char) ch);
        if(ch == -1)
            return null;

        in.read();
        return b.toString();
    }
}
