package com.metropolis.stocks.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BuyOrder {
    private Symbol symbol;
    private int shares;
}
