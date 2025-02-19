/*
 * Author:     Dante Hart
 * Assignment: Program 0
 * Class:      CSI 4321
 *
 */
package fabric.serialization.test;

import fabric.serialization.*;
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
 * A JUnit testing class for testing the methods of the Ack class
 */
@DisplayName("Testing Ack Class")
public class AckTest {

    /**
     * Constructor declared here to clear JavaDoc compile warning
     */
    protected AckTest(){

    }

    private static final Charset ENC = StandardCharsets.ISO_8859_1;
    private static final String ACKOP = "ACK";
    private Ack ACK;
    private static final byte[] ACKENC = "ACK\r\n".getBytes(ENC);

    /**
     * Testing Ack Constructor and Getter methods
     */
    @Nested
    @DisplayName("Testing Ack Constructor and Getter Methods")
    protected class ConstructorGetterTest{

        /**
         * Constructor declared here to clear JavaDoc compile warning
         */
        protected ConstructorGetterTest(){

        }

        /**
         * Tests the getOperation method to ensure the ACK returns the correct message
         */
        @DisplayName("Valid getOperation() return")
        @Test
        protected void testValidGetOperation(){
            Assertions.assertDoesNotThrow(() -> {
                Ack ackMsg = new Ack();
                Assertions.assertSame("ACK", ackMsg.getOperation());
            });
        }

        /**
         * Tests the getOperation method for an invalid return value
         */
        @DisplayName("Invalid getOperation() return")
        @Test
        protected void testInvalidGetOperation(){
            Assertions.assertDoesNotThrow(() -> {
                Ack ackMsg = new Ack();
                Assertions.assertNotSame("ERROR", ackMsg.getOperation());
            });
        }

        /**
         * Tests the toString method to ensure the ACK returns the correct string value
         */
        @DisplayName("Valid toString() return")
        @Test
        protected void testValidToString(){
            Assertions.assertDoesNotThrow(() -> {
                Ack ackMsg = new Ack();
                Assertions.assertSame("Ack", ackMsg.toString());
            });
        }

        /**
         * Tests the toString method with an invalid return
         */
        @DisplayName("Invalid toString() return")
        @Test
        protected void testInvalidToString(){
            Assertions.assertDoesNotThrow(() -> {
                Ack ackMsg = new Ack();
                Assertions.assertNotSame("Error", ackMsg.toString());
            });
        }
    }

    /**
     * Testing Ack encode
     */
    @Nested
    @DisplayName("Testing Ack message encoding")
    protected class EncodingTest {
        /**
         * Constructor declared here to clear JavaDoc compile warning
         */
        protected EncodingTest(){
        }


        /**
         * Tests for a valid and successful encoding of an ACK message
         * @throws IOException if I/O error detected during tests
         */
        @DisplayName("Happy Path Encoding Test")
        @Test
        protected void testValidEncoding() throws IOException {

            ACK = new Ack();
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            MessageOutput mOut = new MessageOutput(bOut);
            ACK.encode(mOut);
            assertArrayEquals(ACKENC, bOut.toByteArray());
        }
    }

    /**
     * Testing Ack Decode
     */
    @Nested
    @DisplayName("Testing Ack message decoding")
    protected class DecodingTest{
        /**
         * Constructor declared here to clear JavaDoc compile warning
         */
        protected DecodingTest(){
        }


        /**
         * Tests for a valid and successful decoding of an ACK message
         * @throws NullPointerException if null input detecetd
         * @throws ValidationException if invalid input detecetd
         * @throws IOException if I/O error detected during testing
         */
        @DisplayName("Happy Path Decoding Test")
        @Test
        protected void testValidDecoding() throws NullPointerException, ValidationException, IOException {
            MessageInput mIn = new MessageInput(new ByteArrayInputStream(ACKENC));
            Ack a = (Ack) Message.decode(mIn);
            assertEquals(ACKOP, a.getOperation());
        }
    }
}
