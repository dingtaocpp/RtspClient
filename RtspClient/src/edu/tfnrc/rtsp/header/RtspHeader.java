package edu.tfnrc.rtsp.header;

import android.util.Log;

/**
 *
 *
 * Created by leip on 2015/11/25.
 */
public class RtspHeader {

    private String name;
    private String value;

    /**
     * Constructs a new header.
     *
     * @param header
     *          if the character ':' (colon) is not found, it will be the name of
     *          the header. Otherwise, this constructor parses the header line.
     */
    public RtspHeader(String header){
        int colon = header.indexOf(':');
        if (colon == -1) {
            name = header;
        }
        else {
            name = header.substring(0, colon);
            value = header.substring(++colon).trim();   //除去字符串开头和末尾的空格或其他字符

        }
    }

    public RtspHeader(String name, String value){
        this.name = name;
        this.value = value;
    }

    public String getName(){
        return name;
    }

    public String getRawValue(){
        return value;
    }

    public void setRawValue(String value){
        this.value = value;
    }

    public String toString(){
        return name + ": " + value;     //TODO: :后是否加空格
    }

    //两个RtspHeader判等
    public boolean equals(Object obj){

        if(super.equals(obj))//指向同一对象，或name相同即相等
            return true;
        if(obj instanceof String)
            return getName().equals(obj);
        if(obj instanceof RtspHeader){
            return getName().equals(((RtspHeader)obj).getName());
        }
        return false;
    }

    protected final void checkName(String expected) throws Exception{
        if(expected.equalsIgnoreCase(getName()) == false)       //忽略大小写比较字符串
            throw new Exception("[Header Mismatch] - Expexted: " + expected + " " +
                    "Retrieved: " + getName());
    }

    protected final void setName(String name){
        this.value = this.name;     //TODO:待解释
        this.name = name;
    }

}
