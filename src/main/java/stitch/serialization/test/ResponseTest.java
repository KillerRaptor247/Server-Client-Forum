package stitch.serialization.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import stitch.serialization.ErrorCode;
import stitch.serialization.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * A JUnit testing class for testing the Query class
 */
@DisplayName("Testing Response Class")
public class ResponseTest {

    /**
     * Constructor declared here to clear JavaDoc compile warning
     */
    protected ResponseTest(){

    }

    private static final byte[] VALID_RESPONSE_MESSAGE = {0b00101000, 0b00000000, 0b00000000,0b00000000,0b00000000,0b00000000, 0b00000000};


    /**
     * Tests the response byte constructor method with a presumed valid array of bytes
     */
    @DisplayName("Valid Response Byte Deserialization")
    @Test
    protected void testValidResponseBuffer(){
        Assertions.assertDoesNotThrow(() -> {
            Response responseMsg = new Response(VALID_RESPONSE_MESSAGE);
            Assertions.assertSame(0L, responseMsg.getQueryID());
            Assertions.assertEquals(ErrorCode.NOERROR, responseMsg.getErrorCode());
        });
    }

    /**
     * Tests the response byte constructor method with a valid array of bytes
     */
    @DisplayName("Valid Response Constructor")
    @Test
    protected void testValidQueryConstructor(){
        Assertions.assertDoesNotThrow(() -> {
            List<String> emptyPosts = new ArrayList<>();
            Response response = new Response(12345L, ErrorCode.NOERROR, emptyPosts);
            Assertions.assertEquals(12345L, response.getQueryID());
        });
    }

    /**
     * Tests the encode serialization for a Response Message
     */
    @DisplayName("Testing Encode with empty Response")
    @Test
    protected void testEmptyResponseEncode(){
        Assertions.assertDoesNotThrow(() -> {
            Response responseMsg = new Response(VALID_RESPONSE_MESSAGE);
            byte [] encodeArray = responseMsg.encode();
            Assertions.assertArrayEquals(VALID_RESPONSE_MESSAGE, encodeArray);
        });
    }

    /**
     * Tests the encode serialization for a Full Response Message
     */
    @DisplayName("Testing Encode with full response")
    @Test
    protected void testFullResponseEncode(){
        Assertions.assertDoesNotThrow(() -> {
            List<String> testList = new ArrayList<>();
            testList.add("Hello!");
            Response responseMsg = new Response(12345L, ErrorCode.NOERROR, testList);
            byte [] encodeArray = responseMsg.encode();
            Assertions.assertArrayEquals(responseMsg.encode(), encodeArray);
            Assertions.assertEquals(responseMsg.getPosts().size(), 1);
        });
    }
}
