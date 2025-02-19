/*
 * Author:     Dante Hart
 * Assignment: Program 1
 * Class:      CSI 4321
 *
 */
package fabric.serialization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a Bout message and provides serialization/deserialization.
 * The image is set/stored as raw (that is from the original file) bytes. You do not need to validate the raw bytes as a valid image. The image is only encoded in Base64 when serialized (i.e., immediately before decode and right after encode, but never stored Base64 encoded). Java provides Base64 encoding (@see java.util.Base64).
 * Note well that you must use the "Basic" coders. Your encoding should NOT include padding.
 */
public class Bout extends Message{

    /**
     * The message operation for a BOUT
     */
    public static final String MSG_OP = "BOUT";

    /**
     * The category for a bout message
     */
    private String boutCategory;

    /**
     * The Image for a bout message
     */
    private byte[] boutImage;

    /**
     * Constructs message using set values
     * @param category the category for the bout
     * @param image the bytes of the image
     * @throws ValidationException if validation fails
     */
    public Bout(String category, byte[] image) throws ValidationException{
        this.setCategory(category);
        this.setImage(image);
    }

    /**
     * Checks whether the string passed in is a valid category
     * @param category the bout category to be tested
     * @return true if valid category
     */
    protected boolean validateCategory(String category){
        Pattern p = Pattern.compile("^[\\da-zA-Z]+$");
        Matcher m = p.matcher(category);
        return m.matches();
    }

    /**
     * Returns the category
     * @return category
     */
    public String getCategory() {
        return boutCategory;
    }

    /**
     * Sets the category
     * @param boutCategory the new category
     * @throws ValidationException if null or invalid category
     * @return reference to this Bout message
     */
    public Bout setCategory(String boutCategory) throws ValidationException {
        if(boutCategory == null){
            throw new ValidationException("Category Cannot be Null!", boutCategory);
        }
        if(!validateCategory(boutCategory)){
            throw new ValidationException("Invalid Category Detected!", boutCategory);
        }
        this.boutCategory = boutCategory;
        return this;
    }

    /**
     * Returns Image
     * @return image
     */
    public byte[] getImage() {
        return boutImage;
    }

    /**
     * Sets Image
     * @param boutImage the new image
     * @throws ValidationException if null image
     * @return reference to this Bout message
     */
    public Bout setImage(byte[] boutImage) throws ValidationException {
        try{
            Objects.requireNonNull(boutImage, "Bout Image cannot be null!");
        }catch(Exception e){
            throw new ValidationException(e.getMessage(), e.getCause(), Arrays.toString(boutImage));
        }
        this.boutImage = boutImage;
        return this;
    }

    /**
     * Returns a String Representation of the Bout message object
     *
     * Bout: category=[category] image=[count] bytes
     *
     * For example
     *
     * Bout: category=movie image=35 bytes
     *
     * @return String representation: Knowp
     */
    @Override
    public String toString() {
        return "Bout: category=" + this.getCategory() + " image=" + this.getImage().length + " bytes";
    }

    /**
     * Encodes a Bout message to a MessageOutput
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
        encodeString += " " + this.getCategory();
        // encode the bytes in Base64
        // Get a Basic Encoder without any padding
        Base64.Encoder boutEncode = Base64.getEncoder().withoutPadding();
        encodeString += " " + boutEncode.encodeToString(this.getImage());
        encodeString += Message.CR_LF;
        // After full string is created send it over to the MessageOutput
        out.writeBytes(encodeString.getBytes(Message.CHAR_ENC));
    }


    /**
     * Override the equals method for a Bout Message.
     * @param o The other Object to be tested for equivalence to the message
     * @return Whether the two message objects are equivalent
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Bout bout = (Bout) o;
        return boutCategory.equals(bout.boutCategory) && Arrays.equals(boutImage, bout.boutImage) && Objects.equals(getOperation(), bout.getOperation());
    }

    /**
     * Overrides the hashCode method for a Bout Message.
     * @return the hashCode for this Bout Message
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(super.hashCode(), boutCategory);
        result = 31 * result + Arrays.hashCode(boutImage);
        return result;
    }

    /**
     *  Returns the operation type for a BOUT Message
     *
     * @return Returns message operation BOUT
     */
    @Override
    public String getOperation() {
        return MSG_OP;
    }


}
