package ma.karflow.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception métier générique avec code HTTP configurable.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
