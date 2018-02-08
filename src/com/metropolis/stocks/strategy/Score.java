package com.metropolis.stocks.strategy;


import com.metropolis.stocks.account.Account;
import com.metropolis.stocks.data.State;
import com.metropolis.util.MathUtil;
import com.metropolis.util.Statistics;
import lombok.Data;

import java.util.*;

@Data
public class Score {

    final Map<Integer, Float> monthlyCashBalances = new HashMap<>();

    float netReturn;
    double averageMonthlyReturn;
    double averageMonthlyReturnStdDev;

    public Score() {
    }

    public void updateScore(final Account account) {
        int month = State.getInstance().today().getMonthOfYear();
        if (monthlyCashBalances.keySet().contains(month)) {
            return;
        }
        monthlyCashBalances.put(State.getInstance().today().getMonthOfYear(),
                                account.computeNetWorth());
    }

    public void finalizeScore(final Account account) {
        netReturn = account.computePercentIncrease();

        List<Float> monthlyReturns = new ArrayList<>();
        for (final Integer i : monthlyCashBalances.keySet()) {
            if (!monthlyCashBalances.keySet().contains(i + 1)) {
                continue;
            }
            float pctReturn = MathUtil.decimalIncrease(monthlyCashBalances.get(i),
                                                       monthlyCashBalances.get(i + 1));
            monthlyReturns.add(pctReturn);
        }

        OptionalDouble avgReturns = monthlyReturns.stream().mapToDouble(Float::doubleValue).average();
        averageMonthlyReturn = avgReturns.orElse(0);

        Statistics statistics = new Statistics(monthlyReturns.stream().mapToDouble(Float::doubleValue).toArray());
        averageMonthlyReturnStdDev = statistics.getStdDev();
    }

}
