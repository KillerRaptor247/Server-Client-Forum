/*
 * Author:     Dante Hart
 * Assignment: Program 0
 * Class:      CSI 4321
 *
 */

package fabric.serialization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an error and provides serialization/deserialization
 */
public class Error extends Message{

    /**
     * The message operation for an ERROR
     */
    public static final String MSG_OP = "ERROR";

    /**
     * The Error Message error code
     */
    protected int errorCode;

    /**
     * The Error Message message explanation
     */
    protected String errorMessage;

    /**
     * Validates an Error Message's Message contents
     */


    /**
     * Constructs error message using set values
     *
     * @param code error code
     * @param message error message
     *
     * @throws ValidationException if validation fails (see specification), including null message
     */
    public Error(int code, String message) throws ValidationException {
        this.setCode(code);
        this.setMessage(message);
    }


    /**
     * Returns code
     *
     * @return error code
     */
    public int getCode() {
        return errorCode;
    }

    /**
     * Sets Error Code
     *
     * @param code the new code to set the error code to
     *
     * @return this Error with new code
     *
     * @throws ValidationException if invalid code
     */
    public Error setCode(int code) throws ValidationException{
        if(!validateErrorCode(code)){
            throw new ValidationException("Invalid Error Code when Setting Code", String.valueOf(code));
        }
        this.errorCode = code;
        return this;
    }

    /**
     * Returns error message description
     * @return  errorMessage
     */
    public String getMessage() {
        return errorMessage;
    }

    /**
     * Sets error message
     *
     * @param message the new error message
     *
     * @return this Error with new error message
     *
     * @throws ValidationException invalid message, including null
     */

    public Error setMessage(String message) throws ValidationException {
        try{
            Objects.requireNonNull(message);
        }catch(Exception e){
            throw new ValidationException("Null Message When Setting Error Message", e, message);
        }
        if(!validateErrorMsg(message)){
            throw new ValidationException("Invalid Message When Setting Error Message", message);
        }
        this.errorMessage = message;
        return this;
    }

    /**
     *  Returns a String Representation
     *
     *  Error: code=[code] message=[message]
     *
     *  For Example
     *
     *  Error: code=500 message=Bad Stuff
     *
     * @return string representation of Error message
     */
    public String toString(){
        return "Error: code=" + this.getCode() + " message=" + this.getMessage();
    }

    /**
     * Encodes an Error message to a MessageOutput
     * @param out serialization output sink
     *
     * @throws IOException if I/O error when writing bytes
     * @throws NullPointerException if outputstream is null
     */
    @Override
    public void encode(MessageOutput out) throws IOException, NullPointerException {
        super.encode(out);
        // Create a String to send pass an uninterrupted stream to the MessageOutput
        String encodeString = this.getOperation();
        encodeString += " " + String.valueOf(this.getCode());
        encodeString += " " + this.getMessage();
        encodeString += "\r\n";

        // Once the full string has been created, convert to bytes and send to MessageOutput
        out.writeBytes(encodeString.getBytes(Message.CHAR_ENC));
    }

    /**
     * Validates an Error Message Code
     *
     * @param code the error code to validate
     *
     * @return true if valid error code
     */
    protected boolean validateErrorCode(int code){
        Pattern p = Pattern.compile("^[1-9][0-9][0-9]$");
        Matcher m = p.matcher(String.valueOf(code));
        return m.matches();
    }

    /**
     * Validates an Error Message Message contents
     *
     * @param message the contents of the error message
     *
     * @return true if valid message
     */
    protected boolean validateErrorMsg(String message){
        Pattern p = Pattern.compile("^[\\da-zA-Z\\s]+$");
        Matcher m = p.matcher(message);
        return m.matches();
    }

    /**
     * Override the equals method for an ERROR Message
     * @param o The other Object to be tested for equivalence to the ERROR message
     * @return Whether the two ERROR message objects are equivalent
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Error error = (Error) o;
        return errorCode == error.errorCode && errorMessage.equals(error.errorMessage) && Objects.equals(error.getOperation(), getOperation());
    }

    /**
     * Overrides the hashCode method for an ERROR Message
     * @return the hashCode for this ERROR Message
     */
    @Override
    public int hashCode() {
        return Objects.hash(errorCode, errorMessage);
    }

    /**
     *  Returns the operation type for an ERROR Message
     *
     * @return Returns message operation ERROR
     */
    @Override
    public String getOperation() {
        return MSG_OP;
    }
}
