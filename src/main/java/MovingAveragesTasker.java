import model.History;
import model.Portfolio;
import model.SubmitOrder;
import model.order.Instrument;
import model.order.ProcessedOrder;
import model.order.ValidatedOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MovingAveragesTasker {
    private static final Logger logger = LoggerFactory.getLogger(OrdersController.class);
    private final PriceCollector priceCollector;
    private final MarketPlugin marketPlugin;
    private HashMap<String, List<Long>> prices;

    private final int shortPeriod;
    private final int longPeriod;

    public MovingAveragesTasker(MarketPlugin marketPlugin, PriceCollector priceCollector, int shortPeriod, int longPeriod) {
        this.marketPlugin = marketPlugin;
        this.priceCollector = priceCollector;
        this.shortPeriod = shortPeriod;
        this.longPeriod = longPeriod;
    }

//    //Wylicza srednia z ostatnich n operacji
//    public double getAverage(Instrument instrument, int n) {
//        double avg = 0;
//        History history = marketPlugin.history(instrument);
//        if (history instanceof History.Correct hc) {
//            avg = hc.bought()
//                    .stream()
//                    .sorted(Comparator.comparing(ProcessedOrder.Bought::created).reversed())
//                    .mapToLong(s -> s.offer().price())
//                    .limit(n).average().orElse(0);
//        }
//        return avg;
//    }
//
//    public double getAverageBefore(Instrument instrument, int n) {
//        double avg = 0;
//        History history = marketPlugin.history(instrument);
//        if (history instanceof History.Correct hc) {
//            avg = hc.bought()
//                    .stream()
//                    .sorted(Comparator.comparing(ProcessedOrder.Bought::created).reversed())
//                    .mapToLong(s -> s.offer().price())
//                    .limit(n + 1).skip(1).average().orElse(0);
//        }
//        return avg;
//    }

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

    //Sprawdza czy posiadamy dany instrument w portfelu
    public boolean CanISell(Instrument instrument, Portfolio portfolio) {
        boolean contains;
        if (portfolio instanceof Portfolio.Current pc) {
            contains = pc.portfolio().stream().noneMatch(s -> s.instrument().equals(instrument));
        } else contains = false;
        return contains;
    }

    public long SellQty(Instrument instrument, Portfolio portfolio) {
        // TODO
        //jak dywersyfikowac
        return 1;
    }


    public long BuyQty(Instrument instrument, Portfolio portfolio) {
        // TODO
        //jak dywersyfikowac?
        return 1;
    }

    public void Sell(Instrument instrument, long qty, long price) {
        logger.info("Placing sell order of {} ", instrument.symbol());
        final var sell = new SubmitOrder.Sell(instrument.symbol(), UUID.randomUUID().toString(), qty, price);
        ValidatedOrder validatedSell = marketPlugin.sell(sell);
        logger.info("validated sell: {}", validatedSell);
    }

    public void Buy(Instrument instrument, long qty, long price) {
        logger.info("Placing buy order of {} ", instrument.symbol());
        final var buy = new SubmitOrder.Buy(instrument.symbol(), UUID.randomUUID().toString(), qty, price);
        ValidatedOrder validatedSell = marketPlugin.buy(buy);
        logger.info("validated buy: {}", validatedSell);
    }


    public void updatePrices() {
        this.prices = priceCollector.collectPrices();
    }

    public HashMap<String, Double> getAverages(boolean inShortPeriod, boolean withLastPrice) {
        HashMap<String, Double> averages = new HashMap<>();

        for (String symbol : prices.keySet()) {
            double symbolAverage = getAverage(prices.get(symbol), inShortPeriod ? shortPeriod : longPeriod, withLastPrice);
            averages.put(symbol, symbolAverage);
        }

        return averages;
    }

    double getAverage(List<Long> list, int period, boolean withLastPrice) { // pomocniczo dla metody wyżej
        List<Long> lastPrices;

        if (withLastPrice) {
            lastPrices = list.subList(0, period);
        } else {
            lastPrices = list.subList(1, period + 1);
        }

        return lastPrices.stream().mapToDouble(a -> a).sum() / (double) lastPrices.size(); // coś miało problem użycie .average() od razu
    }

    //Okresla polozenie srednich wzgledem siebie. Gdy wykresy przecinaja sie (nastepuje zmiana z 0 na 1 lub 1 na 0) mamy sygnal do sprzedazy lub kupna
    public int signal(double a, double b) {
        int signal = 3;
        if (a > b) signal = 1;
        else if (a < b) signal = 0;
        return signal;
    }
}
