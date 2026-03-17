package ma.karflow.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Lancée quand une ressource demandée n'existe pas.
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resourceName, Object id) {
        super(resourceName + " introuvable avec l'identifiant : " + id, HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
