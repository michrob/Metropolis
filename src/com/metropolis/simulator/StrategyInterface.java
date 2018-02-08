package com.metropolis.simulator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.metropolis.stocks.strategy.pluggable.*;
import com.metropolis.util.BasicMethod;
import com.metropolis.util.ClassUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.invoke.MethodHandle;
import java.util.*;

@Slf4j
public class StrategyInterface {

    @Getter
    private final Map<String, Pluggable> pluggableMethods = ImmutableMap.of("Buy", new Buy(),
                                                                                   "Sell", new Sell(),
                                                                                   "Discover", new Discover(),
                                                                                   "Filter", new Filter());

    /*@Getter
    private final Map<String, BasicMethod> basicMethods = new HashMap<>();

    @Getter
    private final Map<Class<?>, Mutator> inputMutators = new HashMap<>();*/

    public StrategyInterface() {
        /*final List<Class<?>> classesToEnumerate = ImmutableList.of(Discover.class, Buy.class, Filter.class, Sell.class);

        for (final Class<?> classType : classesToEnumerate) {
            final Map<String, MethodHandle> consumerMethods = ClassUtil.getConsumers(classType, Strategy.class);

            final List<Class<?>> methodInputs = new ArrayList<>();
            try {
                methodInputs.addAll(((Pluggable) classType.newInstance()).getValidInputs());
            } catch (Exception e) {
                log.warn("Exception getting inputs for {}", classType);
            }

            consumerMethods.entrySet()
                           .forEach(entry -> basicMethods.put(entry.getKey(),
                                                              new BasicMethod(entry.getKey(), entry.getValue(), methodInputs)));

            for (final Class<?> inputType : methodInputs) {
                inputMutators.computeIfAbsent(inputType, mutator -> new InputMutator(inputType));
            }
        }*/

    }

    /*public Collection<BasicMethod> getMethods() {
        return basicMethods.values();
    }*/

}
