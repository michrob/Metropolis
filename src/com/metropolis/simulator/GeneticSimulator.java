package com.metropolis.simulator;

import com.metropolis.simulator.mutator.StrategyMutator;
import com.metropolis.stocks.strategy.Strategy;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class GeneticSimulator {

    private static final PersistentPool persistentPool = new PersistentPool();
    private static final StrategyInterface strategyInterface = new StrategyInterface();
    private static StrategyMutator strategyMutator = new StrategyMutator(strategyInterface);

    private static void mutateStrategies(final List<Strategy> strategies) {
        final List<Strategy> strategiesToAdd = new ArrayList<>();

        for (final Strategy strategy : strategies) {
            List<Strategy> childStrategies = strategyMutator.makeMutations(strategy);
            strategiesToAdd.addAll(childStrategies);
            strategiesToAdd.add(new Strategy(strategy));
        }

        strategies.clear();
        strategies.addAll(strategiesToAdd);
    }

    private static void selectOutStrategies(final Collection<Strategy> strategies) {

        //strategies.removeIf(strategy -> strategy.getScore().getAverageMonthlyReturn() <= 0);

        double bestNetReturn = 0;
        for (final Strategy strategy : strategies) {
            if (strategy.getScore().getAverageMonthlyReturn() >= bestNetReturn) {
                bestNetReturn = strategy.getScore().getAverageMonthlyReturn();
            }
        }

        for (Iterator<Strategy> iterator = strategies.iterator(); iterator.hasNext(); ) {
            Strategy strategy = iterator.next();
            if (strategy.getScore().getAverageMonthlyReturn() >= bestNetReturn) {
                log.info("{}",strategy.toString());
                continue;
            } else {
                iterator.remove();
                log.info("{}",strategy.toString());
            }
        }

        /*double bestMonthlyReturn = -9001;
        for (final Strategy strategy : strategies) {
            if (strategy.getScore().getAverageMonthlyReturn() >= bestMonthlyReturn) {
                bestMonthlyReturn = strategy.getScore().getAverageMonthlyReturn();
            }
        }

        for (Iterator<Strategy> iterator = strategies.iterator(); iterator.hasNext(); ) {
            Strategy strategy = iterator.next();
            if (strategy.getScore().getAverageMonthlyReturn() >= bestMonthlyReturn) {
                continue;
            } else {
                iterator.remove();
            }
        }*/


        int bestWeight = Integer.MAX_VALUE;
        for (final Strategy strategy : strategies) {
            if (strategy.getWeight() < bestWeight) {
                bestWeight = strategy.getWeight();
            }
        }

        for (Iterator<Strategy> iterator = strategies.iterator(); iterator.hasNext(); ) {
            Strategy strategy = iterator.next();
            if (strategy.getWeight() <= bestWeight) {
                continue;
            } else {
                iterator.remove();
            }
        }

        if (strategies.size() == 0) {
            strategies.add(new Strategy("DEFAULT"));
        }
    }

    public static void main(final String[] args) {
        long startTime = System.currentTimeMillis();

        List<Strategy> bestStrategies = persistentPool.getStrategies();

        Simulator.doSimulation(bestStrategies);

        int i = 0;
        while (i < 20) {

            mutateStrategies(bestStrategies);
            Simulator.doSimulation(bestStrategies);
            selectOutStrategies(bestStrategies);

            log.info("Iteration {}", i);
            bestStrategies.forEach(strat -> log.info("\n{}", strat));

            ++i;
        }

        long endTime = System.currentTimeMillis();

        log.info("{} iterations took {} milliseconds", i, (endTime - startTime));

        persistentPool.setStrategies(bestStrategies);
        persistentPool.persist();
    }
}
