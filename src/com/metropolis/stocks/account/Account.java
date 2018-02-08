package com.metropolis.stocks.account;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.metropolis.stocks.data.*;
import com.metropolis.stocks.market.Market;
import com.metropolis.stocks.source.WSJ;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Data
@JsonIgnoreProperties({"unfulfillableOrders"})
public class Account {

    public static final int MINIMUM_SECURITY_PRICE = 5;  // Actual minimum is $3

    private static final int MINIMUM_BALANCE = 25000;
    private static final int SPENDABLE_CASH = 10000;
    private static final int STARTING_BALANCE = MINIMUM_BALANCE + SPENDABLE_CASH;

    private static final Market staticMarket = Market.instance();

    private String accountName;
    private float cashBalance;

    // MARKET:SYMBOL -> Asset
    private Map<String, Asset> ownedAssets = new HashMap<>();
    private List<BuyOrder> unfulfillableOrders = new ArrayList<>();

    public Account() {
    }

    public Account(final String accountName) {
        this.accountName = accountName;
        this.cashBalance = STARTING_BALANCE;
    }

    public Account(final Account otherAccount) {
        this.accountName = otherAccount.getAccountName();
        this.cashBalance = STARTING_BALANCE;
    }

    public void buySymbols(final List<BuyOrder> buyOrders) {
        final Map<Symbol, Float> quotes =
                staticMarket.retrieveQuotesForSymbols(buyOrders.stream().map(BuyOrder::getSymbol).collect(Collectors.toSet()));

        float totalSpent = 0;

        final List<Asset> assetsBought = new ArrayList<>();

        for (final BuyOrder buyOrder : buyOrders) {
            final Symbol symbol = buyOrder.getSymbol();
            final int shares = buyOrder.getShares();

            if (!quotes.containsKey(symbol)) {
                log.debug("No quote for symbol {}", symbol);
                unfulfillableOrders.add(buyOrder);
                continue;
            }

            final float price = quotes.get(symbol);

            if (price <= MINIMUM_SECURITY_PRICE) {
                log.trace("Symbol {} costs {} per share which is less than minimum of {}.",
                          symbol, price, MINIMUM_SECURITY_PRICE);
                continue;
            }

            final float pricePaid = (price * shares);

            if (this.cashBalance - totalSpent - pricePaid < MINIMUM_BALANCE) {
                unfulfillableOrders.add(buyOrder);
                continue;
            }

            totalSpent += price * shares;

            Asset asset = new Asset(symbol, shares, State.getInstance().today(), price);
            assetsBought.add(asset);
        }

        if (assetsBought.size() == 0) {
            return;
        }

        log.debug("Buying {} assets for total of ${}", assetsBought, totalSpent);

        this.cashBalance = this.cashBalance - totalSpent;
        for (final Asset asset : assetsBought) {
            if (!ownedAssets.containsKey(asset.getSymbol().toString())) {
                ownedAssets.put(asset.getSymbol().toString(), asset);
                continue;
            }

            final Asset existingAsset = ownedAssets.get(asset.getSymbol().toString());
            existingAsset.add(asset.getShares());
        }

        assetsBought.forEach(asset -> ownedAssets.put(asset.getSymbol().toString(), asset));
    }

    public void sellSymbols(final List<SellOrder> sellOrders) {
        final Map<Symbol, Float> quotes =
                staticMarket.retrieveQuotesForSymbols(sellOrders.stream().map(SellOrder::getSymbol).collect(Collectors.toSet()));

        float totalSale = 0;
        int symbolsSold = 0;

        Set<String> sold = new HashSet<>();
        for (final SellOrder sellOrder : sellOrders) {
            final Symbol symbol = sellOrder.getSymbol();

            if (!quotes.containsKey(symbol)) {
                //log.debug("No quote for symbol {}", symbol);
                continue;
            }

            final float price = quotes.get(symbol);

            final Asset existingAsset = ownedAssets.get(symbol.toString());

            int sharesToSell = sellOrder.getShares();
            if (sharesToSell > existingAsset.getShares()) {
                sharesToSell = existingAsset.getShares();
            }

            existingAsset.subtract(sharesToSell);

            if (existingAsset.getShares() == 0) {
                ownedAssets.remove(symbol.toString());
            }

            sold.add(sellOrder.getSymbol().getTicker());

            totalSale += price * sharesToSell;
            symbolsSold++;
        }

        if (symbolsSold == 0) {
            return;
        }

        this.cashBalance += totalSale;

        log.debug("Selling {} for total of ${}", sellOrders, totalSale);
    }

    private Float computePenalty() {
        float totalPrice = 0;
        for (final BuyOrder buyOrder : unfulfillableOrders) {
            totalPrice += buyOrder.getShares() * WSJ.getMaxPrice(buyOrder.getSymbol().getTicker());
        }
        return 0.0f;
    }

    private Float computeNonliquidValue() {
        Map<Symbol, Float> quotesForAssets =
                staticMarket.retrieveQuotesForSymbols(ownedAssets.values().stream().map(Asset::getSymbol).collect(Collectors.toSet()));

        Double assetsValue = quotesForAssets.values().stream().mapToDouble(Float::doubleValue).sum();

        return assetsValue.floatValue() - computePenalty();
    }

    public Float computeNetWorth() {
        return cashBalance + computeNonliquidValue();
    }

    private Float computeProfit() {
        float networth = computeNetWorth();
        return networth - STARTING_BALANCE;
    }

    public Float computePercentIncrease() {
        float profit = computeProfit();
        return profit / SPENDABLE_CASH;
    }

    @Override
    public String toString() {
        return String.format("Account{%s, balance %s, netw %s, pct %s}",
                             accountName, cashBalance, computeNetWorth(), computePercentIncrease());
    }
}
