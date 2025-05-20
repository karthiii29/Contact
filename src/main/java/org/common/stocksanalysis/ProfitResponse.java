package org.common.stocksanalysis;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfitResponse {

    private final String investment;
    private final String currentValue;
    private final String totalReturnPercentage;
    private final String totalReturnRupees;
    private final Data data;

    public ProfitResponse(double investment, double currentValue, double totalReturnPercentage, List<StockPrice> stocks) {
        this.investment = formatCurrency(investment);
        this.currentValue = formatCurrency(currentValue);
        this.totalReturnPercentage = formatPercentage(totalReturnPercentage);
        this.totalReturnRupees = formatCurrency(investment * (totalReturnPercentage / 100));

        this.data = new Data(stocks.size(), 0, 0);

        for (StockPrice stock : stocks) {
            if (stock.getCurrentPrice() > stock.getOldPrice()) {
                this.data.incrementProfitStocks();
            } else if (stock.getCurrentPrice() < stock.getOldPrice()) {
                this.data.incrementLossStocks();
            }
        }

    }

    private String formatCurrency(double amount) {
        // Format using Indian grouping: 1,00,000 etc.
        DecimalFormat formatter = new DecimalFormat("##,##,###.##", new DecimalFormatSymbols(Locale.ENGLISH));
        String formatted = formatter.format(amount);
        return "₹" + formatted;
    }

    private String formatPercentage(double percentage) {
        return (percentage % 1 == 0)
                ? String.format("%.0f%%", percentage)
                : String.format("%.2f%%", percentage);
    }

    // Getters
    public String getInvestment() {
        return investment;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public String getTotalReturnPercentage() {
        return totalReturnPercentage;
    }

    public String getTotalReturnRupees() {
        return totalReturnRupees;
    }


    public Data getData() {
        return data;
    }

    // Nested data summary class
    public static class Data {
        private final int totalStocks;
        private int profitStocks;
        private int lossStocks;

        public Data(int totalStocks, int profitStocks, int lossStocks) {
            this.totalStocks = totalStocks;
            this.profitStocks = profitStocks;
            this.lossStocks = lossStocks;
        }

        public int getTotalStocks() {
            return totalStocks;
        }

        public int getProfitStocks() {
            return profitStocks;
        }

        public int getLossStocks() {
            return lossStocks;
        }

        public void incrementProfitStocks() {
            this.profitStocks++;
        }

        public void incrementLossStocks() {
            this.lossStocks++;
        }
    }
}






