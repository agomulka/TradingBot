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


public class BuyingStrategy implements TradingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(OrdersController.class);
    private final MarketPlugin marketPlugin;

    private final MovingAveragesTasker averagesTasker;


    public BuyingStrategy(MarketPlugin marketPlugin, MovingAveragesTasker averagesTasker) {
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

            if (position == 1) { // tutaj sprawdzanie portfela? w tym miejscu w SellingStrategy jest sprawdzanie CanISell
                final var price = getLastPrice(symbol);
                final var qty = BuyQty(symbol, portfolio);
                if (canIBuy(symbol, portfolio)) {
                    Buy(symbol, qty, price);
                }
            }
        }
    }

    public void Buy(String symbol, long qty, long price) {
        logger.info("Placing buy order of {} ", symbol);
        final var buy = new SubmitOrder.Buy(symbol, UUID.randomUUID().toString(), qty, price);
        ValidatedOrder validatedSell = marketPlugin.buy(buy);
        logger.info("validated buy: {}", validatedSell);
    }

    //dywersyfikacja portfela
    public long BuyQty(String symbol, Portfolio portfolio) {
        // TODO
        //jak dywersyfikowac?

        long amount = (long) averagesTasker.getAverageAmount(symbol);
        return amount;

        int instrumentNumber = hashMapBought.keySet().size();
        int percent = 100 / instrumentNumber;
        Long portfolioValue;
        if (portfolio instanceof Portfolio.Current pc) {
            portfolioValue = pc.cash();

            for (String symbol : hashMapBought.keySet()) {
                List<Long> prices = hashMapBought.get(symbol);
                Long closingPrice = prices.get(0);
                float quantity = (portfolioValue * percent) / (100 * closingPrice);  //number of shares to buy

                if (signalToBuy == true) { // chyba bez tego
                    String tradeID = UUID.randomUUID().toString();
                    Double q = Math.floor(quantity);
                    long qualityLong = q.longValue();
                    //Double cp = Math.floor(closingPrice);
                    //long closingPriceLong = cp.longValue();
                    if (checkIfNotSubmitted(symbol, qualityLong, closingPrice)) {    //sprawdza czy nie zostało wystawione takie zlecenie
                        SubmitOrder.Buy order = new SubmitOrder.Buy(symbol, tradeID, qualityLong, closingPrice);
                        queueToBuy.add(order);
                    }
                }
            }
        }
        return 1;
    }

    // sprawdzić czy wystarcza gotówki na zakup uwzględniając wiszące oferty
    public boolean canIBuy(String symbol, Portfolio portfolio) {
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
        Portfolio portfolio = marketPlugin.portfolio();
        if (portfolio instanceof Portfolio.Current pc) {
            Optional<SubmittedOrder.Buy> any = pc.toBuy().stream()
                    .filter(buy -> buy.instrument().symbol().equals(symbol)
                            && buy.bid().qty() == qualityLong && buy.bid().price() == closingPrice)
                    .findAny();
            return any.isEmpty(); //gdy nie ma takiego zlecenia zwraca True i mozemy je kupić
        }
        return false;
    }
}
