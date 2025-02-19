/*
 * Author:     Dante Hart
 * Assignment: Program 0
 * Class:      CSI 4321
 *
 */
package fabric.serialization;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents generic portion of message and provides serialization/deserialization You may make concrete anything listed as abstract in this interface.
 * In other words, abstract is not part of the requirement (while the class, method, and parameters are required).
 */
public abstract class Message{

    /**
     * Carriage Return Line feed character constant for encoding and decode testing messages
     */
    public static final String CR_LF = "\r\n";


    /**
     * Constant for character set encoding
     */
    public static final Charset CHAR_ENC = StandardCharsets.ISO_8859_1;

    /**
     * Pattern object for matching regex patterns for message validation
     */
    private static Pattern messagePattern;

    /**
     * Matcher object for testing the message Pattern for a valid message
     */
    private static Matcher messageMatcher;

    /**
     *  Returns the operation type for a Message
     *
     * @return Returns message operation(e.g, ACK, ID, etc.)
     */
    public abstract String getOperation();

    /**
     * Validates a parsed string to see if it is an ACK message
     *
     * @param parseString  a string to parse
     *
     * @return false if valid ack
     */
    private static boolean validateAck(String parseString){
        return parseString.length() != Ack.MSG_OP.length() + CR_LF.length();
    }

    /**
     * Validates a parsed string to see if it is an ERROR message
     *
     * @param parseString a string to parse
     *
     * @return true if valid ERROR message
     */
    private static boolean validateError(String parseString){
        messagePattern = Pattern.compile("^" + Error.MSG_OP + "\\x20[1-9]\\d\\d\\x20[\\da-zA-Z\\x20]+\\r\\n\\z");
        messageMatcher = messagePattern.matcher(parseString);
        return messageMatcher.matches();
    }

    /**
     * Validates a parsed string to see if it is a Fabric Message
     *
     * @param parseString a string to parse
     *
     * @return false if valid FABRIC message
     */
    private static boolean validateFabric(String parseString){
        return !parseString.contains(" ") || parseString.length() != Fabric.MSG_OP.length() + CR_LF.length() + Fabric.FABRIC_VER.length() + 1 || !parseString.contains(Fabric.FABRIC_VER);
    }

    /**
     * Validates a parsed string to see if it is a Id Message
     *
     * @param parseString a string to parse
     *
     * @return true if valid ID message
     */
    private static boolean validateID(String parseString){
        messagePattern = Pattern.compile("^" + ID.MSG_OP + "\\x20[\\da-zA-Z]+\\r\\n\\z");
        messageMatcher = messagePattern.matcher(parseString);
        return messageMatcher.matches();
    }

    /**
     * Validates a parsed string to see if it is a Knowp Message
     *
     * @param parseString a string to parse
     *
     * @return false if valid KNOWP message
     */
    private static boolean validateKnowp(String parseString){
        return parseString.length() != Knowp.MSG_OP.length() + CR_LF.length();
    }

    /**
     * Validates a parsed string to see if it is a Challenge Message
     *
     * @param parseString a string to parse
     *
     * @return true if valid CLNG message
     */
    private static boolean validateChallenge(String parseString){
        // Try to parse the string into a valid challenge message
        messagePattern = Pattern.compile("^" + Challenge.MSG_OP + "\\x20\\d+\\r\\n\\z");
        messageMatcher = messagePattern.matcher(parseString);
        return messageMatcher.matches();
    }

    /**
     * Validates a parsed string to see if it is a Credentials Message
     *
     * @param parseString a string to parse
     *
     * @return true if valid CRED Message
     */
    private static boolean validateCredential(String parseString){
        // Try to parse the string into a valid challenge message
        messagePattern = Pattern.compile("^" + Credentials.MSG_OP + "\\x20[\\dA-F]+\\r\\n\\z");
        messageMatcher = messagePattern.matcher(parseString);
        return messageMatcher.matches();
    }

    /**
     * Validates a parsed string to see if it is a Bout Message
     *
     * @param parseString a string to parse
     *
     * @return true if valid BOUT message
     */
    private static boolean validateBout(String parseString){
        // Try to parse the string into a valid BOUT message
        messagePattern = Pattern.compile("^" + Bout.MSG_OP + "\\x20[\\da-zA-Z]+\\x20[\\da-zA-Z\\/\\+]+\\r\\n\\z");
        messageMatcher = messagePattern.matcher(parseString);
        return messageMatcher.matches();
    }
    /**
     *  Deserializes message from input source
     *
     * @param in deserialization input source
     *
     * @return a specific message resulting from deserialization
     *
     * @throws ValidationException if parse or validation problem
     * @throws IOException if I/O problem
     * @throws NullPointerException if in is null
     */
    public static Message decode(MessageInput in) throws NullPointerException, ValidationException, IOException{
        // Make sure the MessageInput is not null
        Objects.requireNonNull(in, "MessageInput in cannot be null");

        // get the parsedString from the bytes
        String parseString = in.readBytes();

        // Validate for a legal fabric message before parsing
        if(!parseString.endsWith(CR_LF)){
            throw new IOException("Invalid Message Parsed. No CRLF detected in " + parseString);
        }
        // Once a legal fabric form is validated, try to parse out correct message
        if(parseString.indexOf(Ack.MSG_OP) == 0){
            // ACK message detected
            // Begin ACK validation
            // Verify the Size
            if(validateAck(parseString)) {
                throw new ValidationException("Invalid ACK Message Format Detected", parseString);
            }
            return new Ack();
        }
        // If ERROR message detected
        else if(parseString.indexOf(Error.MSG_OP) == 0){
            // Begin ERROR validation
            // Try to Parse
            // If string doesn't pass regex throw exception
            if(!validateError(parseString)){
                throw new ValidationException("Invalid Error Message Format Detected " + parseString, parseString);
            }
            // otherwise create and return a new Error
            String errArg[] = parseString.split("\\x20", 3);
            return new Error(Integer.parseInt(errArg[1]), errArg[2].substring(0, errArg[2].indexOf(CR_LF)));
        }
        // If FABRIC message detected
        else if(parseString.indexOf(Fabric.MSG_OP) == 0){
            // Begin FABRIC validation
            // Try to Parse
            if(validateFabric(parseString)){
                throw new ValidationException("Invalid FABRIC Message Format Detected", parseString);
            }
            return new Fabric();
        }
        // If ID message detected
        else if(parseString.indexOf(ID.MSG_OP) == 0){
            // Begin ID validation
            if(!validateID(parseString)){
                throw new ValidationException("Invalid ID Message Format Detected " + parseString, parseString);
            }
            // convert parseString to ID to create new ID after validation
            // remember that the Java String.substring beginning Index is INCLUSIVE (pure agony)
            String parsedID = parseString.substring(parseString.indexOf(" ") + 1, parseString.indexOf(CR_LF));
            return new ID(parsedID);
        }
        // If Knowp Message detected
        else if(parseString.indexOf(Knowp.MSG_OP) == 0){
            //Try to Parse
            // Verify the Size
            if(validateKnowp(parseString)) {
                throw new ValidationException("Invalid KNOWP Message Format Detected", parseString);
            }
            return new Knowp();
        }
        // If Challenge Message detected
        else if(parseString.indexOf(Challenge.MSG_OP) == 0){
            // See if valid format was given
            if(!validateChallenge(parseString)){
                throw new ValidationException("Invalid Challenge Message Format Detected " + parseString, parseString);
            }
            // Parse out the servnonce for the challenge
            // split string into 2
            String clngArg[] = parseString.split("\\x20");
            return new Challenge(clngArg[1].substring(0, clngArg[1].indexOf(CR_LF)));
        }
        // If Credential Message detected
        else if(parseString.indexOf(Credentials.MSG_OP) == 0){
            if(!validateCredential(parseString)){
                throw new ValidationException("Invalid Credentials Message Format Detected " + parseString, parseString);
            }
            // Parse out the hash for the credentials
            // split string into 2
            String credArg[] = parseString.split("\\x20");
            return new Credentials(credArg[1].substring(0, credArg[1].indexOf(CR_LF)));
        }
        else if(parseString.indexOf(Bout.MSG_OP) == 0){
            // See if valid format was given
            if(!validateBout(parseString)){
                throw new ValidationException("Invalid BOUT Message Format Detected " + parseString, parseString);
            }
            // Parse out the category and then use Base64 Decoder for image
            String boutArg[] = parseString.split("\\x20", 3);
            Base64.Decoder boutDecode = Base64.getDecoder();
            //return new bout with category and image with decoding
            // Decode in Base64 only the image payload
            return new Bout(boutArg[1], boutDecode.decode(boutArg[2].substring(0, boutArg[2].indexOf(CR_LF))));
        }
        else{
            throw new ValidationException("Invalid Message Parsed. No valid op found at beginning of Message", parseString);
        }

    }

    /**
     *  Serializes message to given output sink. Will be overridden by its subclasses.
     *
     * @param out serialization output sink
     *
     * @throws IOException if I/O problem
     * @throws NullPointerException if out is null
     */
    public void encode(MessageOutput out) throws IOException, NullPointerException{
        Objects.requireNonNull(out, "MessageOutput out Cannot be null!");
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
        return Objects.equals(this.getOperation(), msg.getOperation());
    }

    /**
     * Overrides the hashCode method for a Message. Will likely be overridden by its subclasses
     * @return the hashCode for this Message
     */
    @Override
    public int hashCode() {
        return Objects.hash(getOperation());
    }
}
