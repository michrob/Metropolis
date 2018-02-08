package com.metropolis.simulator.mutator;


import com.google.common.collect.ImmutableList;
import com.metropolis.stocks.strategy.pluggable.input.Input;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InputMutator implements Mutator<Input> {

    private Map<Class<?>, Mutator> fieldMutators = new HashMap<>();
    private Class<?> classType;


    public InputMutator(final Class<?> classType) {
        this.classType = classType;
        for (final Field field : classType.getDeclaredFields()) {
            if (field.getType().equals(Integer.class)) {
                fieldMutators.put(field.getType(), new IntMutator());
            } else if (field.getType().isEnum()) {
                fieldMutators.put(field.getType(), new EnumMutator());
            } else if (field.getType().equals(Float.class)) {
                fieldMutators.put(field.getType(), new FloatMutator());
            }
        }
    }

    @Override
    public Optional<Input> mutate(final Object object) {
        try {
            Object newObject = classType.newInstance();
            for (final Field field : newObject.getClass().getDeclaredFields()) {
                if (fieldMutators.containsKey(field.getType())) {
                    Mutator mutator = fieldMutators.get(field.getType());

                    if (field.get(object) != null) {
                        field.set(newObject, field.get(object));
                    }

                    Optional mutatedOptional = mutator.mutate(field.get(newObject));

                    if (mutatedOptional.isPresent()) {
                        field.set(newObject, mutatedOptional.get());
                    }
                }
            }

            if (!(newObject instanceof Input)) {
                return Optional.empty();
            }

            return Optional.of((Input) newObject);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Input> makeMutations(final Object object) {
        return ImmutableList.of();
    }

}
