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
 * Represents an fabric message and provides serialization/deserialization
 */
public class Fabric extends Message{

    /**
     * The message operation for a FABRIC
     */
    public static final String MSG_OP = "FABRIC";

    /**
     * The Fabric version
     */
    public static final String FABRIC_VER = "1.0";

    /**
     * Constructs FABRIC message
     */
    public Fabric(){

    }

    /**
     * Encodes an Fabric message to a MessageOutput
     * @param out serialization output sink
     *
     * @throws IOException if I/O error when writing bytes
     * @throws NullPointerException if outputstream is null
     */
    @Override
    public void encode(MessageOutput out) throws IOException, NullPointerException {
        super.encode(out);
        // Create String to pass to the MessageOutput to write out
        String encodeString = MSG_OP;
        encodeString += " " + FABRIC_VER;
        encodeString += "\r\n";
        // After full string is created send it over to the MessageOutput
        out.writeBytes(encodeString.getBytes(Message.CHAR_ENC));
    }


    /**
     * Returns a String representation
     * Fabric 1.0
     *
     * For example
     * Fabric 1.0
     * @return String representation of a FABRIC message
     */
    @Override
    public String toString() {
        return "Fabric " + FABRIC_VER;
    }

    /**
     * Override the equals method for a FABRIC Message
     * @return Whether the two FABRIC message objects are equivalent
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fabric fab = (Fabric) o;
        return Objects.equals(this.getOperation(), fab.getOperation());
    }

    /**
     * Overrides the hashCode method for a Fabric Message
     * @return the hashCode for this Fabric Message
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     *  Returns the operation type for an FABRIC Message
     *
     * @return Returns message operation FABRIC
     */
    @Override
    public String getOperation() {
        return MSG_OP;
    }
}
