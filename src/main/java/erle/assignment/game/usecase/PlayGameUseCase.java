package erle.assignment.game.usecase;

import erle.assignment.game.common.RandomNumberGeneratorService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Validated
@AllArgsConstructor
public class PlayGameUseCase {

    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 100;
    private final RandomNumberGeneratorService randomNumberGeneratorService;

    public Response handle(Request request) {
        return playerWins(request) ? new Response(calculateWin(request)) : new Response(BigDecimal.ZERO);
    }

    private boolean playerWins(Request request) {
        return request.number > randomNumberGeneratorService.getInRange(MIN_NUMBER, MAX_NUMBER);
    }

    private BigDecimal calculateWin(Request request) {
        if (request.number == 100) {
            return request.bet.multiply(BigDecimal.valueOf(99)).setScale(2, RoundingMode.HALF_UP);
        }
        return request.bet
                .multiply(BigDecimal.valueOf(99)
                        .divide(BigDecimal.valueOf(100)
                                .subtract(BigDecimal.valueOf(request.number)), 2, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);

    }

    public record Request(

            @NotNull
            @Positive(message = "Bet must be bigger than 0")
            BigDecimal bet,

            @NotNull
            @Min(value = MIN_NUMBER, message = "Number should not be less than " + MIN_NUMBER)
            @Max(value = MAX_NUMBER, message = "Number should not be greater than " + MAX_NUMBER)
            Integer number
    ) { }

    public record Response(
            BigDecimal winningAmount
    ) { }

}
