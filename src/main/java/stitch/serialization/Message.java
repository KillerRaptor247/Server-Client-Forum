/*
 * Author:     Dante Hart
 * Assignment: Program 4
 * Class:      CSI 4321
 *
 */
package stitch.serialization;

import java.util.Objects;

/**
 * Represents a Stitch message Header
 */
public abstract class Message {

    // The query ID for a stitch message
    private long queryID;

    /**
     * Expected Version for a Stitch protocol Message
     */
    protected static final byte STITCH_VER_BITS = 0b00100000;
    /** byte for expected query version header
     */
    protected static final byte QUERY_VER = 0b00100000;

    /** byte for expected query version header
     */
    protected static final byte RESPONSE_VER = 0b00101000;

    /**
     * Maximum value of a 4 byte Query ID
     */
    public static final long MAX_QID = 4294967295L;

    /**
     * Parses the queryID from a byte buffer
     * @param b the byte array to parse the queryID from
     * @return the query ID
     */
    protected static long parseQueryID(byte[] b){
        long convQId = 0;
        // convert array of 4 bytes to long and return the value
        for(int i = 2; i < 6; i++){
            // cast each order byte and shift the offset and place into convQId
            convQId = (convQId << Byte.SIZE) | (b[i] & 0xFF);
        }
        return convQId;
    }


    /**
     * Serialize the Message
     * @return the serialized message
     */
    public abstract byte[] encode();

    /**
     * Returns the messages query ID
     * @return queryID
     */

    public long getQueryID(){
        return queryID;
    }

    /**
     * Sets the messages query id
     * @param qID the query Id
     * @return reference to this message
     * @throws IllegalArgumentException if query ID out of range
     */
    public Message setQueryID(long qID) throws IllegalArgumentException{
        //long highOrderID = qID >> Byte.SIZE * 4;
        //highOrderID = highOrderID & 0xFFFFFFFFL;
        // check high order bits
        if(qID < 0 || qID > MAX_QID){
            throw new IllegalArgumentException( qID + " :Query ID is outside of range!");
        }
        queryID = qID;
        return this;
    }

    /**
     * Override the equals method for a Message. This will likely be overridden by its subclasses
     * @param o The other Object to be tested for equivalence to the message
     * @return Whether the two message objects are equivalent
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message msg = (Message) o;
        return Objects.equals(this.getQueryID(), msg.getQueryID());
    }

    /**
     * Overrides the hashCode method for a Message. Will likely be overridden by its subclasses
     * @return the hashCode for this Message
     */
    @Override
    public int hashCode() {
        return Objects.hash(getQueryID());
    }
}
