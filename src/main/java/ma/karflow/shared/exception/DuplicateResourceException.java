package ma.karflow.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Lancée quand on tente de créer une ressource qui existe déjà (conflit d'unicité).
 */
public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(String resourceName, String field, Object value) {
        super(resourceName + " avec " + field + " '" + value + "' existe déjà", HttpStatus.CONFLICT);
    }
}
