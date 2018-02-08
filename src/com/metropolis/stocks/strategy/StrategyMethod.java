package com.metropolis.stocks.strategy;


import com.metropolis.stocks.strategy.pluggable.Pluggable;
import com.metropolis.stocks.strategy.pluggable.input.Input;
import com.metropolis.util.BasicMethod;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class StrategyMethod {

    private Pluggable pluggable;
    private List<Input> parameters;

    public StrategyMethod(final Pluggable pluggable) {
        this.pluggable = pluggable;
        this.parameters = new ArrayList<>();
    }

    public StrategyMethod(final StrategyMethod other) {
        this.pluggable = other.getPluggable();
        this.parameters = new ArrayList<>(other.getParameters());
    }

    public void addParameters(final List<Input> input) {
        input.forEach(this::addParameter);
    }

    public void addParameter(final Object input) {
        if (!(input instanceof Input)) {
            return;
        }
        parameters.add((Input) input);
    }

    public void invoke(final Strategy strategy) {
        try {
            pluggable.accept(strategy, parameters);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "StrategyMethod{" + pluggable + ", " + parameters + "}";
    }
}
