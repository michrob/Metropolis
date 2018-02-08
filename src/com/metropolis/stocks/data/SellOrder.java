package com.metropolis.stocks.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SellOrder {
    private Symbol symbol;
    private int shares;
}
