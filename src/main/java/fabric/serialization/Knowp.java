/*
 * Author:     Dante Hart
 * Assignment: Program 1
 * Class:      CSI 4321
 *
 */
package fabric.serialization;

import java.io.IOException;
import java.util.Objects;

/**
 *  Represents a Knowp messages and provides serialization/deserialization
 */
public class Knowp extends Message{

    /**
     * The message operation for a KNOWP
     */
    public static final String MSG_OP = "KNOWP";

    /**
     * Constructs a Knowp Message
     */
    public Knowp() {

    }

    /**
     * Returns a String Representation of the Knowp message object
     *
     * Knowp
     *
     * For example
     *
     * Knowp
     *
     * @return String representation: Knowp
     */
    @Override
    public String toString(){
        return "Knowp";
    }

    /**
     * Encodes a Knowp message to a MessageOutput
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
     * Override the equals method for an Knowp Message
     * @return Whether the two Knowp message objects are equivalent
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Knowp knowp = (Knowp) o;
        return Objects.equals(this.getOperation(), knowp.getOperation());
    }

    /**
     * Overrides the hashCode method for a Knowp Message
     * @return the hashCode for this Knowp Message
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     *  Returns the operation type for a KNOWP Message
     *
     * @return Returns message operation KNOWP
     */
    @Override
    public String getOperation() {
        return MSG_OP;
    }
}
