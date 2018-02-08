package com.metropolis.simulator.mutator;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class EnumMutator implements Mutator<Object> {

    private static final Random random = new Random();

    @Override
    public Optional<Object> mutate(final Object object) {
        if (object == null) {
            return Optional.of(0);
        }
        if (object instanceof Enum) {
            Enum enumObject = (Enum) object;
            Object[] possibleValues = enumObject.getDeclaringClass().getEnumConstants();
            Object newValue = possibleValues[random.nextInt(possibleValues.length)];
            return Optional.of(newValue);
        }
        return Optional.empty();
    }

    @Override
    public List<Object> makeMutations(final Object object) {
        return ImmutableList.of();
    }

}
