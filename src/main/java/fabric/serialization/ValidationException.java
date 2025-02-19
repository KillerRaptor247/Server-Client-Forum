/*
 * Author:     Dante Hart
 * Assignment: Program 0
 * Class:      CSI 4321
 *
 */
package fabric.serialization;

import java.io.Serial;
import java.util.Objects;

/**
 * Exception for handling validation problems
 */
public class ValidationException extends Exception{

    @Serial
    private static final long serialVersionUID = 123456789L;

    /**
     * The exception bad string token causing exception (can be null)
     */
    protected String badToken;

    /**
     * Constructs validation Exception
     *
     * @param message exception message
     * @param cause exception cause
     * @param badToken bad string token causing exception (null if no such string)
     *
     */
    public ValidationException(String message, Throwable cause, String badToken){
        super(message, cause);
        this.badToken = badToken;
    }

    /**
     * Constructs validation Exception
     *
     * @param message exception message
     * @param badToken bad string token causing exception (null if no such string)
     */
    public ValidationException(String message, String badToken){
        this(message, null, badToken);
    }

    /**
     * Returns bad token
     * @return badToken variable
     */
    public String getBadToken(){
        return this.badToken;
    }

    /**
     * Override the equals method for a ValidationException.
     * @param o The other Object to be tested for equivalence to the ValidationException
     * @return Whether the two message output objects are equivalent
     */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationException that = (ValidationException) o;
        return this.getMessage().equals(that.getMessage()) && this.getCause().equals(that.getCause()) && Objects.equals(badToken, that.badToken);
    }

    /**
     * Overrides the hashCode method for a ValidationException
     * @return the hashCode for this ValidationException
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.getMessage(), this.getCause(), badToken);
    }
}
