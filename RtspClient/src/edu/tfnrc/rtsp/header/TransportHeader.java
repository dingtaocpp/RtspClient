package edu.tfnrc.rtsp.header;

import android.util.Log;

import java.util.Arrays;
import java.util.List;

/**
 * Models a "Transport" header from RFC 2326. According to specification, there may be parameters, which will be inserted as a list of strings, which follow below:
 * <code>
 parameter           =    ( "unicast" | "multicast" )   $
 |    ";" "destination" [ "=" address ]
 |    ";" "interleaved" "=" channel [ "-" channel ]     $
 |    ";" "append"
 |    ";" "ttl" "=" ttl
 |    ";" "layers" "=" 1*DIGIT
 |    ";" "port" "=" port [ "-" port ]
 |    ";" "client_port" "=" port [ "-" port ]           $
 |    ";" "server_port" "=" port [ "-" port ]
 |    ";" "ssrc" "=" ssrc
 |    ";" "mode" = <"> 1\#mode <">
 ttl                 =    1*3(DIGIT)
 port                =    1*5(DIGIT)
 ssrc                =    8*8(HEX)
 channel             =    1*3(DIGIT)
 address             =    host
 mode                =    <"> *Method <"> | Method
 </code>
 * Created by leip on 2015/11/26.
 */
public class TransportHeader extends RtspHeader {

    public static final String NAME = "Transport";

    public static enum LowerTransport {
        TCP, UDP, DEFAULT
    };

    private LowerTransport transport;   //协议类型
    private List<String> parameters;    //后续参数

    //先分析传输协议类型，再添加参数
    public TransportHeader(String header){
        super(header);
        String value = getRawValue();
        if(!value.startsWith("RTP/AVP")){   //开头格式Transport: RTP/AVP
            throw new IllegalArgumentException("Missing RTP/AVP");
        }

        int index = 7;
        if(value.charAt(index) == '/')      //RTP/AVP/(TCP||UDP);
        {
            switch(value.charAt(++index))
            {
                case 'T':
                    transport = LowerTransport.TCP;
                    break;
                case 'U':
                    transport = LowerTransport.UDP;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid Transport: "
                            + value.substring(7));
            }
            index += 3;
        } else
            transport = LowerTransport.DEFAULT;
        if(value.charAt(index) != ';' && index != value.length())
            throw new IllegalArgumentException("Parameter block expected");
        addParameters(value.substring(++index).split(";") /*取得之后所有字符串，以";"为标志分为String[]*/
                    );

    }

    public TransportHeader(LowerTransport transport, String... parameters)
    {
        super(NAME);
        this.transport = transport;
        addParameters(parameters);
    }

    public String getParameter(String part){
        for(String parameter : parameters) {
            if (parameter.startsWith(part))
                return parameter;
        }
        throw new IllegalArgumentException("No such parameter named " + part);
    }

    void addParameters(String[] parameterList){
        if(parameters == null)
            parameters = Arrays.asList(parameterList);  //数组转为链表
        else
            parameters.addAll(Arrays.asList(parameterList));
    }
    LowerTransport getTransport(){
        return transport;
    }

    public String toString(){
        StringBuffer buffer = new StringBuffer(NAME).append(": ").append("RTP/AVP");
        if(transport != LowerTransport.DEFAULT)
            buffer.append('/').append(transport);       //枚举类转字符串
        for(String parameter : parameters)
            buffer.append(';').append(parameter);
        return buffer.toString();

    }

}
