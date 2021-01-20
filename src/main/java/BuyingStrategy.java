import model.History;
import model.Portfolio;
import model.SubmitOrder;
import model.order.Instrument;
import model.order.ProcessedOrder;
import model.order.SubmittedOrder;
import model.order.ValidatedOrder;

import java.util.*;

public class BuyingStrategy implements TradingStrategy {
    private final MarketPlugin marketPlugin;
    List<HashMap<String, List<Long>>> list;
    HashMap<String, List<Long>> hashMapBought = new HashMap<>();
    HashMap<String, Float> hashMapBoughtAvg = new HashMap<>();
    Queue<HashMap<String, List<Long>>> queue;
    Queue<SubmitOrder.Buy> queueToBuy = new LinkedList<>();


    public BuyingStrategy(MarketPlugin marketPlugin, HashMap<String, List<Long>> hashMap) {
        this.marketPlugin = marketPlugin;
        this.hashMapBought = hashMap;
    }

    @Override
    public void trade() {
        //calculate average

        for (String symbol : hashMapBought.keySet()) {
            Float sum = (float) 0;
            Float avg = (float) 0;
            for (Long price : hashMapBought.get(symbol)) {
                sum += price;
            }
            avg = sum / hashMapBought.get(symbol).size();
            hashMapBoughtAvg.put(symbol, avg);
        }

        //deciding to buy on Moving average  (średnia krocząca)
        //zakładam że cena z poprzedniego dnia to cena następna na liście
        //              (istnieje przypadek że jest z tego samego dnia)
        Boolean signalToBuy = false;

        for (String symbol : hashMapBought.keySet()) {
            Float averagePrice = hashMapBoughtAvg.get(symbol);
            List<Long> longs = hashMapBought.get(symbol);
            Float closingPrice = longs.get(0).floatValue();
            Float yesterdayPrice = longs.get(1).floatValue();
            if (averagePrice <= closingPrice && averagePrice > yesterdayPrice) {
                signalToBuy = true;
            }
        }


        //dywersyfikacja portfela

        int instrumentNumber = hashMapBought.keySet().size();
        int percent = 100 / instrumentNumber;
        Long portfolioValue;
        Portfolio portfolio = marketPlugin.portfolio();
        if (portfolio instanceof Portfolio.Current pc) {
            portfolioValue = pc.cash();

            for (String symbol : hashMapBought.keySet()) {
                List<Long> prices = hashMapBought.get(symbol);
                Long closingPrice = prices.get(0);
                float quality = (portfolioValue * percent) / (100 * closingPrice);  //number of shares to buy
                if (signalToBuy == true) {
                    String tradeID = UUID.randomUUID().toString();
                    Double q = Math.floor(quality);
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

    public long BuyQty(Instrument instrument, Portfolio portfolio) {
        // TODO
        //jak dywersyfikowac?
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