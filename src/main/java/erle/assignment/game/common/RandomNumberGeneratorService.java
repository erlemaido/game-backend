package erle.assignment.game.common;

import org.springframework.stereotype.Service;

import java.util.Random;


@Service
public class RandomNumberGeneratorService {
    private static final Random random = new Random();

    public int getInRange(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("Max number must be greater than min!");
        }
        return random.nextInt(min, max + 1);
    }
}
