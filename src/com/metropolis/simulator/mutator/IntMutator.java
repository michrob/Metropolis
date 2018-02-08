package com.metropolis.simulator.mutator;


import com.google.common.collect.ImmutableList;
import com.metropolis.util.RandomUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class IntMutator implements Mutator<Integer> {

    private static final List<MUTATION> mutations = Arrays.asList(MUTATION.values());
    private static final int STEP = 1;

    public IntMutator() {
    }

    private Integer doMutation(final MUTATION mutation, final Integer integer) {
        switch (mutation) {
            case RANDOM:
                return RandomUtil.nextInt(100);
            case STEP:
                return integer + RandomUtil.choice(-1, 1);
        }
        return 1;
    }

    @Override
    public Optional<Integer> mutate(final Object object) {
        if (object == null) {
            return Optional.of(1);
        }

        if (object instanceof Integer) {
            Integer inputInteger = (Integer) object;
            final Integer[] outputInteger = new Integer[1];
            RandomUtil.randomChoice(mutations).ifPresent(mutation -> outputInteger[0] = doMutation(mutation, inputInteger));
            return Optional.of(Math.max(1, outputInteger[0]));
        }
        return Optional.empty();
    }

    @Override
    public List<Integer> makeMutations(final Object object) {
        return ImmutableList.of();
    }

    private enum MUTATION {
        STEP,
        RANDOM
    }

}
