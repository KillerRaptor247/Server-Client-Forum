/*
 * Author:     Dante Hart
 * Assignment: Program 0
 * Class:      CSI 4321
 *
 */
package fabric.serialization.test;

import fabric.serialization.Error;
import fabric.serialization.ValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * A JUnit testing class for testing the methods of the Error class
 */
@DisplayName("Testing ValidationException Class")
public class ValidationExceptionTest {

    /**
     * A class to test that Validation Exception is thrown when it should be
     */
    @Nested
    @DisplayName("Testing ValidationException Throw")
    protected class ValidationExceptionThrowTest{
        /**
         * Constructor declared here to clear JavaDoc compile warning
         */
        protected ValidationExceptionThrowTest(){

        }

        /**
         * Tests that a validation exception is thrown appropriately when invalid info is given
         */
        @DisplayName("Testing throw instance of class")
        @Test
        protected void validExceptThrowTest(){
            Assertions.assertThrows(ValidationException.class, () ->{
                int invalidCode = 044;
                Error e = new Error(invalidCode,"Hubba balooey");
            });
        }
    }
}
