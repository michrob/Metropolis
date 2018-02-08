package com.metropolis.simulator;


import com.fasterxml.jackson.core.type.TypeReference;
import com.metropolis.stocks.strategy.Strategy;
import com.metropolis.util.BasicMethod;
import com.metropolis.util.JSONUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PersistentPool {

    private static final String filename = "population.json";
    private static final StrategyInterface strategyInterface = new StrategyInterface();

    @Getter
    @Setter
    private List<Strategy> strategies = new ArrayList<>();

    public PersistentPool() {
        deserialize();
        if (strategies.size() == 0) {
            strategies.add(new Strategy("DEFAULT"));
        }
    }

    public synchronized void persist() {
        List<Strategy> newStrategies = new ArrayList<>();
        for (final Strategy strategy : strategies) {
            newStrategies.add(new Strategy(strategy));
        }
        JSONUtil.saveObjectToFile(filename, newStrategies);
    }

    private void deserialize() {
        List<Strategy> newStrategies = new ArrayList<>();
        try {
            newStrategies = JSONUtil.objectMapper.readValue(new File(filename), new TypeReference<List<Strategy>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        strategies.addAll(newStrategies);
    }

}
