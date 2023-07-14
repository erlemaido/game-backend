package erle.assignment.game.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RandomNumberGeneratorServiceUnitTest {

    private final RandomNumberGeneratorService service = new RandomNumberGeneratorService();

    @Test
    void givenMinAndMaxNumber_whenGeneratingRandomNumber_thenGeneratedNumberInRange() {
        // given
        var minNumber = 1;
        var maxNumber = 100;

        // when
        var randomNumber = service.getInRange(minNumber, maxNumber);

        // then
        assertThat(randomNumber)
                .isGreaterThanOrEqualTo(minNumber)
                .isLessThanOrEqualTo(maxNumber);
    }

    @Test
    void givenGreaterMinThanAllowed_whenGeneratingRandomNumber_thenThrowException() {
        // given
        var minNumber = 100;
        var maxNumber = 1;

        // when
        assertThrows(IllegalArgumentException.class, () -> service.getInRange(minNumber, maxNumber));
    }

}
