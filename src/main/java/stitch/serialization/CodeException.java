/*
 * Author:     Dante Hart
 * Assignment: Program 4
 * Class:      CSI 4321
 *
 */
package stitch.serialization;

import java.io.Serial;
import java.util.Objects;

/**
 * Exception class used for signaling failure of message creation/management with an error code
 */
public class CodeException extends Exception{
    // serialization number
    @Serial
    private static final long serialVersionUID = 123456789L;

    /**
     * The error code of the code exception
     */
    private final ErrorCode errorCode;

    /**
     * Code Exception Constructor with just error code parameter
     * @param errorCode the error code to initialize the exception with
     */
    public CodeException(ErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        Objects.requireNonNull(errorCode, "Error Code cannot be null!");
        this.errorCode = errorCode;
    }

    /**
     * Code Exception Constructor with error code parameter and cause
     * @param errorCode the error code to initialize the exception with
     * @param cause the throwable cause of the CodeException
     */
    public CodeException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getErrorMessage(), cause);
        Objects.requireNonNull(errorCode, "Error Code cannot be null!");
        this.errorCode = errorCode;
    }

    /**
     * Returns the Code Exception's Error Code
     * @return errorCode
     */
    public ErrorCode getErrorCode(){
        return errorCode;
    }

    /**
     * Override the equals method for a CodeException.
     * @param o The other Object to be tested for equivalence to the CodeException
     * @return Whether the two message output objects are equivalent
     */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodeException that = (CodeException) o;
        return this.getMessage().equals(that.getMessage()) && this.getCause().equals(that.getCause()) && Objects.equals(errorCode, that.errorCode);
    }

    /**
     * Overrides the hashCode method for a ValidationException
     * @return the hashCode for this ValidationException
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.getMessage(), this.getCause(), errorCode);
    }
}
