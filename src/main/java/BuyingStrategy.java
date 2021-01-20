import model.History;
import model.Portfolio;
import model.SubmitOrder;
import model.order.Instrument;
import model.order.ProcessedOrder;
import model.order.SubmittedOrder;
import model.order.ValidatedOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

// generalnie gotowe

public class BuyingStrategy implements TradingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(OrdersController.class);
    private final MarketPlugin marketPlugin;

    private final MovingAveragesTasker averagesTasker;

    HashMap<String, List<Long>> hashMapBought = new HashMap<>();
    Queue<SubmitOrder.Buy> queueToBuy = new LinkedList<>();


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

        HashMap<String, Double> shortAveragesLast = averagesTasker.getAverages(true, true);
        HashMap<String, Double> shortAveragesBefore = averagesTasker.getAverages(true, false);
        HashMap<String, Double> longAveragesLast = averagesTasker.getAverages(false, true);
        HashMap<String, Double> longAveragesBefore = averagesTasker.getAverages(false, false);

        for (String symbol : shortAveragesLast.keySet()) {

            final var avgS = shortAveragesLast.get(symbol);
            final var avgSB = shortAveragesBefore.get(symbol);
            final var avgL = longAveragesLast.get(symbol);
            final var avgLB = longAveragesBefore.get(symbol);

            final var position = averagesTasker.signal(avgS, avgL) - averagesTasker.signal(avgSB, avgLB);

            if (position == 1) { // tutaj sprawdzanie portfela? w tym miejscu w SellingStrategy jest sprawdzanie CanISell
                final var price = getPrice(instrument); // TODO obie strategie
                final var qty = BuyQty(instrument, portfolio);
                Buy(instrument, qty, price);
            }
        }


        //obsługa kolejki zleceń
        Long portfolioValueNew;
        for (SubmitOrder.Buy item : queueToBuy) {
            Portfolio portfolioNew = marketPlugin.portfolio();
            if (portfolio instanceof Portfolio.Current pc) {
                portfolioValueNew = pc.cash();
                if (item.bid() * item.qty() < portfolioValueNew) {
                    //   ValidatedOrder validatedBuy =
                    marketPlugin.buy(item);
                }
            }
        }


    }

    public void Buy(Instrument instrument, long qty, long price) {
        logger.info("Placing buy order of {} ", instrument.symbol());
        final var buy = new SubmitOrder.Buy(instrument.symbol(), UUID.randomUUID().toString(), qty, price);
        ValidatedOrder validatedSell = marketPlugin.buy(buy);
        logger.info("validated buy: {}", validatedSell);
    }

    //dywersyfikacja portfela
    public long BuyQty(Instrument instrument, Portfolio portfolio) {
        // TODO
        //jak dywersyfikowac?
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

    //Pobiera cene ostatniej zrealizowanej transakcji
    public long getPrice(Instrument instrument) {
        long price = 0;
        History history = marketPlugin.history(instrument);
        if (history instanceof History.Correct hc) {
            price = hc.bought()
                    .stream()
                    .sorted(Comparator.comparing(ProcessedOrder.Bought::created).reversed())
                    .mapToLong(s -> s.offer().price()).findFirst().orElse(0);
        }
        return price;
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