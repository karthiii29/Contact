package org.common.stocksanalysis;

import java.util.List;

public class ProfitRequest {

    private double capital;
    private List<StockPrice> stocks;

    public ProfitRequest() {
    }

    public double getCapital() {
        return capital;
    }

    public void setCapital(double capital) {
        this.capital = capital;
    }

    public List<StockPrice> getStocks() {
        return stocks;
    }

    public void setStocks(List<StockPrice> stocks) {
        this.stocks = stocks;
    }
}

