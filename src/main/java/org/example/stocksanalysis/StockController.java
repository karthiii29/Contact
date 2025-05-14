package org.example.stocksanalysis;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    @PostMapping("/profit-summary")
    public ResponseEntity<ProfitResponse> getProfitSummary(@RequestBody ProfitRequest request) {
        List<StockPrice> stocks = request.getStocks();
        double capital = request.getCapital();

        if (stocks == null || stocks.isEmpty() || capital <= 0) {
            return ResponseEntity.badRequest().build();
        }

//        double totalProfitPercentage = 0;
//        double totalProfit = 0;
//
//        for (StockPrice stock : stocks) {
//            double profit = stock.getCurrentPrice() - stock.getOldPrice();
//            double profitOrLossPercentage = (profit / stock.getOldPrice()) * 100;
//            totalProfitPercentage += profitOrLossPercentage;
//        }
//
//        double currentValue = capital + totalProfit;
//        double returns = totalProfitPercentage / stocks.size();

        int n = stocks.size();

        double totalPercentage = 0;

        for (StockPrice stock : stocks) {
            double oldPrice = stock.getOldPrice();
            double currentPrice = stock.getCurrentPrice();

            double stockReturnPercent = ((currentPrice - oldPrice) / oldPrice) * 100;
            totalPercentage += stockReturnPercent;
        }

        double averageReturnPercent = totalPercentage / n;
        double profitOrLoss = capital * (averageReturnPercent / 100);
        double overallAmount = capital + profitOrLoss;


        ProfitResponse response = new ProfitResponse(capital, overallAmount, averageReturnPercent, stocks);


        return ResponseEntity.ok(response);
    }
}
