package tn.esprit.autoloc.web;

import java.time.LocalDateTime;

/**
 * Payload returned by every error response of the API.
 * <p>
 * Shape taken from the workshop "Chapitre 6 : Spring MVC &amp; Spring Doc" (annexes):
 * {@code timestamp}, {@code status}, {@code error}, {@code message}, {@code path}.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
