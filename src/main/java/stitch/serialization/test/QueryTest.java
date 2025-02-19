package stitch.serialization.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import stitch.serialization.Query;

/**
 * A JUnit testing class for testing the Query class
 */
@DisplayName("Testing Query Class")
public class QueryTest {

    /**
     * Constructor declared here to clear JavaDoc compile warning
     */
    protected QueryTest(){

    }

    private static final byte[] VALID_QUERY_MESSAGE = {0b00100000, 0b00000000, 0b00000000,0b00000000,0b00000000,0b00000000, 0b00000000, 0b00000000};


    /**
     * Tests the query byte constructor method with a presumed valid array of bytes
     */
    @DisplayName("Valid Query Byte Deserialization")
    @Test
    protected void testValidQueryBuffer(){
        Assertions.assertDoesNotThrow(() -> {
            Query queryMsg = new Query(VALID_QUERY_MESSAGE);
            Assertions.assertSame(0L, queryMsg.getQueryID());
            Assertions.assertSame(0, queryMsg.getRequestedPosts());
        });
    }

    /**
     * Tests the query byte constructor method with a valid array of bytes
     */
    @DisplayName("Valid Query Constructor")
    @Test
    protected void testValidQueryConstructor(){
        Assertions.assertDoesNotThrow(() -> {
            Query queryMsg = new Query(12345L, 36);
            Assertions.assertEquals(12345L, queryMsg.getQueryID());
            Assertions.assertEquals(36, queryMsg.getRequestedPosts());
        });
    }

    /**
     * Tests the encode serialization for a Query Message
     */
    @DisplayName("Testing Encode with empty query")
    @Test
    protected void testEmptyQueryEncode(){
        Assertions.assertDoesNotThrow(() -> {
            Query queryMsg = new Query(VALID_QUERY_MESSAGE);
            byte [] encodeArray = queryMsg.encode();
            Assertions.assertArrayEquals(VALID_QUERY_MESSAGE, encodeArray);
        });
    }

    /**
     * Tests the encode serialization for a Full Query Message
     */
    @DisplayName("Testing Encode with full query")
    @Test
    protected void testFullQueryEncode(){
        Assertions.assertDoesNotThrow(() -> {
            Query queryMsg = new Query(12345L, 36);
            byte [] encodeArray = queryMsg.encode();
            Assertions.assertArrayEquals(queryMsg.encode(), encodeArray);
        });
    }


}
