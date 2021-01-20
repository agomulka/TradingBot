import model.Portfolio;
import model.SubmitOrder;
import model.order.Instrument;
import model.order.SubmittedOrder;
import model.order.ValidatedOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class SellingStrategy implements TradingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(PriceCollector.class);
    private final MarketPlugin marketPlugin;
    List<HashMap<String, List<Long>>> list;
    HashMap<String, List<Long>> hashMapSold = new HashMap<>();
    HashMap<String, Float> hashMapSoldAvg = new HashMap<>();
    Queue<SubmitOrder.Sell> queueToSell = new LinkedList<>();

    public SellingStrategy(MarketPlugin marketPlugin, HashMap<String, List<Long>> hashMap) {
        this.marketPlugin = marketPlugin;
        this.hashMapSold = hashMap;
    }

    @Override
    public void trade() {

        //calculate average

        for (String symbol : hashMapSold.keySet()) {
            Float sum = (float) 0;
            Float avg = (float) 0;
            for (Long price : hashMapSold.get(symbol)) {
                sum += price;
            }
            avg = sum / hashMapSold.get(symbol).size();
            hashMapSoldAvg.put(symbol, avg);
        }


        //deciding to sell on Moving average  (średnia krocząca)
        //zakładam że cena z poprzedniego dnia to cena następna na liście
        //              (istnieje przypadek że jest z tego samego dnia)


        //pobranie akutalnej listy instrumentow z portfela
        Boolean signalToSold = false;
        Portfolio portfolio = marketPlugin.portfolio();
        List<String> instrumentInPortfolio;
        if (portfolio instanceof Portfolio.Current pc) {
            instrumentInPortfolio = pc.portfolio().stream().map(x -> x.instrument().symbol())
                    .collect(Collectors.toList());


            for (String symbol : instrumentInPortfolio) {
                Float averagePrice = hashMapSoldAvg.get(symbol);
                List<Long> prices = hashMapSold.get(symbol);
                Float closingPrice = prices.get(0).floatValue();
                Float yesterdayPrice = prices.get(1).floatValue();
                if (averagePrice >= closingPrice && averagePrice <= yesterdayPrice) {
                    signalToSold = true;
                }
            }
        }

        //dywersyfikacja portfela dla sprzedaży

        int instrumentNumber = hashMapSold.keySet().size();
        int percent = 100/instrumentNumber;
       // int maxPortfolio = 10000;
        Long portfolioValue;
      //  Portfolio portfolio = marketPlugin.portfolio();
        if (portfolio instanceof Portfolio.Current pc) {
            portfolioValue = pc.cash();
            for (String symbol : hashMapSold.keySet()) {
                List<Long> longs = hashMapSold.get(symbol);
                Long closingPrice = longs.get(0);
                float quality = (portfolioValue * percent) / (100 * closingPrice);  //numbers of share to sell

                if( signalToSold == true){
                    String tradeID = UUID.randomUUID().toString();
                    Double q = Math.floor(quality);
                    long qualityLong = q.longValue();
                    if(checkIfNotSubmitted(symbol, qualityLong, closingPrice)) {  //sprawdza czy nie zostało wystawione takie zlecenie
                        SubmitOrder.Sell order = new SubmitOrder.Sell(symbol, tradeID, qualityLong, closingPrice);
                        queueToSell.add(order);
                    }
                }
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

    public long SellQty(Instrument instrument, Portfolio portfolio) {
        // TODO
        //jak dywersyfikowac
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




