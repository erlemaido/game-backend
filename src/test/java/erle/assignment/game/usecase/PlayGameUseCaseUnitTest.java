package erle.assignment.game.usecase;

import erle.assignment.game.common.RandomNumberGeneratorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(MockitoExtension.class)
class PlayGameUseCaseUnitTest {
    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 100;
    private static final BigDecimal PLAYER_BET = BigDecimal.valueOf(40.5);
    private static final int PLAYER_NUMBER = 50;

    @Mock
    private RandomNumberGeneratorService randomNumberGeneratorService;
    @InjectMocks
    private PlayGameUseCase useCase;

    @Test
    void givenRequest_whenPlayerWins_thenReturnCorrectWinningAmount() {
        // given
        when(randomNumberGeneratorService.getInRange(MIN_NUMBER, MAX_NUMBER)).thenReturn(MIN_NUMBER);

        // when
        var response = useCase.handle(createRequest());

        // then
        assertThat(response.winningAmount()).isEqualTo(BigDecimal.valueOf(80.19));
    }

    @Test
    void givenPlayerNumber100_whenPlayerWins_thenReturnCorrectWinningAmount() {
        // given
        when(randomNumberGeneratorService.getInRange(MIN_NUMBER, MAX_NUMBER)).thenReturn(MIN_NUMBER);

        // when
        var response = useCase.handle(new PlayGameUseCase.Request(PLAYER_BET, MAX_NUMBER));

        // then
        assertThat(response.winningAmount()).isEqualTo(BigDecimal.valueOf(4009.50).setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void givenRequest_whenPlayerLoses_thenReturnZero() {
        // given
        when(randomNumberGeneratorService.getInRange(MIN_NUMBER, MAX_NUMBER)).thenReturn(MAX_NUMBER);

        // when
        var response = useCase.handle(createRequest());

        // then
        assertThat(response.winningAmount()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void givenRequest_whenPlayerPlaysMillionRounds_thenCalculateRTP() {
        // given
        var numOfRounds = 1000000;
        var numOfThreads = 24;
        var totalBet = new AtomicReference<>(BigDecimal.ZERO);
        var totalWin = new AtomicReference<>(BigDecimal.ZERO);

        when(randomNumberGeneratorService.getInRange(MIN_NUMBER, MAX_NUMBER)).thenReturn(MIN_NUMBER);

        // when
        try (var executorService = Executors.newFixedThreadPool(numOfThreads)) {
            for (int i = 0; i < numOfRounds; i++) {
                executorService.execute(() -> {
                    var request = createRequest();
                    var response = useCase.handle(request);

                    totalBet.getAndUpdate(total -> total.add(request.bet()));
                    totalWin.getAndUpdate(total -> total.add(response.winningAmount()));
                });
            }
        }

        var rtp = calculateRTP(totalBet.get(), totalWin.get());

        // then
        assertThat(totalBet.get()).isEqualByComparingTo(BigDecimal.valueOf(40500000));
        assertThat(totalWin.get()).isEqualByComparingTo(BigDecimal.valueOf(80190000));
        assertThat(rtp).isEqualByComparingTo(BigDecimal.valueOf(198));
    }

    private static PlayGameUseCase.Request createRequest() {
        return new PlayGameUseCase.Request(PLAYER_BET, PLAYER_NUMBER);
    }

    private static BigDecimal calculateRTP(BigDecimal totalBet, BigDecimal totalWin) {
        return totalWin
                .divide(totalBet, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

}
