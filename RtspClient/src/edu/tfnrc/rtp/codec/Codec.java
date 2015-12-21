package edu.tfnrc.rtp.codec;

import edu.tfnrc.rtp.media.format.Format;
import edu.tfnrc.rtp.util.Buffer;

/**
 * Abstract codec
 * Created by leip on 2015/12/16.
 */
public abstract class Codec {

    /**
     * The input buffer was converted successfully to output
     */
    public static final int BUFFER_PROCESSED_OK = 0;

    /**
     * The input buffer could not be handled
     */
    public static final int BUFFER_PROCESSED_FAILED = 1 << 0;

    /**
     * The input buffer chunk was not fully consumed
     */
    public static final int INPUT_BUFFER_NOT_CONSUMED = 1 << 1;

    /**
     * The output buffer chunk was not filled
     */
    public static final int OUTPUT_BUFFER_NOT_FILLED = 1 << 2;

    /**
     * Input format
     */
    private Format inputFormat;

    /**
     * Ouput format
     */
    private Format outputFormat;

    /**
     * Set the input format
     *
     * @param input Input format
     * @return New format
     */
    public Format setInputFormat(Format input) {
        inputFormat = input;
        return input;
    }

    /**
     * Set the output format
     *
     * @param output Output format
     * @return New format
     */
    public Format setOutputFormat(Format output) {
        outputFormat = output;
        return output;
    }

    /**
     * Return the input format
     *
     * @return Format
     */
    public Format getInputFormat() {
        return inputFormat;
    }

    /**
     * Return the output format
     *
     * @return Format
     */
    public Format getOutputFormat() {
        return outputFormat;
    }

    /**
     * Performs the media processing defined by this codec
     *
     * @param input The buffer that contains the media data to be processed
     * @param output The buffer in which to store the processed media data
     * @return Processing result
     */
    public abstract int process(Buffer input, Buffer output);
}
