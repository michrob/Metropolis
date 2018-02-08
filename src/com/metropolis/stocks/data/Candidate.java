package com.metropolis.stocks.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Candidate {
    private Symbol symbol;
    private Float quote;
}
