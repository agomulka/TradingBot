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

public class SellingStrategy implements TradingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(OrdersController.class);
    private final MarketPlugin marketPlugin;

    private final MovingAveragesTasker averagesTasker;

    HashMap<String, List<Long>> hashMapSold = new HashMap<>();
    Queue<SubmitOrder.Sell> queueToSell = new LinkedList<>();

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

            if (position == -1 && CanISell(instrument, portfolio)) {
                final var price = getPrice(instrument); // TODO obie strategie
                final var qty = SellQty(instrument, portfolio);
                Sell(instrument, qty, (long)(1.1*price));
            }
        }


        //obsługa kolejki zleceń
        Long portfolioValueNew;
        for (SubmitOrder.Sell item: queueToSell) {
            Portfolio portfolioNew = marketPlugin.portfolio();
            if (portfolioNew instanceof Portfolio.Current pc) {
                portfolioValueNew = pc.cash();
                if (item.ask() * item.qty() < portfolioValueNew) {      //check if we have enough money
                    marketPlugin.sell(item);
                }
            }
        }

    }

    public void Sell(Instrument instrument, long qty, long price) {
        logger.info("Placing sell order of {} ", instrument.symbol());
        final var sell = new SubmitOrder.Sell(instrument.symbol(), UUID.randomUUID().toString(), qty, price);
        ValidatedOrder validatedSell = marketPlugin.sell(sell);
        logger.info("validated sell: {}", validatedSell);
    }

    //dywersyfikacja portfela dla sprzedaży
    public long SellQty(Instrument instrument, Portfolio portfolio) {
        // TODO
        //jak dywersyfikowac
        int instrumentNumber = hashMapSold.keySet().size();
        int percent = 100/instrumentNumber;
        // int maxPortfolio = 10000;
        Long portfolioValue;
        if (portfolio instanceof Portfolio.Current pc) {
            portfolioValue = pc.cash();
            for (String symbol : hashMapSold.keySet()) {
                List<Long> longs = hashMapSold.get(symbol);
                Long closingPrice = longs.get(0);
                float quantity = (portfolioValue * percent) / (100 * closingPrice);  //numbers of share to sell

                if( signalToSold == true) { // chyba bez tego
                    String tradeID = UUID.randomUUID().toString();
                    Double q = Math.floor(quantity);
                    long qualityLong = q.longValue();
                    if(checkIfNotSubmitted(symbol, qualityLong, closingPrice)) {  //sprawdza czy nie zostało wystawione takie zlecenie
                        SubmitOrder.Sell order = new SubmitOrder.Sell(symbol, tradeID, qualityLong, closingPrice);
                        queueToSell.add(order);
                    }
                }
            }
        }
        return 1;
    }

    //Sprawdza czy posiadamy dany instrument w portfelu
    public boolean CanISell(Instrument instrument, Portfolio portfolio) {
        boolean contains;
        if (portfolio instanceof Portfolio.Current pc) {
            contains = pc.portfolio().stream().noneMatch(s -> s.instrument().equals(instrument));
        } else contains = false;
        return contains;
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
        if(portfolio instanceof Portfolio.Current pc){
            Optional<SubmittedOrder.Sell> any = pc.toSell().stream().filter(sell -> sell.instrument().symbol().equals(symbol)
                    && sell.ask().qty() == qualityLong && sell.ask().price() == closingPrice)
                    .findAny();
            return any.isEmpty(); //gdy nie ma takiego zlecenia zwraca True i mozemy je kupić
        }
        return false;
    }
}




