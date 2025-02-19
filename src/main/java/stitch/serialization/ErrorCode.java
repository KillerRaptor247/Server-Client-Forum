/*
 * Author:     Dante Hart
 * Assignment: Program 4
 * Class:      CSI 4321
 *
 */
package stitch.serialization;



/**
 * Allowable error code with associated numeric values and error messages. Each ErrorCode(e.g BADVERSION) has an associated value (integer) and message (string)
 *
 */
public enum ErrorCode {
    /**
     * Code if no Error Code
     */
    NOERROR,
    /**
     * Code if Bad Version bits detected
     */
    BADVERSION,
    /**
     * Code if unexpected error code given
     */
    UNEXPECTEDERRORCODE,
    /**
     * Code if Unexpected Packet Type
     */
    UNEXPECTEDPACKETTYPE,
    /**
     * Code if Packet too long
     */
    PACKETTOOLONG,
    /**
     * Code if Packet too short
     */
    PACKETTOOSHORT,
    /**
     * Code if bad reserve or other network validation error
     */
    NETWORKERROR,
    /**
     * Code if packet field validation error
     */
    VALIDATIONERROR;

    /**
     * Get the error code associated with the given error value
     * @param errorCodeValue integer error code value
     * @return the enum value corresponding to the error code
     * @throws IllegalArgumentException if invalid error code given
     */
    public static ErrorCode getErrorCode(int errorCodeValue) throws IllegalArgumentException{
        return switch (errorCodeValue) {
            case 0 -> NOERROR;
            case 1 -> BADVERSION;
            case 2 -> UNEXPECTEDERRORCODE;
            case 3 -> UNEXPECTEDPACKETTYPE;
            case 4 -> PACKETTOOLONG;
            case 5 -> PACKETTOOSHORT;
            case 7 -> NETWORKERROR;
            case 8 -> VALIDATIONERROR;
            default -> throw new IllegalArgumentException("Invalid Error Code Type Given!");
        };
    }

    /**
     * Returns the integer value of the error code for this ErrorCode
     * @return int
     */
    public int getErrorCodeValue(){
        return switch (this){
            case NOERROR -> 0;
            case BADVERSION -> 1;
            case UNEXPECTEDERRORCODE -> 2;
            case UNEXPECTEDPACKETTYPE -> 3;
            case PACKETTOOLONG -> 4;
            case PACKETTOOSHORT -> 5;
            case NETWORKERROR -> 7;
            case VALIDATIONERROR -> 8;
        };
    }

    /**
     * Get the error message
     * @return the message associate with the error code
     */
    public String getErrorMessage(){
        return switch (this){
            case NOERROR -> "No error";
            case BADVERSION -> "Bad version";
            case UNEXPECTEDERRORCODE -> "Unexpected error code";
            case UNEXPECTEDPACKETTYPE -> "Unexpected packet type";
            case PACKETTOOLONG -> "Packet too long";
            case PACKETTOOSHORT -> "Packet too short";
            case NETWORKERROR -> "Network error";
            case VALIDATIONERROR -> "Validation error";
        };
    }

    @Override
    public String toString(){
        return this.getErrorMessage();
    }


}
