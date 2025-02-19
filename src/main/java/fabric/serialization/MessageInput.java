/*
 * Author:     Dante Hart
 * Assignment: Program 0
 * Class:      CSI 4321
 *
 */
package fabric.serialization;

import java.io.*;
import java.util.Objects;

/**
 * Deserialization input source This should ONLY include general methods for parsing.
 * Do not include protocol-specific methods; those should be in the protocol-specific classes.
 */
public class MessageInput {

    /**
     * The input stream source for an instance of Message Input
     */
    protected InputStream inputSource;

    /**
     * DataInputStream to Wrap in the InputStream in
     */

    protected DataInputStream dataInput;

    /**
     * Constructs a new input source from an InputStream. Must be non-blocking.
     *
     * @param in byte input source
     *
     * @throws NullPointerException if in is null
     */
    public MessageInput(InputStream in) throws NullPointerException{
        Objects.requireNonNull(in, "InputStream Cannot be null!");
        this.inputSource = in;
    }


    /**
     * Read in the bytes given to the MessageInput from the InputStream
     *
     * @return the number of bytes able to be read and put into the buffer
     * @throws IOException if error when reading bytes
     */
    public String readBytes() throws IOException {
        // NEVER ASSUME ANYTHING ABOUT INPUT EVER
        dataInput = new DataInputStream(inputSource);

        byte currByte;
        ByteArrayOutputStream currBuffer = new ByteArrayOutputStream();

        // read in one byte at a time we can't assume anything about the input
        // until we've reach EOS or the end of a message
        while((currByte = (byte) dataInput.read()) != -1){


            // store the byte into the ByteArrayOutputStream
            currBuffer.write(currByte);

            if(currByte == '\n'){
                break;
            }
        }

        // After Successful Copy into the read buffer the outputstream
        return currBuffer.toString(Message.CHAR_ENC);
    }


    /**
     * Override the equals method for a MessageInput.
     * @param o The other Object to be tested for equivalence to the MessageInput
     * @return Whether the two message input objects are equivalent
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageInput that = (MessageInput) o;
        return inputSource.equals(that.inputSource);
    }

    /**
     * Overrides the hashCode method for a MessageInput
     * @return the hashCode for this MessageInput
     */
    @Override
    public int hashCode() {
        return Objects.hash(inputSource);
    }
}
