package edu.tfnrc.rtp.stream;

import edu.tfnrc.rtp.util.Buffer;

/**
 * Created by leip on 2015/12/16.
 */
public interface ProcessorOutputStream {

    /**
     * Open the output stream
     *
     * @throws Exception
     */
    public void open() throws Exception;

    /**
     * Close from the output stream
     */
    public void close();

    /**
     * Write to the stream without blocking
     *
     * @param buffer Input buffer
     * @throws Exception
     */
    public void write(Buffer buffer) throws Exception;
}
