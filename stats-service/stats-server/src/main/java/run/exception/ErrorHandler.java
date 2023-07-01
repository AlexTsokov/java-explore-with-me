package run.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;

@RestControllerAdvice
public class ErrorHandler {

    @Getter
    @AllArgsConstructor
    private static class ErrorResponse {
        private final String error;
    }

    @ExceptionHandler({
            DataIntegrityViolationException.class,
            DateTimeParseException.class,
            IllegalArgumentException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse errorBadRequest(final Throwable e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse errorInternalServerError(final RuntimeException e) {
        return new ErrorResponse(e.getMessage());
    }
}