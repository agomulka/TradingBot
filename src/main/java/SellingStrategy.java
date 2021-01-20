import model.Portfolio;
import model.SubmitOrder;
import model.order.SubmittedOrder;
import model.order.ValidatedOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
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

        if (portfolio instanceof Portfolio.Current pc) {
            final var cash = pc.cash();
            logger.info("Available cash: {}", cash);
        }

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

            if (position == -1 && canISell(symbol, portfolio)) {
                final var price = getLastPrice(symbol);
                final var qty = SellQty(symbol, portfolio);
                Sell(symbol, qty, (long) (1.1 * price));
            }
        }
    }

    public void Sell(String symbol, long qty, long price) {
        logger.info("Placing sell order of {} ", symbol);
        final var sell = new SubmitOrder.Sell(symbol, UUID.randomUUID().toString(), qty, price);
        ValidatedOrder validatedSell = marketPlugin.sell(sell);
        logger.info("validated sell: {}", validatedSell);
    }

    //dywersyfikacja portfela dla sprzedaży
    public long SellQty(String symbol, Portfolio portfolio) {
        // TODO
        //jak dywersyfikowac
        long amount = (long) averagesTasker.getAverageAmount(symbol);
        amount = Math.min(amount, portfolio); // TODO TUTAJ
        return amount;

        int instrumentNumber = hashMapSold.keySet().size();
        int percent = 100 / instrumentNumber;
        // int maxPortfolio = 10000;
        Long portfolioValue;
        if (portfolio instanceof Portfolio.Current pc) {
            portfolioValue = pc.cash();
            for (String symbol : hashMapSold.keySet()) {
                List<Long> longs = hashMapSold.get(symbol);
                Long closingPrice = longs.get(0);
                float quantity = (portfolioValue * percent) / (100 * closingPrice);  //numbers of share to sell

                if (signalToSell == true) { // chyba bez tego
                    String tradeID = UUID.randomUUID().toString();
                    Double q = Math.floor(quantity);
                    long qualityLong = q.longValue();
                    if (checkIfNotSubmitted(symbol, qualityLong, closingPrice)) {  //sprawdza czy nie zostało wystawione takie zlecenie
                        SubmitOrder.Sell order = new SubmitOrder.Sell(symbol, tradeID, qualityLong, closingPrice);
                        queueToSell.add(order);
                    }
                }
            }
        }
        return 1;
    }

    //Sprawdza czy posiadamy dany instrument w portfelu
    public boolean canISell(String symbol, Portfolio portfolio) {
        boolean contains;
        if (portfolio instanceof Portfolio.Current pc) {
            contains = pc.portfolio().stream().noneMatch(s -> s.instrument().symbol().equals(symbol));
        } else contains = false;
        return contains;
    }

    //Pobiera cene ostatniej zrealizowanej transakcji
    public long getLastPrice(String symbol) {
        return averagesTasker.getPrices().get(symbol).get(0);
    }

    @Override
    public boolean checkIfNotSubmitted(String symbol, Long qualityLong, Long closingPrice) {
//        Submitted submitted = marketPlugin.submitted();
//        if (submitted instanceof Submitted.Correct sc) {
//            Optional<SubmittedOrder.Sell> any = sc.sell().stream()
//                    .filter(sell -> sell.instrument().symbol().equals(symbol)
//                            && sell.ask().qty() == qualityLong && sell.ask().price() == closingPrice)
//                    .findAny();
//            return any.isEmpty();  //gdy nie ma takiego zlecenia zwraca True i mozemy je kupić
//        }
//        return false;
        Portfolio portfolio = marketPlugin.portfolio();
        if (portfolio instanceof Portfolio.Current pc) {
            Optional<SubmittedOrder.Sell> any = pc.toSell().stream().filter(sell -> sell.instrument().symbol().equals(symbol)
                    && sell.ask().qty() == qualityLong && sell.ask().price() == closingPrice)
                    .findAny();
            return any.isEmpty(); //gdy nie ma takiego zlecenia zwraca True i mozemy je kupić
        }
        return false;
    }
}
