/*
 * Author:     Dante Hart
 * Assignment: Program 1
 * Class:      CSI 4321
 *
 */
package fabric.serialization;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a challenge and provides serialization/deserialization
 */
public class Challenge extends Message{

    /**
     * The message operation for a CLNG
     */
    public static final String MSG_OP = "CLNG";

    /**
     * The nonce for the server challenges
     */
    protected String nonce;

    /**
     * Constructs challenge message using given values
     * @param nonce the nonce to pass in
     * @throws ValidationException if validation fails, including null nonce
     */
    public Challenge(String nonce) throws ValidationException {
        this.setNonce(nonce);
    }

    /**
     * Return nonce
     * @return nonce
     */
    public String getNonce() {
        return this.nonce;
    }

    /**
     * Validates an Challenge messages nonce
     *
     * @param nonce the nonce to validate
     *
     * @return true if valid nonce
     */
    protected boolean validateNonce(String nonce){
        Pattern p = Pattern.compile("^[\\d]+$");
        Matcher m = p.matcher(nonce);
        return m.matches();
    }

    /**
     * Sets nonce
     * @param nonce the new nonce to set
     * @return this Challenge with new nonce
     * @throws ValidationException is null or invalid nonce
     */
    public Challenge setNonce(String nonce) throws ValidationException {
        try{
            Objects.requireNonNull(nonce, "nonce Cannot be Null!");
        }catch(NullPointerException e){
            throw new ValidationException(e.getMessage(), e, nonce);
        }
        // valid for proper ID
        if(!validateNonce(nonce)){
            throw new ValidationException("Invalid nonce detected! " + Arrays.toString(nonce.getBytes(Message.CHAR_ENC)), nonce);
        }
        this.nonce = nonce;
        return this;
    }

    /**
     * Encodes a Challenge message to a MessageOutput
     * @param out serialization output sink
     *
     * @throws IOException if I/O error when writing bytes
     * @throws NullPointerException if outputstream is null
     */
    @Override
    public void encode(MessageOutput out) throws IOException, NullPointerException {
        super.encode(out);
        // Create parseString to pass to MessageOutput
        String parseString = MSG_OP;
        parseString += " " + this.getNonce();
        parseString += "\r\n";
        out.writeBytes(parseString.getBytes(Message.CHAR_ENC));
    }

    /**
     * Returns a String representation
     * Challenge: nonce=[nonce]
     *
     * For example
     * Challenge: nonce=500
     * @return String representation of an ID message
     */
    @Override
    public String toString() {
        return "Challenge: nonce=" + this.getNonce();
    }

    /**
     * Override the equals method for a Challenge Message.
     * @param o The other Object to be tested for equivalence to the message
     * @return Whether the two message objects are equivalent
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Challenge challenge = (Challenge) o;
        return nonce.equals(challenge.nonce) && Objects.equals(getOperation(), challenge.getOperation());
    }

    /**
     * Overrides the hashCode method for a Challenge Message.
     * @return the hashCode for this Challenge Message
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), nonce);
    }

    /**
     *  Returns the operation type for an CLNG Message
     *
     * @return Returns message operation CLNG
     */
    @Override
    public String getOperation() {
        return MSG_OP;
    }
}
