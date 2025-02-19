/*
 * Author:     Dante Hart
 * Assignment: Program 4
 * Class:      CSI 4321
 *
 */
package stitch.serialization;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a Stitch response and performs serialization/deserialization
 */
public class Response extends Message {


    // constant for expected header length for response messages
    // (Message Header + Post list length)
    // The shortest response message header is presumed to be ended after a post list length of 0
    private static final int MIN_RESPONSE_HEADER_BYTE_LEN = 7;

    private static final int MAX_POST_LIST_LENGTH = 255;

    private static final int MAX_INDIVIDUAL_POST_LENGTH = 65535;

    /** The absolute maximum length of a response message
    (Message Header + Max Post list length (255) * max individual post length (65535))
     */
    public static final int MAX_RESPONSE_HEADER_BYTE_LEN = MIN_RESPONSE_HEADER_BYTE_LEN + (MAX_POST_LIST_LENGTH * Short.BYTES) + (MAX_POST_LIST_LENGTH * MAX_INDIVIDUAL_POST_LENGTH);

    private static final byte RESPONSE_RESERVED_BITS = 0b00001000;

    private List<String> postList;

    private ErrorCode errorCode;

    // validates a response version
    // b is the byte to validate
    private static void validateResponseVersion(byte b) throws CodeException{
        // isolate upper 4 bits by masking with 11110000
        byte result = (byte) (b & 0b11110000);
        // verify if result is equal to the expected header
        if(result != STITCH_VER_BITS){
            throw new CodeException(ErrorCode.BADVERSION);
        }
        result = (byte) (b & 0b11111000);
        if(result != RESPONSE_VER){
            throw new CodeException(ErrorCode.UNEXPECTEDPACKETTYPE);
        }
    }

    // validate the packet type
    // b is the byte to validate
    private static void validatePacketType(byte b) throws CodeException{
        // isolate lower 4 bits by masking with 00001111
        byte result = (byte) (b & 0b1111);
        // verify if result is expected packet type
        // verify for Query Packet
        if(result == 0){
            throw new CodeException(ErrorCode.UNEXPECTEDPACKETTYPE);
        }
        if(result != RESPONSE_RESERVED_BITS){
            throw new CodeException(ErrorCode.NETWORKERROR);
        }
    }

    private static List<String> parsePostList(byte[] b) throws CodeException{
        int convPostList = 0;
        // convert post length to integer
        int index = 6;
        convPostList = b[index] & 0xFF;
        index++;

        int totalByteLength = MIN_RESPONSE_HEADER_BYTE_LEN + (convPostList * Short.BYTES);

        List<String> tempPostList = new ArrayList<>();

        // based on the converted post list, loop n times and parse strings
        for(int i = 0; i < convPostList; i++){
            // first parse the individual post length from the buffer
            // parse low order and high order and combine into single int
            int individPostLength = (b[index] & 0xFF) << Byte.SIZE | (b[index + 1] & 0xFF);
            // update index as to not reread in post length as characters
            index += Short.BYTES;
            // read characters from buffer up to n time
            String currPost;
            try{
                currPost = new String(b, index, individPostLength, StandardCharsets.ISO_8859_1);
            }catch(Exception e){
                throw new CodeException(ErrorCode.PACKETTOOSHORT, e);
            }

            // add post to the list
            try{
                tempPostList.add(currPost);
            }catch(Exception e){
                throw new CodeException(ErrorCode.VALIDATIONERROR, e);
            }
            // update index
            index += individPostLength;
            totalByteLength += individPostLength;
        }

        if(totalByteLength < b.length){
            throw new CodeException(ErrorCode.PACKETTOOLONG);
        }
        return tempPostList;
    }

    /**
     *
     * Deserialize query from given buffer
     * @param buffer array of bytes to attempt Response deserialization
     * @throws CodeException if validation fails
     */
    public Response(byte[] buffer) throws CodeException{
        if(buffer == null){
            throw new CodeException(ErrorCode.VALIDATIONERROR);
        }
        if(buffer.length > MAX_RESPONSE_HEADER_BYTE_LEN){
            throw new CodeException(ErrorCode.PACKETTOOLONG);
        }
        if(buffer.length < MIN_RESPONSE_HEADER_BYTE_LEN){
            throw new CodeException(ErrorCode.PACKETTOOSHORT);
        }
        validateResponseVersion(buffer[0]);
        validatePacketType(buffer[0]);
        // If version and packet type are valid, validate the errorCode by attempting to set the error code
        try{
            this.setErrorCode(ErrorCode.getErrorCode(buffer[1]));
        }catch(IllegalArgumentException e){
            throw new CodeException(ErrorCode.UNEXPECTEDERRORCODE, e);
        }

        // After the ErrorCode, validate the QueryID
        try{
            this.setQueryID(parseQueryID(buffer));
        }catch(IllegalArgumentException e){
            throw new CodeException(ErrorCode.VALIDATIONERROR ,e);
        }

        // After the QueryID, parse and validate the post list
        try{
            this.setPosts(parsePostList(buffer));
        }catch(IllegalArgumentException e){
            throw new CodeException(ErrorCode.VALIDATIONERROR ,e);
        }
    }

    /**
     * Response Constructor
     * @param qID queryID
     * @param errCode the Error Code
     * @param posts The list of posts
     * @throws IllegalArgumentException If invalid value passed to any of the setters function
     */
    public Response (long qID, ErrorCode errCode, List<String> posts) throws IllegalArgumentException{
        this.setErrorCode(errCode);
        this.setQueryID(qID);
        this.setPosts(posts);
    }

    /**
     * Sets the Error Code of this response
     * @param errCode the new error code
     * @return a reference to the current response message
     * @throws IllegalArgumentException if error code is null
     */
    public Response setErrorCode(ErrorCode errCode) throws IllegalArgumentException{
        if(errCode == null){
            throw new IllegalArgumentException("Error Code cannot be null!");
        }
        this.errorCode = errCode;
        return this;
    }

    /**
     * Return the error code
     * @return errorCode
     */
    public ErrorCode getErrorCode(){
        return this.errorCode;
    }

    /**
     * Sets the post list variable of the current Response Message
     * @param posts the new post list
     * @return reference to current Response
     * @throws IllegalArgumentException if post list is null or out of range or individual post is null or out of range or if post contains illegal characters
     */
    public Response setPosts(List<String> posts) throws IllegalArgumentException{
        // if check if post list is null
        if(posts == null) {
            throw new IllegalArgumentException("Post list cannot be null!");
        }
        // check if posts list length is valid
        if (((posts.size() >> Byte.SIZE) & 0xFF) != 0){
            throw new IllegalArgumentException("Post list length is out of range!");
        }
        // check if any individual post is invalid
        for (int i = 0; i < posts.size(); i++) {
            // if individual post is null or cannot encode a character within the ISO_8859_1 CharSet
            if(posts.get(i) == null){
                throw new IllegalArgumentException("Individual Post in a post list cannot be null!");
            }
            // individual post length is invalid
            if(((posts.get(i).length() >> (Byte.SIZE * 2)) & 0xFFFF) != 0){
                throw new IllegalArgumentException("Individual Post length is out of range!");
            }

            // try to encode the string to see if it can be a valid ISO_8859_1 encoding
            for(char c : posts.get(i).toCharArray()){
                if( !(c >= 0x20 && c <= 0x7E)){
                    throw new IllegalArgumentException("Illegal Character!");
                }
            }
        }

        postList = posts;
        return this;
    }

    /**
     * returns the post list
     * @return postList
     */
    public List<String> getPosts(){
        return this.postList;
    }

    /**
     * Serialize the Message
     *
     * @return the serialized message
     */
    @Override
    public byte[] encode() {
        ByteArrayOutputStream responseEncodeStream = new ByteArrayOutputStream();
        // write the response versions
        responseEncodeStream.write(RESPONSE_VER);
        // write ErrorCode field for response
        responseEncodeStream.write(errorCode.getErrorCodeValue());

        // convert query id to byte array and write to buffer
        byte[] qIdBytes = ByteBuffer.allocate(Integer.BYTES).putInt((int) getQueryID()).array();
        for (byte qIdByte : qIdBytes) {
            responseEncodeStream.write(qIdByte);
        }

        // write to buffer length of post list
            responseEncodeStream.write((byte) getPosts().size());

        // for each post in post list write the length and the post itself
        for(int i = 0; i < getPosts().size(); i++){
            byte[] currPostLenBytes = ByteBuffer.allocate(Short.BYTES).putShort(Integer.valueOf(postList.get(i).length()).shortValue()).array();
            for (byte currPostLenByte : currPostLenBytes){
                responseEncodeStream.write(currPostLenByte);
            }
            // once length has been written, write the post itself
            for(int j = 0; j < postList.get(i).length(); j++){
                responseEncodeStream.write( (byte) postList.get(i).charAt(j));
            }
        }

        return responseEncodeStream.toByteArray();
    }

    /**
     * public String toString()
     * Returns a String representation
     * Response: QueryID= [id] Error=[errcode] Posts=[noPosts]: [post1], ..., [postn]
     *
     * For example
     * Response: QueryID=5 Error=NO ERROR Posts=3: fruit, salad, bacon
     */
    @Override
    public String toString(){
        String outputString = "Response: QueryID=" + this.getQueryID() + " Error=" + this.getErrorCode().getErrorCodeValue() + " Posts=" + this.getPosts().size() + ": ";
        for (int i = 0; i < this.getPosts().size(); i++) {
            if(i == 0){
                outputString += (this.getPosts().get(i));
            }
            else{
                outputString += (", ") + (this.getPosts().get(i));
            }
        }
        return  outputString;
    }

    /**
     * Override the equals method for a Response.
     * @param o The other Object to be tested for equivalence to the message
     * @return Whether the two message objects are equivalent
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Response msg = (Response) o;
        return Objects.equals(this.getPosts(), msg.getPosts()) && Objects.equals(this.getErrorCode(), msg.getErrorCode());
    }

    /**
     * Overrides the hashCode method for a Query Message.
     * @return the hashCode for this Query Message
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), postList, errorCode);
    }
}
