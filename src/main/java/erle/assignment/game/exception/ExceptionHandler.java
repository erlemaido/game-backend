package erle.assignment.game.exception;

import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.Collection;
import java.util.Optional;

@ControllerAdvice
public class ExceptionHandler {

    @MessageExceptionHandler(org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException.class)
    @SendTo("/queue/errors")
    public ErrorResult handleValidationException(MethodArgumentNotValidException ex) {
        var errors = Optional.ofNullable(ex.getBindingResult())
                .map(Errors::getFieldErrors)
                .stream()
                .flatMap(Collection::stream)
                .map(ExceptionHandler::createError)
                .toList();

        return new ErrorResult(errors);
    }

    private static Error createError(FieldError error) {
        return new Error(error.getField(), error.getDefaultMessage());
    }

}
