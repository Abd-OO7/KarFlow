package ma.karflow.shared.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void success_shouldReturnSuccessTrue() {
        ApiResponse<String> response = ApiResponse.success("hello");

        assertTrue(response.success());
        assertEquals("hello", response.data());
        assertNull(response.message());
        assertNotNull(response.timestamp());
    }

    @Test
    void successWithMessage_shouldIncludeMessage() {
        ApiResponse<Integer> response = ApiResponse.success(42, "Done");

        assertTrue(response.success());
        assertEquals(42, response.data());
        assertEquals("Done", response.message());
    }

    @Test
    void error_shouldReturnSuccessFalse() {
        ApiResponse<Void> response = ApiResponse.error("Something went wrong");

        assertFalse(response.success());
        assertNull(response.data());
        assertEquals("Something went wrong", response.message());
        assertNotNull(response.timestamp());
    }
}
