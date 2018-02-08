package com.metropolis.stocks.strategy.pluggable;


import com.metropolis.stocks.strategy.Strategy;
import com.metropolis.stocks.strategy.pluggable.input.Input;

import java.util.List;

public interface Pluggable {

    void accept(final Strategy strategy, final List<Input> inputs);

    List<Class<?>> getValidInputs();

}
