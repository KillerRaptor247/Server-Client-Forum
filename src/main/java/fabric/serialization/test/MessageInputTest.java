/*
 * Author:     Dante Hart
 * Assignment: Program 0
 * Class:      CSI 4321
 *
 */
package fabric.serialization.test;

import fabric.serialization.MessageInput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A JUnit testing class for testing the methods of the MessageInput class
 */
@DisplayName("Testing Message Input Class")
public class MessageInputTest {

    /**
     * A nested testing class for testing the readBytes method of a Message Input class
     */
    @Nested
    @DisplayName("Testing readBytes() method")
    protected class ReadByteTest{
        /**
         * Constructor declared here to clear JavaDoc compile warning
         */
        protected ReadByteTest(){

        }

        /**
         * Tests that the readBytes() method can successfully read into a byte buffer
         * @throws IOException if theres error during I/O
         */
        @DisplayName("Happy Path Read into Buffer single call")
        @Test
        protected void testValidReadBytes() throws IOException {
            String msg = "Hello";
            String result;
            ByteArrayInputStream inStream = new ByteArrayInputStream(msg.getBytes());
            MessageInput msgIn = new MessageInput(inStream);

            result = msgIn.readBytes();

            assertEquals(result, msg);
        }
    }

}
