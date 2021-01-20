import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;


// klasa obsługująca wszystko, co potrzebne do wyliczania średnich i sygnałów dla tej strategii
// (ale nie sprawdza konta ani czy mamy akcje - to robią Buying- i SellingStrategy!!)

public class MovingAveragesTasker {
    private static final Logger logger = LoggerFactory.getLogger(OrdersController.class);
    private final Collector collector;
    private final int shortPeriod;
    private final int longPeriod;
    private final int amountPeriod;
    private HashMap<String, List<Long>> prices;
    private HashMap<String, List<Long>> amounts;

    public MovingAveragesTasker(Collector collector, int shortPeriod, int longPeriod, int amountPeriod) {
        this.collector = collector;
        this.shortPeriod = shortPeriod;
        this.longPeriod = longPeriod;
        this.amountPeriod = amountPeriod;
    }


    // uaktualnia historię cen (żeby nie powtarzać priceCollector.collectPrices())
    public void updatePrices() {
        this.prices = collector.collectPrices();
    }

    public void updateAmounts() {
        this.prices = collector.collectAmounts();
    }


    // srednie uwzględniające ostatnie transakcje dla wszystkich instrumentów
    public HashMap<String, Double> getAveragePrices(boolean inShortPeriod, boolean withLastPrice) {
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
            lastPrices = list.subList(0, Math.min(period, list.size()));
        } else {
            lastPrices = list.subList(1, Math.min(period + 1, list.size()));
        }

        if (list.size() == 0)
            return 0

        return lastPrices.stream().mapToDouble(a -> a).sum() / (double) lastPrices.size(); // coś miało problem użycie .average() od razu
    }

    public double getAverageAmount(String symbol) {
        List<Long> lastAmounts = amounts.get(symbol).subList(0, Math.min(amountPeriod, amounts.size()));

        if (lastAmounts.size() == 0)
            return 0;

        return lastAmounts.stream().mapToDouble(a -> a).sum() / (double) lastAmounts.size(); // coś miało problem użycie .average() od razu
    }

    //Okresla polozenie srednich wzgledem siebie. Gdy wykresy przecinaja sie (nastepuje zmiana z 0 na 1 lub 1 na 0) mamy sygnal do sprzedazy lub kupna
    public int signal(double a, double b) {
        int signal = 3;
        if (a > b) signal = 1;
        else if (a < b) signal = 0;
        return signal;
    }

    public HashMap<String, List<Long>> getPrices() {
        return prices;
    }
}
