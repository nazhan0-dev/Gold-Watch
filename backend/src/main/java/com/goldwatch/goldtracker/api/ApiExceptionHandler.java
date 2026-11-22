package com.goldwatch.goldtracker.api;

import com.goldwatch.goldtracker.domain.HistoryRange;
import com.goldwatch.goldtracker.domain.PriceSeries;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Every error is answered as RFC 9457 problem JSON, e.g.
 * {@code {"status":400,"title":"Invalid parameter","detail":"...","parameter":"range"}}.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /** Wrong value for a typed parameter (unknown type/range, malformed date). */
    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String name = ex.getPropertyName();
        String detail;
        Class<?> required = ex.getRequiredType();
        if (required == PriceSeries.class) {
            detail = "Unknown type '" + ex.getValue() + "'. Allowed values: " + PriceSeries.allowedCodes();
        } else if (required == HistoryRange.class) {
            detail = "Unknown range '" + ex.getValue() + "'. Allowed values: " + HistoryRange.allowedCodes();
        } else if (required == LocalDate.class) {
            detail = "Invalid date '" + ex.getValue() + "'. Expected format YYYY-MM-DD";
        } else {
            detail = "Invalid value '" + ex.getValue() + "' for parameter '" + name + "'";
        }
        return ResponseEntity.badRequest().body(invalidParameter(name, detail));
    }

    @ExceptionHandler(InvalidParameterException.class)
    ResponseEntity<ProblemDetail> handleInvalidParameter(InvalidParameterException ex) {
        return ResponseEntity.badRequest().body(invalidParameter(ex.getParameter(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleUnexpected(Exception ex) {
        log.error("Unhandled API error", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong on our side. Please try again later.");
        problem.setTitle("Internal error");
        return ResponseEntity.internalServerError().body(problem);
    }

    private static ProblemDetail invalidParameter(String parameter, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Invalid parameter");
        problem.setProperty("parameter", parameter);
        return problem;
    }
}
