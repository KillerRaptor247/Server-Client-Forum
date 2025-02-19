/*
 * Author:     Dante Hart
 * Assignment: Program 4
 * Class:      CSI 4321
 *
 */
package stitch.serialization;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Represents a Stitch query and performs serialization/deserialization
 */

public class Query extends Message{

    // constant for expected header length for all query messages
    private static final int QUERY_HEADER_BYTE_LEN = 8;

    private int requestedPosts;

    // validates a query version
    // b is the byte to validate
    private static void validateQueryVersion(byte b) throws CodeException{
        // isolate upper 4 bits by masking with 11110000
        byte result = (byte) (b & 0b11110000);
        // verify if result is equal to the expected header
        if(result != STITCH_VER_BITS){
            throw new CodeException(ErrorCode.BADVERSION);
        }
    }

    // validate the QR field and reserved bits
    // b is the byte to validate
    private static void validatePacketType(byte b) throws CodeException{
        // isolate lower 4 bits by masking with 00001111
        byte result = (byte) (b & 0b00001111);
        // verify if result is expected packet type
        if(result == 0b00001000){
            throw new CodeException(ErrorCode.UNEXPECTEDPACKETTYPE);
        }
        if(result != 0){
            throw new CodeException(ErrorCode.NETWORKERROR);
        }
    }

    // validate the expected error code byte
    private static void validateErrorCode(byte b) throws CodeException{
        // ErrorCode should always be 0 for a Query Message
        if(b != 0){
            throw new CodeException(ErrorCode.UNEXPECTEDERRORCODE);
        }
    }


    // Parse the requested posts from the buffer
    private static int parseRequestedPosts(byte[] b){
        int convReqPosts = 0;
        for(int i = 6; i < 8; i++){
            // cast each order byte and shift the offset and place into convReqPosts
            convReqPosts = (convReqPosts << Byte.SIZE) | (b[i] & 0xFF);
        }
        return convReqPosts;
    }

    /**
     * Deserialize query from given buffer
     * @param buffer array of bytes to attempt Query deserialization
     * @throws CodeException if validation fails during parsing or setting values
     */
    public Query(byte[] buffer) throws CodeException{
        // First test if buffer is null
        if(buffer == null){
            throw new CodeException(ErrorCode.VALIDATIONERROR);
        }
        // Try to validate based on minimum size of a Query Header
        if(buffer.length > QUERY_HEADER_BYTE_LEN){
            throw new CodeException(ErrorCode.PACKETTOOLONG);
        }
        if(buffer.length < QUERY_HEADER_BYTE_LEN){
            throw new CodeException(ErrorCode.PACKETTOOSHORT);
        }
        // If query packet is of appropriate size, validate the first byte for version and packet type
        validateQueryVersion(buffer[0]);
        validatePacketType(buffer[0]);
        // If version and packet type are valid, validate the the errorCode
        validateErrorCode(buffer[1]);
        // After the ErrorCode, validate the QueryID
        try{
            this.setQueryID(parseQueryID(buffer));
        }catch(IllegalArgumentException e){
            throw new CodeException(ErrorCode.VALIDATIONERROR ,e);
        }

        // Finally parse and validate the requested posts
        try{
            this.setRequestedPosts(parseRequestedPosts(buffer));
        }catch(IllegalArgumentException e){
            throw new CodeException(ErrorCode.VALIDATIONERROR ,e);
        }
    }

    /**
     * Creates new query given individual attributes
     * @param qID the query ID
     * @param requests number of requested posts
     */
    public Query(long qID, int requests) throws IllegalArgumentException{
        this.setQueryID(qID);
        this.setRequestedPosts(requests);
    }

    /**
     * Gets the number of requested posts in the message
     * @return requestedPosts
     */
    public int getRequestedPosts(){
        return this.requestedPosts;
    }

    /**
     * Set the number of requested post
     * @param requests desired number of requests
     * @return reference to this query object
     * @throws IllegalArgumentException if requests is out of range
     */
    public Query setRequestedPosts(int requests) throws IllegalArgumentException{
        // check high order bits
        if(requests < 0 || requests > 65535){
            throw new IllegalArgumentException(requests + " Requested Posts is outside of range!");
        }
        requestedPosts = requests;
        return this;
    }

    /**
     * Serialize the Message
     *
     * @return the serialized message
     */
    @Override
    public byte[] encode() {
        ByteArrayOutputStream queryEncodeStream = new ByteArrayOutputStream();
        // write the query versions
        queryEncodeStream.write(QUERY_VER);
        // write all 0's for ErrorCode field indicating no errors
        queryEncodeStream.write(ErrorCode.NOERROR.getErrorCodeValue());
        // convert query id to byte array and write to buffer
        byte[] qIdBytes = ByteBuffer.allocate(Integer.BYTES).putInt((int) getQueryID()).array();
        for (byte qIdByte : qIdBytes) {
            queryEncodeStream.write(qIdByte);
        }
        // write data field which is unsigned two-byte integer
        byte[] postsBytes = ByteBuffer.allocate(Short.BYTES).putShort(Integer.valueOf(getRequestedPosts()).shortValue()).array();
        for (byte postByte : postsBytes) {
            queryEncodeStream.write(postByte);
        }
        return queryEncodeStream.toByteArray();
    }

    /**
     * Returns a String representation
     * Query: QueryID=[id] ReqPosts=[noPosts]
     *
     * For example
     * Query: QueryID=5 ReqPosts=2
     * @return string representation of Query Message
     */
    @Override
    public String toString(){
        return "Query: QueryID=" + this.getQueryID() + " ReqPosts=" + this.getRequestedPosts();
    }

    /**
     * Override the equals method for a Query.
     * @param o The other Object to be tested for equivalence to the message
     * @return Whether the two message objects are equivalent
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Query msg = (Query) o;
        return Objects.equals(this.getRequestedPosts(), msg.getRequestedPosts());
    }

    /**
     * Overrides the hashCode method for a Query Message.
     * @return the hashCode for this Query Message
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), requestedPosts);
    }

}
