/*
 * Author:     Dante Hart
 * Assignment: Program 1
 * Class:      CSI 4321
 *
 */
package fabric.serialization;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an fabric message and provides serialization/deserialization
 */
public class ID extends Message{

    /**
     * The message operation for an ID
     */
    public static final String MSG_OP = "ID";

    /**
     * An ID messages ID identifier
     */
    protected String idNum;

    /**
     *
     * @param ID the desired identifier for the ID message
     * @throws ValidationException if invalid identifier given
     */
    public ID(String ID) throws ValidationException {
        this.setID(ID);
    }

    /**
     * Returns the ID message's identifer
     * @return the idNum member variable
     */
    public String getID() {
        return this.idNum;
    }

    /**
     * Validates an ID messages ID
     *
     * @param ID the ID to validate
     *
     * @return true if valid ID
     */
    protected boolean validateID(String ID){
        Pattern p = Pattern.compile("^[\\da-zA-Z]+$");
        Matcher m = p.matcher(ID);
        return m.matches();
    }

    /**
     *  Sets Identifier
     * @param idNum new Identifier to try to assign
     * @throws ValidationException if null or invalid identifier detected
     * @return this ID with new Identifier
     */
    public ID setID(String idNum) throws ValidationException {
        try{
            Objects.requireNonNull(idNum, "idNum Cannot be Null!");
        }catch(NullPointerException e){
            throw new ValidationException(e.getMessage(), e, idNum);
        }
        // valid for proper ID
        if(!validateID(idNum)){
            throw new ValidationException("Invalid ID detected!", idNum);
        }
        this.idNum = idNum;
        return this;
    }

    /**
     * Encodes an ID message to a MessageOutput
     * @param out serialization output sink
     *
     * @throws IOException if I/O error when writing bytes
     * @throws NullPointerException if outputstream is null
     */
    @Override
    public void encode(MessageOutput out) throws IOException, NullPointerException {
        super.encode(out);
        // create a String and pass it to the output stream
        String encodeString = MSG_OP;
        encodeString += " " + this.getID() + CR_LF;

        // write the bytes of the string to the output stream
        out.writeBytes(encodeString.getBytes(Message.CHAR_ENC));
    }

    /**
     * Returns a String representation
     * ID: id=[id]
     *
     * For example
     * ID: id=user
     * @return String representation of an ID message
     */
    @Override
    public String toString() {
        return this.getOperation() + ": id=" + this.getID();
    }

    /**
     * Override the equals method for an ID Message.
     * @param o The other Object to be tested for equivalence to the message
     * @return Whether the two message objects are equivalent
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ID id = (ID) o;
        return idNum.equals(id.idNum) && Objects.equals(getOperation(), id.getOperation());
    }

    /**
     * Overrides the hashCode method for an ID Message.
     * @return the hashCode for this ID Message
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), idNum);
    }

    /**
     *  Returns the operation type for an ID Message
     *
     * @return Returns message operation ID
     */
    @Override
    public String getOperation() {
        return MSG_OP;
    }
}
