package erle.assignment.game.exception;

import java.util.List;

public record ErrorResult(
        List<Error> errors
) {}
