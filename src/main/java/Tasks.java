import model.History;
import model.Portfolio;
import model.SubmitOrder;
import model.order.Instrument;
import model.order.ProcessedOrder;
import model.order.ValidatedOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.UUID;

public class Tasks {
    private static final Logger logger = LoggerFactory.getLogger(OrdersController.class);
    private final MarketPlugin marketPlugin;

    public Tasks(MarketPlugin marketPlugin) {
        this.marketPlugin = marketPlugin;
    }

    //Wylicza srednia z ostatnich n operacji
    public double getAverage(Instrument instrument, int n) {
        double avg = 0;
        History history = marketPlugin.history(instrument);
        if (history instanceof History.Correct hc) {
            avg = hc.bought()
                    .stream()
                    .sorted(Comparator.comparing(ProcessedOrder.Bought::created).reversed())
                    .mapToLong(s -> s.offer().price())
                    .limit(n).average().orElse(0);
        }
        return avg;
    }

    public double getAverageBefore(Instrument instrument, int n) {
        double avg = 0;
        History history = marketPlugin.history(instrument);
        if (history instanceof History.Correct hc) {
            avg = hc.bought()
                    .stream()
                    .sorted(Comparator.comparing(ProcessedOrder.Bought::created).reversed())
                    .mapToLong(s -> s.offer().price())
                    .limit(n+1).skip(1).average().orElse(0);
        }
        return avg;
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

    //Sprawdza czy posiadamy dany instrument w portfelu
    public boolean CanISell(Instrument instrument, Portfolio portfolio){
        boolean contains;
        if (portfolio instanceof Portfolio.Current pc) {
            contains = pc.portfolio().stream().noneMatch(s->s.instrument().equals(instrument));
        }
        else contains = false;
        return contains;
    }

    public long SellQty(Instrument instrument, Portfolio portfolio) {
        //jak dywersyfikowac
        return 1;
    }


    public long BuyQty(Instrument instrument, Portfolio portfolio) {
        //jak dywersyfikowac?
        return 1;
    }

    public void Sell(Instrument instrument, long qty, long price){
        logger.info("Placing sell order of {} ", instrument.symbol());
        final var sell = new SubmitOrder.Sell(instrument.symbol(), UUID.randomUUID().toString(), qty, price);
        ValidatedOrder validatedSell = marketPlugin.sell(sell);
        logger.info("validated sell: {}", validatedSell);
    }

    public void Buy(Instrument instrument, long qty, long price){
        logger.info("Placing buy order of {} ", instrument.symbol());
        final var buy = new SubmitOrder.Buy(instrument.symbol(), UUID.randomUUID().toString(), qty, price);
        ValidatedOrder validatedSell = marketPlugin.buy(buy);
        logger.info("validated buy: {}", validatedSell);
    }

    //Okresla polozenie srednich wzgledem siebie. Gdy wykresy przecinaja sie (nastepuje zmiana z 0 na 1 lub 1 na 0) mamy sygnal do sprzedazy lub kupna
    public int signal(double a, double b) {
        int signal = 3;
        if(a>b) signal = 1;
        else if(a<b) signal = 0;
        return signal;
    }

}
