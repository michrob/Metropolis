package com.metropolis.simulator.mutator;


import java.util.List;
import java.util.Optional;

public interface Mutator<T> {

    public Optional<T> mutate(final Object object);

    public List<T> makeMutations(final Object object);

}
