package com.metropolis.simulator.mutator;


import com.google.common.collect.ImmutableList;
import com.metropolis.simulator.Simulator;
import com.metropolis.simulator.StrategyInterface;
import com.metropolis.stocks.data.Event;
import com.metropolis.stocks.strategy.Strategy;
import com.metropolis.stocks.strategy.StrategyMethod;
import com.metropolis.stocks.strategy.pluggable.Pluggable;
import com.metropolis.stocks.strategy.pluggable.input.Input;
import com.metropolis.util.BasicMethod;
import com.metropolis.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class StrategyMutator implements Mutator<Strategy> {

    private static final List<MUTATION> mutations = Arrays.asList(MUTATION.values());
    private final StrategyInterface strategyInterface;

    public StrategyMutator(final StrategyInterface strategyInterface) {
        this.strategyInterface = strategyInterface;
    }

    private void addRandomMethods(final Strategy strategy, final int methodsToAdd) {
        Collection<Pluggable> pluggables = strategyInterface.getPluggableMethods().values();

        for (int i = 0; i < methodsToAdd; ++i) {
            final Optional<Pluggable> randomPluggable =
                    pluggables.stream().skip((int) (pluggables.size() * Math.random())).findFirst();
            if (!randomPluggable.isPresent()) {
                continue;
            }

            final Event randomEvent = Simulator.SIM_EVENTS[RandomUtil.nextInt(Simulator.SIM_EVENTS.length)];

            final StrategyMethod newStrategyMethod = new StrategyMethod(randomPluggable.get());

            List<Class<?>> validInputs = newStrategyMethod.getBasicMethod().getValidInputs();
            Class<?> randomClass = validInputs.get(RandomUtil.nextInt(validInputs.size()));

            if (!strategyInterface.getInputMutators().containsKey(randomClass)) {
                continue;
            }

            final Mutator objectMutator = strategyInterface.getInputMutators().get(randomClass);

            Object randomInstance;
            try {
                randomInstance = randomClass.newInstance();
            } catch (Exception e) {
                log.warn("Exception instantiating inputs for {}", randomClass);
                continue;
            }

            final Optional optionalMutation = objectMutator.mutate(randomInstance);
            if (!optionalMutation.isPresent()) {
                log.warn("Exception mutating Object {}", randomInstance);
                continue;
            }

            Input mutatedInput = null;

            Object mutatedObject = optionalMutation.get();
            if (mutatedObject instanceof Input) {
                mutatedInput = (Input) mutatedObject;
            }

            if (mutatedInput == null) {
                continue;
            }

            newStrategyMethod.addParameter(mutatedInput);

            final List<StrategyMethod> methodsForEvent =
                    strategy.getStrategyMethods().computeIfAbsent(randomEvent, list -> new ArrayList<>());

            methodsForEvent.add(RandomUtil.nextInt(methodsForEvent.size()), newStrategyMethod);
        }
    }

    private void dropRandomInputs(final Strategy strategy, final int parametersToDrop) {
        for (int i = 0; i < parametersToDrop; ++i) {
            final Optional<Event> randomEventOptional = RandomUtil.randomChoice(strategy.getStrategyMethods().keySet());
            if (!randomEventOptional.isPresent()) {
                continue;
            }

            final Event randomEvent = randomEventOptional.get();
            if (!strategy.getStrategyMethods().containsKey(randomEvent)) {
                continue;
            }

            final List<StrategyMethod> strategyMethods = strategy.getStrategyMethods().get(randomEvent);
            final Optional<StrategyMethod> optionalStrategyMethod = RandomUtil.randomChoice(strategyMethods);

            if (!optionalStrategyMethod.isPresent()) {
                continue;
            }

            StrategyMethod strategyMethod = optionalStrategyMethod.get();

            if (strategyMethod.getParameters().size() > 0) {
                strategyMethod.getParameters().remove(RandomUtil.nextInt(strategyMethod.getParameters().size()));
            }

            if (strategyMethod.getParameters().size() == 0) {
                strategyMethods.remove(strategyMethod);
            }
        }
    }

    private void mutateInputs(final Strategy strategy) {
        for (final SortedMap.Entry<Event, List<StrategyMethod>> entry : strategy.getStrategyMethods().entrySet()) {

            List<StrategyMethod> newMethods = new ArrayList<>();
            for (final StrategyMethod strategyMethod : entry.getValue()) {
                final StrategyMethod newStrategyMethod = new StrategyMethod(strategyMethod);

                final List<Input> newParameters = new ArrayList<>();
                for (final Input param : newStrategyMethod.getParameters()) {

                    final Map<Class<?>, Mutator> inputMutators = strategyInterface.getInputMutators();
                    if (inputMutators.containsKey(param.getClass())) {

                        Optional childInputOptional = inputMutators.get(param.getClass()).mutate(param);
                        if (childInputOptional.isPresent() && childInputOptional.get() instanceof Input) {
                            newParameters.add((Input) childInputOptional.get());
                        }
                    }
                }

                newStrategyMethod.setParameters(newParameters);
                newMethods.add(newStrategyMethod);
            }

            entry.setValue(newMethods);
        }
    }

    private Strategy doMutation(final MUTATION mutation, final Strategy inputStrategy) {
        final Strategy childStrategy = new Strategy(inputStrategy);

        int mutationsToApply = RandomUtil.nextInt(25) + 1;

        switch (mutation) {
            case MUTATE_INPUTS:
                mutateInputs(childStrategy);
                break;
            case ADD_RANDOM_METHODS:
                addRandomMethods(childStrategy, mutationsToApply);
                break;
            case DROP_RANDOM_PARAMETERS:
                dropRandomInputs(childStrategy, mutationsToApply);
        }

        return childStrategy;
    }

    @Override
    public Optional<Strategy> mutate(final Object object) {
        if (!(object instanceof Strategy)) {
            return Optional.empty();
        }

        final Strategy parentStrategy = (Strategy) object;
        final Strategy[] childStrategy = {new Strategy(parentStrategy)};

        RandomUtil.randomChoice(mutations).ifPresent(mutation -> childStrategy[0] = doMutation(mutation, childStrategy[0]));

        return Optional.of(childStrategy[0]);
    }

    @Override
    public List<Strategy> makeMutations(final Object object) {
        if (!(object instanceof Strategy)) {
            return ImmutableList.of();
        }

        final List<Strategy> mutatedStrategies = new ArrayList<>();
        final Strategy parentStrategy = (Strategy) object;

        int weight = parentStrategy.getWeight();
        weight = Math.min(20, weight);
        weight = Math.max(50, weight);

        for (final MUTATION mutation : mutations) {
            for (int i = 0; i < 15; ++i) {
                mutatedStrategies.add(doMutation(mutation, parentStrategy));
            }
        }

        return mutatedStrategies;
    }

    private enum MUTATION {
        ADD_RANDOM_METHODS,
        MUTATE_INPUTS,
        DROP_RANDOM_PARAMETERS
    }
}
