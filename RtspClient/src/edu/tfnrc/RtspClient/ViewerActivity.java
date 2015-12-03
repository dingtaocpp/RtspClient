package edu.tfnrc.RtspClient;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import edu.tfnrc.rtsp.RtspControlTest;

public class ViewerActivity extends Activity implements View.OnClickListener {

    private Button buttonSetup, buttonOptions, buttonDescribe,
            buttonPlay, buttonPause, buttonStop;

    private TextView textView;

    private RtspControlTest controlTest;

    private String uri = "192.168.0.1";

    private String resource = "test.264";



    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        textView = (TextView)findViewById(R.id.textView);
        controlTest = new RtspControlTest(uri, resource, handler);

        buttonDescribe  = (Button)findViewById(R.id.buttonDescribe);
        buttonOptions   = (Button)findViewById(R.id.buttonOptions);
        buttonSetup     = (Button)findViewById(R.id.buttonSetup);
        buttonPlay      = (Button)findViewById(R.id.buttonPlay);
        buttonPause     = (Button)findViewById(R.id.buttonPause);
        buttonStop      = (Button)findViewById(R.id.buttonStop);

        buttonDescribe.setOnClickListener(this);
        buttonOptions.setOnClickListener(this);
        buttonSetup.setOnClickListener(this);
        buttonPlay.setOnClickListener(this);
        buttonPause.setOnClickListener(this);
        buttonStop.setOnClickListener(this);

        Log.d("Button", "Button find");

    }

    private final Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg){
            textView.setText((String)msg.obj);
        }
    };

    @Override
    public void onClick(View v) {
        Log.d("Button", "Button clicked");
        new Thread(new ControlTest(v.getId())).start();
    }

    class ControlTest implements Runnable{

        private int viewId;
        public ControlTest(int id){
            viewId = id;
        }

        public void run(){
            switch(viewId){
                case R.id.buttonDescribe:
                    controlTest.describe();
                    Log.d("Button", "describe button");
                    break;
                case R.id.buttonOptions:
                    controlTest.options();
                    Log.d("Button", "options button");
                    break;
                case R.id.buttonSetup:
                    controlTest.setup();
                    Log.d("Button", "setup button");
                    break;
                case R.id.buttonPlay:
                    controlTest.play();
                    break;
                case R.id.buttonPause:
                    controlTest.pause();
                    break;
                case R.id.buttonStop:
                    controlTest.stop();
                    break;
                default:
                    Log.e("Button", "Error Button");

            }
        }
    }
}
