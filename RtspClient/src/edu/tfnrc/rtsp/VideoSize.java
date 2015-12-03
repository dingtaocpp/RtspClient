package edu.tfnrc.rtsp;

/**
 * Created by leip on 2015/11/27.
 */
public class VideoSize {

    private int width;
    private int height;

    public VideoSize(){
        width = 1280;
        height = 720;
    }
    public VideoSize(int width, int height){
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }
    public void setVideoSize(int width, int height){
        this.width = width;
        this.height = height;
    }
}
