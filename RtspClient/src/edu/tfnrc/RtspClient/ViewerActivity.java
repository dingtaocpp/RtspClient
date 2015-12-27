package edu.tfnrc.RtspClient;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import edu.tfnrc.rtp.RtpVideoRenderer;
import edu.tfnrc.rtsp.RtspControl;
import edu.tfnrc.rtsp.RtspControlTest;

import java.lang.ref.WeakReference;

public class ViewerActivity extends Activity implements View.OnClickListener {

    //debug
    private static final String TAG = "ViewerActivity";

    private Button buttonStart, buttonStop;

    public TextView requestView, responseView;

    private RtpVideoRenderer incomingRenderer = null;

    private String uri = "rtsp://192.168.2.1:6880/";

    private String resource = "test.264";



    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        requestView = (TextView)findViewById(R.id.requestView);
        responseView = (TextView)findViewById(R.id.responseView);
//        controlTest = new RtspControl(uri, resource);
//        controlTest.getClient().setHandler(handler);

        buttonStart  = (Button)findViewById(R.id.buttonStart);
        buttonStop   = (Button)findViewById(R.id.buttonStop);

        buttonStop.setOnClickListener(this);
        buttonStart.setOnClickListener(this);

        Log.d(TAG, "on Create");
        //有木有wifi
        if(((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE))
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() != NetworkInfo.State.CONNECTED) {
            Toast.makeText(this, "请连接WIFI", Toast.LENGTH_LONG).show();
        }
        else {
            new Thread() {
                @Override
                public void run() {
                    try {
                        incomingRenderer = new RtpVideoRenderer(uri, handler);
                        Log.d(TAG, "new Renderer");
                    } catch (Exception e) {
                        Log.e(TAG, "fail to new Renderer", e);

                    }
                }
            }.start();
        }
    }

    Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg){
            if(msg.what == 0)
                requestView.setText((String)msg.obj);
            else if(msg.what == 1)
                responseView.setText((String)msg.obj);
        }
    };

    @Override
    public void onClick(View v) {
        Log.d("Button", "Button clicked");
        switch (v.getId()){
            case R.id.buttonStart:
                incomingRenderer.open();
                incomingRenderer.start();
                break;
            case R.id.buttonStop:
                incomingRenderer.stop();
                incomingRenderer.close();
                break;

            default:
                Log.e("Button", "Error Button");
        }
    }

//    class ClientTest implements Runnable{
//
//        private int viewId;
//        public ClientTest(int id){
//            viewId = id;
//        }
//
//        public void run(){
//            switch(viewId){
//
//                case R.id.buttonStart:
//                    incomingRenderer.open();
//                    incomingRenderer.start();
//                    break;
//                case R.id.buttonStop:
//                    incomingRenderer.stop();
//                    incomingRenderer.close();
//                    break;
//                default:
//                    Log.e("Button", "Error Button");
//
//            }
//        }
//    }


    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");

        super.onDestroy();

        if(incomingRenderer != null) {
            incomingRenderer.stop();
            incomingRenderer.close();
        }
    }
}
