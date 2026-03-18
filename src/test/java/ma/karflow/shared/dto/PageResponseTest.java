package ma.karflow.shared.dto;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResponseTest {

    @Test
    void from_withMapper_shouldMapContent() {
        Page<Integer> page = new PageImpl<>(List.of(1, 2, 3), PageRequest.of(0, 10), 3);

        PageResponse<String> response = PageResponse.from(page, String::valueOf);

        assertEquals(List.of("1", "2", "3"), response.content());
        assertEquals(0, response.page());
        assertEquals(10, response.size());
        assertEquals(3, response.totalElements());
        assertEquals(1, response.totalPages());
        assertTrue(response.first());
        assertTrue(response.last());
    }

    @Test
    void from_direct_shouldPreserveContent() {
        Page<String> page = new PageImpl<>(List.of("a", "b"), PageRequest.of(0, 5), 2);

        PageResponse<String> response = PageResponse.from(page);

        assertEquals(List.of("a", "b"), response.content());
        assertEquals(2, response.totalElements());
    }
}
