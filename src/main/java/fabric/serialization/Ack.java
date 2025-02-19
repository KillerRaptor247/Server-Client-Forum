/*
 * Author:     Dante Hart
 * Assignment: Program 0
 * Class:      CSI 4321
 *
 */

package fabric.serialization;

import java.io.IOException;
import java.util.Objects;

/**
 * Represents ACK and provides serialization/deserialization
 */
public class Ack extends Message{


    /**
     * The Message Operation for an ACK message
     */
    public static final String MSG_OP = "ACK";
    /**
     * Constructs ACK message
     */
    public Ack(){
    }

    /**
     * Returns a String Representation of the Ack message object
     *
     * Ack
     *
     * For example
     *
     * Ack
     *
     * @return String representation: Ack
     */
    @Override
    public String toString(){
        return "Ack";
    }

    /**
     * Encodes an Ack message to a MessageOutput
     * @param out serialization output sink
     *
     * @throws IOException if I/O error when writing bytes
     * @throws NullPointerException if outputstream is null
     */

    @Override
    public void encode(MessageOutput out) throws IOException, NullPointerException {
        super.encode(out);
        out.writeBytes(MSG_OP.getBytes(Message.CHAR_ENC));
        out.writeBytes(CR_LF.getBytes(Message.CHAR_ENC));
    }


    /**
     * Override the equals method for an ACK Message
     * @return Whether the two ACK message objects are equivalent
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ack ack = (Ack) o;
        return Objects.equals(this.getOperation(), ack.getOperation());
    }

    /**
     * Overrides the hashCode method for an ACK Message
     * @return the hashCode for this ACK Message
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     *  Returns the operation type for an ACK Message
     *
     * @return Returns message operation ACK
     */
    @Override
    public String getOperation() {
        return MSG_OP;
    }
}
