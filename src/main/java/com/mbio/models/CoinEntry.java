package com.mbio.models;

/**
 * One coin in the "Spot market" list on mb.io/en-AE/explore.
 */
public class CoinEntry {

    private final String symbol;
    private final String name;
    private final String price;
    private final String change;

    public CoinEntry(String symbol, String name, String price, String change) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
        this.change = change;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getChange() {
        return change;
    }

    @Override
    public String toString() {
        return symbol + " | " + name + " | " + price + " | " + change;
    }
}
