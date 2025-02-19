/*
 * Author:     Dante Hart
 * Assignment: Program 1
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
 *  Represents a credentials and provides serialization/deserialization
 */
public class Credentials extends Message{

    /**
     * The message operation for a CRED
     */
    public static final String MSG_OP = "CRED";

    /**
     * An MD-5 hash of the servnonce and the pwd
     */
    protected String credHash;

    /**
     * The length of what an 16-byte MD5 Hash should be if each byte is converted to a two character hex value
     */
    protected static final int HASH_LENGTH = 32;

    /**
     * Validates whether the hash given is a valid hash for a Credentials class
     * @param credHash the hash to be tested
     * @return true if valid hash
     */
    protected boolean validateHash(String credHash) {
        // First validate the length of the Hash
        if(credHash.length() != Credentials.HASH_LENGTH){
            return false;
        }
        // Then Regex Match the hash
        Pattern p = Pattern.compile("^[\\dA-F]+$");
        Matcher m = p.matcher(credHash);
        return m.matches();
    }

    /**
     * Constructs credentials message using given hash
     * @param credHash hash for credentials
     * @throws ValidationException if validation or hash fails
     */
    public Credentials(String credHash) throws ValidationException{
        this.setHash(credHash);
    }

    /**
     * returns hash
     * @return hash
     */
    public String getHash() {
        return this.credHash;
    }

    /**
     * Sets hash
     * @param credHash the new credentials hash to set
     * @return this credentials with new hash
     * @throws ValidationException if null or invalid hash
     */
    public Credentials setHash(String credHash) throws ValidationException {
        try{
            Objects.requireNonNull(credHash, "credHash Cannot be Null!");
        }catch(NullPointerException e){
            throw new ValidationException(e.getMessage(), e, credHash);
        }
        // valid for proper ID
        if(!validateHash(credHash)){
            throw new ValidationException("Invalid hash detected!", credHash);
        }
        this.credHash = credHash;
        return this;
    }

    @Override
    public void encode(MessageOutput out) throws IOException, NullPointerException {
        super.encode(out);
        // create string for parsing to MessageOutput
        String parseString = MSG_OP;
        parseString += " " + this.getHash();
        parseString += "\r\n";
        out.writeBytes(parseString.getBytes(Message.CHAR_ENC));
    }

    /**
     * Returns a String representation
     * Credentials: hash=[hash]
     *
     * For example
     * Credential: hash=000102030405060708090A0B0C0D0E0F
     * @return String representation of a Credentials message
     */
    @Override
    public String toString() {
        return "Credentials: hash=" + this.getHash();
    }

    /**
     * Override the equals method for a Credentials Message.
     * @param o The other Object to be tested for equivalence to the message
     * @return Whether the two message objects are equivalent
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Credentials that = (Credentials) o;
        return credHash.equals(that.credHash) && Objects.equals(getOperation(), that.getOperation());
    }

    /**
     * Overrides the hashCode method for a Credentials Message.
     * @return the hashCode for this Credentials Message
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), credHash);
    }

    /**
     *  Returns the operation type for a CRED Message
     *
     * @return Returns message operation CRED
     */
    @Override
    public String getOperation() {
        return MSG_OP;
    }
}
