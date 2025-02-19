/*
 * Author:     Dante Hart
 * Assignment: Program 0
 * Class:      CSI 4321
 *
 */
package fabric.serialization.test;

import fabric.serialization.MessageInput;
import fabric.serialization.MessageOutput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * A JUnit testing class for testing the methods of the MessageOutput class
 */
@DisplayName("Testing Message Output Class")
public class MessageOutputTest {

    /**
     * Nested testing class for the writeBytes method of the Message Output Class
     */
    @Nested
    @DisplayName("Testing writeBytes() method")
    protected class WriteByteTest{
        /**
         * Constructor declared here to clear JavaDoc compile warning
         */
        protected WriteByteTest(){

        }

        /**
         * Tests that the writeBytes() method can successfully write into a byte buffer
         * @throws IOException if I/O problem during read and write
         */
        @DisplayName("Happy Path Write into Buffer single call")
        @Test
        protected void testValidWriteBytes() throws IOException {
            String msg = "Hello";
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            MessageOutput msgOut = new MessageOutput(outStream);

            msgOut.writeBytes(msg.getBytes());
            assertArrayEquals(outStream.toByteArray(), msg.getBytes());
        }
    }
}
