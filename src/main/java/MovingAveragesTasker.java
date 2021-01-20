import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;


// klasa obsługująca wszystko, co potrzebne do wyliczania średnich i sygnałów dla tej strategii
// (ale nie sprawdza konta ani czy mamy akcje - to robią Buying- i SellingStrategy!!)

// generalnie gotowe

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


    // uaktualnia historię cen (żeby nie powtarzać priceCollector.collectPrices())
    public void updatePrices() {
        this.prices = priceCollector.collectPrices();
    }


    // srednie uwzględniając ostatnie transakcje dla wszystkich instrumentów
    public HashMap<String, Double> getAverages(boolean inShortPeriod, boolean withLastPrice) {
        HashMap<String, Double> averages = new HashMap<>();

        for (String symbol : prices.keySet()) {
            double symbolAverage = getAverage(prices.get(symbol), inShortPeriod ? shortPeriod : longPeriod, withLastPrice);
            averages.put(symbol, symbolAverage);
        }

        return averages;
    }

    // pomocniczo dla metody wyżej
    public double getAverage(List<Long> list, int period, boolean withLastPrice) {
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
