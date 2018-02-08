package com.metropolis.util;

import java.util.Collection;
import java.util.Optional;
import java.util.Random;

public class RandomUtil {

    private static final Random random = new Random();

    public static boolean nextBoolean() {
        return random.nextBoolean();
    }

    public static int nextInt(final int bound) {
        return bound == 0 ? bound : random.nextInt(bound);
    }

    public static float nextFloat() {
        return random.nextFloat();
    }

    public static <T> T choice(final T redpill, T bluepill) {
        if (Math.random() < .5) {
            return redpill;
        }
        return bluepill;
    }

    public static <E> Optional<E> randomChoice(final Collection<E> e) {
        return e.stream()
                .skip((int) (e.size() * Math.random()))
                .findFirst();
    }
}
