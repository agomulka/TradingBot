import model.Portfolio;
import model.SubmitOrder;
import model.order.ValidatedOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.UUID;



public class SellingStrategy implements TradingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(OrdersController.class);
    private final MarketPlugin marketPlugin;

    private final MovingAveragesTasker averagesTasker;

    public SellingStrategy(MarketPlugin marketPlugin, MovingAveragesTasker averagesTasker) {
        this.marketPlugin = marketPlugin;
        this.averagesTasker = averagesTasker;
    }

    @Override
    public void trade() {
        Portfolio portfolio = marketPlugin.portfolio();

//        if (portfolio instanceof Portfolio.Current pc) {
//            final var cash = pc.cash();
//            logger.info("Available cash: {}", cash);
//        }

        HashMap<String, Double> shortAveragesLast = averagesTasker.getAveragePrices(true, true);
        HashMap<String, Double> shortAveragesBefore = averagesTasker.getAveragePrices(true, false);
        HashMap<String, Double> longAveragesLast = averagesTasker.getAveragePrices(false, true);
        HashMap<String, Double> longAveragesBefore = averagesTasker.getAveragePrices(false, false);

        for (String symbol : shortAveragesLast.keySet()) {

            final var avgS = shortAveragesLast.get(symbol);
            final var avgSB = shortAveragesBefore.get(symbol);
            final var avgL = longAveragesLast.get(symbol);
            final var avgLB = longAveragesBefore.get(symbol);

            final var position = averagesTasker.signal(avgS, avgL) - averagesTasker.signal(avgSB, avgLB);

            if (position == -1) {
                final var price = getLastPrice(symbol);
                final var qty = SellQty(symbol, portfolio);
                if (qty > 0 && checkIfNotSubmitted(symbol, qty, price)) {
                    Sell(symbol, qty, price);
                }
            }
        }
    }

    public void Sell(String symbol, long qty, long price) {
        logger.info("Placing sell order of {} ", symbol);
        final var sell = new SubmitOrder.Sell(symbol, UUID.randomUUID().toString(), qty, price);
        ValidatedOrder validatedSell = marketPlugin.sell(sell);
        logger.info("validated sell: {}", validatedSell);
    }

    //zwroci 0 jeśli nie mamy danej akcji w portfelu
    public long SellQty(String symbol, Portfolio portfolio) {
        long amount = (long) averagesTasker.getAverageAmount(symbol);
        if (portfolio instanceof Portfolio.Current pc) {
            if (canISell(symbol, portfolio)) {
                final var qtyInPortfolio = pc.portfolio().stream().filter(sell -> sell.instrument().symbol().equals(symbol)).findFirst().get().qty();
                return Math.min(amount, qtyInPortfolio);
            } else return 0;
        } else return 0;

//        int instrumentNumber = hashMapSold.keySet().size();
//        int percent = 100 / instrumentNumber;
//        // int maxPortfolio = 10000;
//        Long portfolioValue;
//        if (portfolio instanceof Portfolio.Current pc) {
//            portfolioValue = pc.cash();
//            for (String symbol : hashMapSold.keySet()) {
//                List<Long> longs = hashMapSold.get(symbol);
//                Long closingPrice = longs.get(0);
//                float quantity = (portfolioValue * percent) / (100 * closingPrice);  //numbers of share to sell
//
//            }
//        }


    }

    //Sprawdza czy posiadamy dany instrument w portfelu
    public boolean canISell(String symbol, Portfolio portfolio) {
        boolean contains;
        if (portfolio instanceof Portfolio.Current pc) {
            contains = pc.portfolio().stream().anyMatch(s -> s.instrument().symbol().equals(symbol));
        } else contains = false;
        return contains;
    }

    //Pobiera cene ostatniej zrealizowanej transakcji
    public long getLastPrice(String symbol) {
        return averagesTasker.getPrices().get(symbol).get(0);
    }

    @Override
    //gdy nie ma takiego zlecenia zwraca true
    public boolean checkIfNotSubmitted(String symbol, Long qty, Long price) {
        Portfolio portfolio = marketPlugin.portfolio();
        if (portfolio instanceof Portfolio.Current pc) {
            return pc.toSell().stream().filter(sell -> sell.instrument().symbol().equals(symbol)
                    && sell.ask().qty() == qty && sell.ask().price() == price)
                    .findAny().isEmpty();
        }
        return false;
    }
}
