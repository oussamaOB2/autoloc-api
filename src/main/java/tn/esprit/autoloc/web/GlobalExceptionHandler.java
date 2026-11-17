package tn.esprit.autoloc.web;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tn.esprit.autoloc.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Centralised error handling.
 * <p>
 * Files of the workshop "Chapitre 6 : Spring MVC &amp; Spring Doc" (step 18) placed in the
 * {@code tn.esprit.autoloc.web} package, with the stricter handlers of the project kept:
 * <ul>
 *   <li>{@link ResourceNotFoundException} -&gt; 404</li>
 *   <li>{@link IllegalArgumentException} -&gt; 400</li>
 *   <li>bean validation ({@code @Valid}) -&gt; 400 with the list of invalid fields</li>
 *   <li>{@link DataIntegrityViolationException} -&gt; 409</li>
 *   <li>anything else -&gt; 500, details only in the server log</li>
 * </ul>
 * Extends {@link ResponseEntityExceptionHandler} so that Spring's own MVC errors (unknown route,
 * 405, 415, unreadable JSON...) keep their correct status instead of being swallowed by the
 * generic handler; {@link #handleExceptionInternal} renders them with the same
 * {@link ErrorResponse} body.
 * <p>
 * Note: {@code org.springframework.web.ErrorResponse} is deliberately not imported, the simple
 * name {@code ErrorResponse} refers to the record of this package.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String INTERNAL_MESSAGE = "Une erreur interne est survenue.";
    private static final String CONFLICT_MESSAGE =
            "Opération impossible : contrainte d'intégrité violée (doublon ou donnée liée).";

    /* ------------------------------------------------------------------ helpers */

    private ResponseEntity<Object> build(HttpStatus status, String message, String path) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(LocalDateTime.now(), status.value(),
                        status.getReasonPhrase(), message, path));
    }

    /** Request URI (context path included) of the failing request. */
    private String pathOf(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getRequestURI();
        }
        return request.getDescription(false);
    }

    private String messageOf(Object body, Exception ex) {
        if (body instanceof ProblemDetail problemDetail && problemDetail.getDetail() != null) {
            return problemDetail.getDetail();
        }
        return ex.getMessage();
    }

    /** True when the cause chain contains a database constraint violation. */
    private boolean isConstraintViolation(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String name = current.getClass().getName();
            if (name.endsWith("ConstraintViolationException")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    /* -------------------------------------------------------------- application errors */

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFound(ResourceNotFoundException ex,
                                                          HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex,
                                                         HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException ex,
                                                      HttpServletRequest request) {
        log.warn("Data integrity violation", ex);
        return build(HttpStatus.CONFLICT, CONFLICT_MESSAGE, request.getRequestURI());
    }

    /**
     * A constraint violation detected while the transaction was being committed is wrapped by
     * Spring into a {@link TransactionException}; it is a conflict, not a server error.
     */
    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<Object> handleTransaction(TransactionException ex,
                                                    HttpServletRequest request) {
        if (isConstraintViolation(ex)) {
            log.warn("Constraint violation detected at commit time", ex);
            return build(HttpStatus.CONFLICT, CONFLICT_MESSAGE, request.getRequestURI());
        }
        log.error("Transaction failure", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_MESSAGE, request.getRequestURI());
    }

    /** Unexpected error -&gt; 500. Details are logged server-side, never sent to the client. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex,
                                                         HttpServletRequest request) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_MESSAGE, request.getRequestURI());
    }

    /* -------------------------------------------------------------- framework errors */

    /** Bean Validation errors ({@code @Valid}) -&gt; 400 with the list of invalid fields. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                   HttpHeaders headers,
                                                                   HttpStatusCode status,
                                                                   WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map((FieldError e) -> e.getField() + " : " + e.getDefaultMessage())
                .collect(Collectors.joining(" ; "));
        return build(HttpStatus.BAD_REQUEST, message, pathOf(request));
    }

    /**
     * Renders Spring's own MVC errors (unknown route, 405, 415, unreadable JSON...) with the
     * same {@link ErrorResponse} body instead of the default {@code ProblemDetail} one.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                             Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode status,
                                                             WebRequest request) {
        HttpStatus resolved = HttpStatus.resolve(status.value());
        String reason = resolved != null ? resolved.getReasonPhrase() : status.toString();
        return ResponseEntity.status(status)
                .headers(headers)
                .body(new ErrorResponse(LocalDateTime.now(), status.value(), reason,
                        messageOf(body, ex), pathOf(request)));
    }
}
