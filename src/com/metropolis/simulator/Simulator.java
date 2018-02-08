package com.metropolis.simulator;

import com.google.common.collect.ImmutableList;
import com.metropolis.EventHandler;
import com.metropolis.stocks.data.Event;
import com.metropolis.stocks.data.State;
import com.metropolis.stocks.strategy.Score;
import com.metropolis.stocks.strategy.Strategy;
import com.metropolis.util.Config;
import com.metropolis.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.*;


@Slf4j
public class Simulator {

    public static final Event[] SIM_EVENTS = {Event.OPEN, Event.CLOSE};
    private static final PersistentPool persistentPool = new PersistentPool();

    private static <T extends Strategy> void simulateDay(final DateTime dateTime, final List<T> strategies) {
        State.getInstance().setToday(dateTime);

        for (final Event event : SIM_EVENTS) {
            EventHandler.dispatchEvent(event, strategies);
            State.getInstance().setToday(State.getInstance().today().plusHours(5));
            strategies.forEach(Strategy::reset);
        }

    }

    public static <T extends Strategy> void doSimulation(final List<T> strategies) {
        int dayDuration = Days.daysBetween(Config.SIM_START_DATE.toLocalDate(), Config.SIM_END_DATE.toLocalDate()).getDays();

        log.info("Running {} day simulation of {} strategies.", dayDuration, strategies.size());
        for (DateTime date = Config.SIM_START_DATE; date.isBefore(Config.SIM_END_DATE); date = date.plusDays(1)) {
            simulateDay(date, strategies);
        }
        strategies.forEach(Strategy::finalize);
        //strategies.stream().filter(strat -> strat.getAccount().getCashBalance() > 30000).forEach(strat -> log.debug("{}", strat.getAccount().toString()));
        //EventHandler.dispatchEvent(Event.PERSIST, strategies);
    }

    public static void main(final String[] args) {
        long startTime = System.currentTimeMillis();

        List<Strategy> strategyMap = persistentPool.getStrategies();

        Strategy strategy;
        if (strategyMap.size() == 0) {
            strategy = new Strategy("SIM");
        } else {
            strategy = strategyMap.iterator().next();
        }

        //strategyMap.forEach(strategy -> Cache.delete(strategy.getName()));
        doSimulation(ImmutableList.of(strategy));

        long endTime = System.currentTimeMillis();

        int dayDuration = Days.daysBetween(Config.SIM_START_DATE.toLocalDate(), Config.SIM_END_DATE.toLocalDate()).getDays();

        log.info("\n{}", JSONUtil.serializeObject(strategy));
        JSONUtil.saveObjectToFile("sim.json", strategy);

        log.info("{} day simulation took {} milliseconds", dayDuration, (endTime - startTime));
    }

}