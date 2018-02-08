package com.metropolis.stocks.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HistoricalStockPrice {
    float openPrice;
    float closePrice;
}
