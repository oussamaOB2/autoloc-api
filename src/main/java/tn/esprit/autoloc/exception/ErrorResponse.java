package tn.esprit.autoloc.exception;

import java.time.LocalDateTime;

public record ErrorResponse(int status, String message, LocalDateTime date) {
}
