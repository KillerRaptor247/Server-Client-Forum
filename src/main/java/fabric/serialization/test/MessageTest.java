/*
 * Author:     Dante Hart
 * Assignment: Program 0
 * Class:      CSI 4321
 *
 */
package fabric.serialization.test;

import fabric.serialization.Message;
import fabric.serialization.MessageInput;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * A JUnit testing class for testing the methods of the Error class
 */
@DisplayName("Testing Message Class")
public class MessageTest {

    /**
     * A class for testing the decode method for a Message class
     */
    @Nested
    @DisplayName("Testing Decode Method")
    protected class MessageDecodeTest{
        private static final byte[] ERRORENC = "ERROR 200 error msg\r\n".getBytes(StandardCharsets.ISO_8859_1);

        /**
         * Constructor declared here to clear JavaDoc compile warning
         */
        protected MessageDecodeTest(){

        }

    }

}
