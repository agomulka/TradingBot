import java.util.HashMap;
import java.util.List;


/**
 * The class storing prices and amounts and calculating all averages used by BuyingStrategy and SellingStrategy.
 */
public class MovingAveragesTasker {
    private final Collector collector;

    private final int shortPeriod;
    private final int longPeriod;
    private final int amountPeriod;

    private HashMap<String, List<Long>> prices;
    private HashMap<String, List<Long>> amounts;


    /**
     * Create a tasker.
     *
     * @param collector    Collector collecting prices and amounts for the tasker.
     * @param shortPeriod  Length of the shorter price averages.
     * @param longPeriod   Length of the longer price averages.
     * @param amountPeriod Length of the averages of amounts.
     */
    public MovingAveragesTasker(Collector collector, int shortPeriod, int longPeriod, int amountPeriod) {
        this.collector = collector;
        this.shortPeriod = shortPeriod;
        this.longPeriod = longPeriod;
        this.amountPeriod = amountPeriod;
    }


    /**
     * Update prices known by the tasker.
     */
    public void updatePrices() {
        this.prices = collector.collectPrices();
    }

    /**
     * Same as above but for amounts.
     */
    public void updateAmounts() {
        this.amounts = collector.collectAmounts();
    }


    /**
     * Calculate average prices of last operations for all instruments.
     *
     * @param inShortPeriod Whether the averages should be shorter or longer.
     * @param withLastPrice Whether the prices to calculate average of contain last prices.
     * @return Map between stocks symbols and their average prices.
     */
    public HashMap<String, Double> getAveragePrices(boolean inShortPeriod, boolean withLastPrice) {
        HashMap<String, Double> averages = new HashMap<>();

        for (String symbol : prices.keySet()) {
            double symbolAverage = getAverage(prices.get(symbol), inShortPeriod ? shortPeriod : longPeriod, withLastPrice);
            averages.put(symbol, symbolAverage);
        }

        return averages;
    }

    /**
     * Auxiliary method for the one above.
     *
     * @param list     List of elements to calculate average of.
     * @param period   Number of elements from the list to take for the average.
     * @param withLast Whether to include the last in time element (first in list) in calculation.
     * @return double The average.
     */
    public double getAverage(List<Long> list, int period, boolean withLast) {
        List<Long> lastPrices;

        if (list.size() == 0)
            return 0;

        if (withLast) {
            lastPrices = list.subList(0, Math.min(period, list.size()));
        } else {
            lastPrices = list.subList(1, Math.min(period + 1, list.size()));
        }

        return lastPrices.stream().mapToDouble(a -> a).sum() / (double) lastPrices.size();
    }

    /**
     * Calculate average amount of last operations for an instrument.
     *
     * @param symbol Symbol of the instrument.
     * @return double The average.
     */
    public double getAverageAmount(String symbol) {
        List<Long> lastAmounts = amounts.get(symbol);
        lastAmounts = lastAmounts.subList(0, Math.min(amountPeriod, lastAmounts.size()));

        if (lastAmounts.size() == 0)
            return 0;

        return lastAmounts.stream().mapToDouble(a -> a).sum() / (double) lastAmounts.size();
    }


    /**
     * Figure out the relation between averages.
     * If prices' plots cross we have signal to sell or buy.
     * General implementation, specific usage depends on strategy.
     *
     * @param a First price.
     * @param b Second price.
     * @return int 0, 1 or 3
     */
    public int signal(double a, double b) {
        int signal = 3;
        if (a >= b) signal = 1;
        else if (a < b) signal = 0;
        return signal;
    }

    /**
     * Self-explanatory
     *
     * @return Map between stocks symbols and their historical prices.
     */
    public HashMap<String, List<Long>> getPrices() {
        return prices;
    }
}
