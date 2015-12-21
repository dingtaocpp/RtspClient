package edu.tfnrc.rtp.stream;

import edu.tfnrc.rtp.util.Buffer;

/**
 * Created by leip on 2015/12/16.
 */
public interface ProcessorInputStream {

    /**
     * Open the input stream
     *
     * @throws Exception
     */
    public void open() throws Exception;

    /**
     * Close the input stream
     */
    public void close();

    /**
     * Read from the input stream without blocking
     *
     * @return Buffer
     * @throws Exception
     */
    public Buffer read() throws Exception;
}
