package ma.karflow.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Lancée quand l'utilisateur n'a pas les droits nécessaires.
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }

    public UnauthorizedException() {
        super("Accès non autorisé", HttpStatus.FORBIDDEN);
    }
}
