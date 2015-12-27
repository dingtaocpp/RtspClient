package edu.tfnrc.rtsp.message;

import android.util.Log;
import edu.tfnrc.rtsp.RtspConstants;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by leip on 2015/11/27.
 */
public class RtspDescriptor {

    private static final String TAG = "RtspDescriptor";

    public static final String SEP = "\r\n";

    private ArrayList<RtspMedia> mediaList;

    public RtspDescriptor(String descriptor){

        mediaList = new ArrayList<RtspMedia>();

        RtspMedia mediaItem = null;

        try {
            //将descriptor分割，分隔符为"\r\n"
            StringTokenizer tokenizer = new StringTokenizer(descriptor, SEP);
            while(tokenizer.hasMoreTokens()){
                String token = tokenizer.nextToken();

                if(token.startsWith("m=")){

                    Log.i(TAG, "new media item: " + token);
                    //发现新的媒体项   a new media item is detected
                    mediaItem = new RtspMedia(token);
                    mediaList.add(mediaItem);
                } else if(token.startsWith("a=") && mediaItem != null){
                    Log.i(TAG, token);

                    mediaItem.setAttribute(token);
                }
            }
        }catch (Exception e){
            Log.e(TAG, "token error", e);
        }

        Log.i(TAG, "media list length: " + mediaList.size());
    }

    public ArrayList<RtspMedia> getMediaList() {
        return mediaList;
    }
    //获取列表中最靠前的视频（非音频）
    public RtspMedia getFirstVideo(){
        RtspMedia video = null;
        for(RtspMedia mediaItem : this.mediaList){
            if(mediaItem.getMediaType().equals(RtspConstants.SDP_VIDEO_TYPE)){
                video = mediaItem;
                break;
            }
        }
        return video;
    }
}
