//package edu.tfnrc.rtp.stream;
//
//import android.graphics.Bitmap;
//import edu.tfnrc.rtp.util.Buffer;
//import edu.tfnrc.rtsp.RtspConstants;
//import edu.tfnrc.rtsp.VideoSize;
//
///**
// * Created by leip on 2016/1/11.
// */
//public class VideoOutput extends MediaOutput {
//
//    /**
//     * Video frame
//     */
//    private int decodedFrame[];
//
//    /**
//     * Bitmap frame
//     */
//    private Bitmap rgbFrame;
//
//    VideoSize videoSize = RtspConstants.getVideoSize();
//
//    public VideoOutput(){
//        decodedFrame = new int[videoSize.getWidth() * videoSize.getHeight()];
//        rgbFrame     = Bitmap.createBitmap(videoSize.getWidth(), videoSize.getHeight(), Bitmap.Config.ARGB_8888);
//    }
//    @Override
//    public void open() throws Exception {
//        super.open();
//    }
//
//    @Override
//    public void write(Buffer buffer) throws Exception {
//        super.write(buffer);
//    }
//
//    @Override
//    public void close() {
//        super.close();
//    }
//}
