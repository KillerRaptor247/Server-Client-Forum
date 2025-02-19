/*
 * Author:     Dante Hart
 * Assignment: Program 0
 * Class:      CSI 4321
 *
 */
package fabric.serialization.test;

import fabric.serialization.*;
import fabric.serialization.Error;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A JUnit testing class for testing the methods of the Error class
 */
@DisplayName("Testing Error Class")
public class ErrorTest {

    private static final Charset ENC = StandardCharsets.ISO_8859_1;
    private static final String ERROP = "ERROR";
    private static final int ERRCODE = 200;
    private static final String ERRMSG = "error msg";
    private Error ERROR;
    private static final byte[] ERRORENC = "ERROR 200 error msg\r\n".getBytes(ENC);

    /**
     * Testing Ack encode
     */
    @Nested
    @DisplayName("Testing Error message encoding")
    protected class EncodingTest {
        /**
         * Constructor declared here to clear JavaDoc compile warning
         */
        protected EncodingTest(){
        }


        /**
         * Tests for a valid and successful encoding of an ERROR message
         *
         * @throws IOException if I/O error during testing
         */
        @DisplayName("Happy Path Encoding Test")
        @Test
        protected void testValidEncoding() throws IOException {

            try {
                ERROR = new Error(ERRCODE, ERRMSG);
            } catch (ValidationException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            MessageOutput mOut = new MessageOutput(bOut);
            ERROR.encode(mOut);
            assertArrayEquals(ERRORENC, bOut.toByteArray());
        }
    }

    /**
     * Testing ERROR Decode
     */
    @Nested
    @DisplayName("Testing Error message decoding")
    protected class DecodingTest{
        /**
         * Constructor declared here to clear JavaDoc compile warning
         */
        protected DecodingTest(){
        }

        /**
         * Tests for a valid and successful decoding of an ERROR message
         * @throws NullPointerException if null given
         * @throws ValidationException if invalid input detected
         * @throws IOException if I/O error during tests
         */
        @DisplayName("Happy Path Decoding Test")
        @Test
        protected void testValidDecoding() throws NullPointerException, ValidationException, IOException {
            MessageInput mIn = new MessageInput(new ByteArrayInputStream(ERRORENC));
            Error e = (Error) Message.decode(mIn);
            assertEquals(ERRCODE, e.getCode());
            assertEquals(ERRMSG, e.getMessage());
            assertEquals(ERROP, e.getOperation());
        }
    }

    /**
     * Testing Error Message Validation
     */
    @Nested
    @DisplayName("Testing Error Message Validation")
    protected class ErrorMsgValidation{
        /**
         * Constructor declared here to clear JavaDoc compile warning
         */
        protected ErrorMsgValidation(){

        }

        /**
         * An example of a valid message payload for an error message
         */
        protected static String VALID_ERR_MSG = "Error Message Test";

        /**
         * An example of a valid error code for an error message
         */
        protected static int VALID_ERR_CODE = 123;

        /**
         * Test for a Happy Path Valid Error Message
         */
        @DisplayName("Valid Error Message Creation")
        @Test
        protected void testValidErrorMsg(){
            Assertions.assertDoesNotThrow(() -> {
                Error e = new Error(VALID_ERR_CODE, VALID_ERR_MSG);
            });
        }

        /**
         * Tests an Error message with an invalid code
         */
        @DisplayName("Invalid Error Code")
        @Test
        protected void testInvalidErrorCode(){
            Assertions.assertThrows(ValidationException.class, () ->{
                int invalidCode = 044;
                Error e = new Error(invalidCode,VALID_ERR_MSG);
            });
        }
    }
}
