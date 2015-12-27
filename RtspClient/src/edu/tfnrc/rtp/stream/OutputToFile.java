package edu.tfnrc.rtp.stream;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import edu.tfnrc.rtp.util.Buffer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by leip on 2015/12/16.
 */
public class OutputToFile implements Runnable , ProcessorOutputStream {

    private static final String TAG = "OutputToFile";

    private ExecutorService pool = Executors.newCachedThreadPool();

    private boolean opened = false;

    private String filePath;

    private Buffer buffer = null;


    public OutputToFile(String path){
        super();

        filePath = Environment.getExternalStorageDirectory() + "/" + path;
    }

    private OutputToFile(String path, Buffer buffer){
        filePath = path;
        this.buffer = buffer;
    }
    @Override
    public void open() throws Exception {
        if(!opened) {
            //Create directory
            File dir = new File(filePath);
            if (!dir.exists())
                dir.mkdir();
            opened = true;
        }
    }

    @Override
    public void close() {
       pool.shutdown();
    }

    @Override
    public void write(Buffer buffer) throws Exception {
        this.buffer = buffer;

        pool.submit(new OutputToFile(filePath, this.buffer));

    }


    @Override
    public void run() {

        if(buffer == null) return;


        try{
            Log.d(TAG, "start writing");

            File file = new File(filePath + "/" + buffer.getTimeStamp() + buffer.getSequenceNumber());
            OutputStream output = new FileOutputStream(file);

            output.write((byte[])buffer.getData(), 0, buffer.getLength());
            output.flush();
        } catch (Exception e){
            Log.e(TAG, "failed to write buffer to file", e);
            if(buffer.getData() == null)
                Log.e(TAG, "data is null");
        }

    }
}
