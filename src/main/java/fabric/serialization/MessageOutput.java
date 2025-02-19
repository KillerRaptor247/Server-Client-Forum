/*
 * Author:     Dante Hart
 * Assignment: Program 0
 * Class:      CSI 4321
 *
 */
package fabric.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Serialization output sink This should ONLY include general methods for output.
 * Do not include protocol-specific methods; those should be in the protocol-specific classes.
 */
public class MessageOutput {

    /**
     * The OutputStream for a MessageOutput class
     */
    protected OutputStream outputSource;

    /**
     * Constructs a new output sink from an OutputStream
     *
     * @param out byte output sink
     *
     * @throws NullPointerException if out is null
     */
    public MessageOutput(OutputStream out) throws NullPointerException{
        Objects.requireNonNull(out, "OutputStream Cannot be null!");
        this.outputSource = out;
    }

    /**
     * Writes out the bytes given to the MessageOutput to the OutputStream
     *
     * @param bytes an array of bytes
     * @throws IOException if error when writing bytes
     */
    public void writeBytes(byte[] bytes) throws IOException {
        outputSource.write(bytes);
    }

    /**
     * Override the equals method for a MessageOutput.
     * @param o The other Object to be tested for equivalence to the MessageOutput
     * @return Whether the two message output objects are equivalent
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageOutput that = (MessageOutput) o;
        return Objects.equals(outputSource, that.outputSource);
    }

    /**
     * Overrides the hashCode method for a MessageOutput
     * @return the hashCode for this MessageOutput
     */
    @Override
    public int hashCode() {
        return Objects.hash(outputSource);
    }
}
