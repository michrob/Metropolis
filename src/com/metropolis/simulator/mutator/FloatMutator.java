package com.metropolis.simulator.mutator;


import com.google.common.collect.ImmutableList;
import com.metropolis.util.RandomUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class FloatMutator implements Mutator<Float> {

    private enum MUTATION {
        STEP,
        RANDOM
    }

    private static final List<MUTATION> mutations = Arrays.asList(MUTATION.values());
    private static final float STEP = .01f;

    @Override
    public Optional<Float> mutate(final Object object) {
        if (object == null) {
            return Optional.of(RandomUtil.nextFloat());
        }

        if (object instanceof Float) {
            Float inputFloat = (Float) object;
            final Float[] outputInteger = new Float[1];
            RandomUtil.randomChoice(mutations).ifPresent(mutation -> outputInteger[0] = doMutation(mutation, inputFloat));
            return Optional.of(Math.abs(outputInteger[0]));
        }
        return Optional.empty();
    }

    @Override
    public List<Float> makeMutations(final Object object) {
        return ImmutableList.of();
    }

    private Float doMutation(final MUTATION mutation, final Float floatObj) {
        switch (mutation) {
            case RANDOM:
                return RandomUtil.nextFloat();
            case STEP:
                return floatObj + RandomUtil.choice(STEP, -STEP);
        }
        return floatObj;
    }

}
