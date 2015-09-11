package Help;

import java.util.Random;

public final class Helper {


    private Helper() {
    }

    /**
     * @return Random float value between -1 and 1
     */
    public static double randomClamped() {
        Random random = new Random();
        return (random.nextDouble() * 2 - 1);
    }

    public static double randomDouble() {
        Random random = new Random();
        return random.nextDouble();
    }

    /**
     * Note that the lower limit is inclusive, but the upper limit is exclusive.
     *
     * @param min incluse minimum number
     * @param max exclusive maximum number
     * @return random number between min and max
     */
    public static int randomInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    public static double sigmoid(double netInput, double response) {
        return (1 / (1 + Math.exp(-netInput / response)));
    }
}
