package mainservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Error handleValidationException(final MethodArgumentNotValidException exception) {
        log.error(exception.toString());
        return new Error(getErrors(exception),
                String.format("В поле: %s. Ошибка: %s", Objects.requireNonNull(exception.getFieldError()).getField(),
                        exception.getFieldError().getDefaultMessage()),
                "Неверный запрос",
                HttpStatus.BAD_REQUEST.name(),
                LocalDateTime.now());
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            ConstraintViolationException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Error handleValidationException(final RuntimeException exception) {
        log.error(exception.toString());
        return new Error(getErrors(exception),
                exception.getMessage(),
                "Неверный запрос",
                HttpStatus.BAD_REQUEST.name(),
                LocalDateTime.now());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Error handleNotFoundException(final NotFoundException exception) {
        log.error(exception.toString());
        return new Error(getErrors(exception),
                exception.getMessage(),
                "Объект не найден",
                HttpStatus.NOT_FOUND.name(),
                LocalDateTime.now());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Error handleDataIntegrityViolationException(final DataIntegrityViolationException exception) {
        log.error(exception.toString());
        return new Error(getErrors(exception),
                exception.getMessage(),
                "Ошибка данных",
                HttpStatus.CONFLICT.name(),
                LocalDateTime.now());
    }

    @ExceptionHandler(DataException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Error handleWrongDataException(final DataException exception) {
        log.error(exception.toString());
        return new Error(getErrors(exception),
                exception.getMessage(),
                "Ошибка данных",
                HttpStatus.CONFLICT.name(),
                LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Error handleException(final RuntimeException exception) {
        log.error("Error 400: {}", exception.getMessage(), exception);
        return new Error(getErrors(exception),
                exception.getMessage(),
                "Ошибка данных",
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                LocalDateTime.now());
    }

    private String getErrors(Exception exception) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
