package com.metropolis.stocks.strategy.pluggable;


import com.google.common.collect.ImmutableList;
import com.metropolis.stocks.account.Account;
import com.metropolis.stocks.data.Asset;
import com.metropolis.stocks.data.SellOrder;
import com.metropolis.stocks.data.State;
import com.metropolis.stocks.data.Symbol;
import com.metropolis.stocks.market.Market;
import com.metropolis.stocks.strategy.Strategy;
import com.metropolis.stocks.strategy.pluggable.input.Input;
import com.metropolis.stocks.strategy.pluggable.input.sell.HoursOldSellInput;
import com.metropolis.stocks.strategy.pluggable.input.sell.ProfitLossSellInput;
import com.metropolis.util.MathUtil;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Sell implements Pluggable {

    private static final Market staticMarket = Market.instance();


    public static void sell(final Strategy strategy, final Object... inputs) {
        for (final Object object : inputs) {
            if (object instanceof HoursOldSellInput) {
                HoursOldSellInput hoursOldSellInput = (HoursOldSellInput) object;

                Account account = strategy.getAccount();
                Map<String, Asset> assets = account.getOwnedAssets();

                int sharesToSell = Math.max(1, hoursOldSellInput.sharesToSell);
                int hoursOld = Math.max(1, hoursOldSellInput.hoursOld);

                final List<SellOrder> sellOrders = new ArrayList<>();
                for (final Asset asset : assets.values()) {
                    final DateTime buyDate = asset.getBuyDate();
                    final DateTime nowDate = State.getInstance().today();
                    if (buyDate.isBefore(nowDate.minusHours(hoursOld))) {
                        sellOrders.add(new SellOrder(asset.getSymbol(), sharesToSell));
                    }
                }
                account.sellSymbols(sellOrders);
            } else if (object instanceof ProfitLossSellInput) {
                ProfitLossSellInput profitLossSellInput = (ProfitLossSellInput) object;

                Account account = strategy.getAccount();
                Map<String, Asset> assets = account.getOwnedAssets();

                float profitThreshold = profitLossSellInput.getProfitThreshold();
                float lossThreshold = profitLossSellInput.getLossThreshold();
                final Map<Symbol, Float> quotes =
                        staticMarket.retrieveQuotesForSymbols(assets.values().stream().map(Asset::getSymbol).collect(Collectors.toSet()));

                final List<SellOrder> sellOrders = new ArrayList<>();
                for (final Map.Entry<String, Asset> assetEntry : assets.entrySet()) {
                    final Asset asset = assetEntry.getValue();

                    if (!quotes.containsKey(asset.getSymbol())) {
                        continue;
                    }

                    float pricePaid = asset.getPricePerShare();
                    float priceNow = quotes.get(asset.getSymbol());

                    if (pricePaid > priceNow) {
                        float decrease = MathUtil.decimalDecrease(pricePaid, priceNow);
                        if (decrease > lossThreshold) {
                            sellOrders.add(new SellOrder(asset.getSymbol(), asset.getShares()));
                        }
                    } else {
                        float increase = MathUtil.decimalIncrease(pricePaid, priceNow);
                        if (increase > profitThreshold) {
                            sellOrders.add(new SellOrder(asset.getSymbol(), asset.getShares()));
                        }
                    }
                }

                account.sellSymbols(sellOrders);
            }
        }
    }

    @Override
    public void accept(final Strategy strategy, final List<Input> inputs) {
        sell(strategy, inputs);
    }

    @Override
    public List<Class<?>> getValidInputs() {
        return ImmutableList.of(HoursOldSellInput.class, ProfitLossSellInput.class);
    }

}
