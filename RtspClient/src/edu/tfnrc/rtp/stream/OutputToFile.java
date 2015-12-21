package edu.tfnrc.rtp.stream;

import android.os.Environment;
import android.util.Log;
import edu.tfnrc.rtp.util.Buffer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by leip on 2015/12/16.
 */
public class OutputToFile extends Thread implements ProcessorOutputStream {

    private static final String TAG = "OutputToFile";

    private String filePath;

    private File file = null;
    private OutputStream output = null;

    private Buffer buffer = null;
    public OutputToFile(String path){
        super();
        filePath = Environment.getExternalStorageDirectory() + "/" + path;
    }

    @Override
    public void open() throws Exception {
        //Create directionary
        File dir = new File(filePath);
        if(!dir.exists())
            dir.mkdir();
    }

    @Override
    public void close() {
       /* try {
            if (output != null)
                output.close();
        } catch (Exception e){
            e.printStackTrace();
        }*/
    }

    @Override
    public void write(Buffer buffer) throws Exception {
        this.buffer = buffer;
        start();


    }

    @Override
    public void run() {

        if(buffer == null) return;
        try{
            Log.d(TAG, "start writing");

            file = new File(filePath + "/" + buffer.getTimeStamp());
            output = new FileOutputStream(file);

            output.write((byte[])buffer.getData());
            output.flush();
        } catch (Exception e){
            Log.e(TAG, "failed to write buffer to file", e);
        }
    }
}
