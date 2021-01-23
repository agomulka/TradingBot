import model.History;
import model.Instruments;
import model.order.Instrument;
import model.order.ProcessedOrder;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


/**
 * The class responsible for collecting amounts and prices of stocks from their histories.
 * Since bought and sold histories are the same no such distinction is needed.
 *
 * @param marketPlugin The MarketPlugin to connect to.
 */
public record Collector(MarketPlugin marketPlugin) {
    /**
     * Collect fresh history of prices change for all stocks.
     *
     * @return Map between stock symbols and lists of prices.
     */
    public HashMap<String, List<Long>> collectPrices() {
        HashMap<String, List<Long>> hashMap = new HashMap<>();

        // download prices, sort them and put into hashMap
        Instruments instruments = marketPlugin.instruments();
        List<Long> priceList;

        if (instruments instanceof Instruments.Correct ic) {
            History history;
            for (Instrument instrument : ic.available()) {
                history = marketPlugin.history(instrument);
                if (history instanceof History.Correct hc) {
                    priceList = hc.bought().stream()
                            .sorted(Comparator.comparing(ProcessedOrder.Bought::created).reversed())
                            .map(x -> x.offer().price()).collect(Collectors.toList());

                    hashMap.put(instrument.symbol(), priceList);
                }
            }
        }
        return hashMap;
    }

    /**
     * Collect fresh history of for all offers, all stocks.
     *
     * @return Map between stock symbols and lists of amounts.
     */
    public HashMap<String, List<Long>> collectAmounts() {
        HashMap<String, List<Long>> hashMap = new HashMap<>();

        //download prices, sort them and put into hashMap
        Instruments instruments = marketPlugin.instruments();
        List<Long> priceList;

        if (instruments instanceof Instruments.Correct ic) {
            History history;
            for (Instrument instrument : ic.available()) {
                history = marketPlugin.history(instrument);
                if (history instanceof History.Correct hc) {
                    priceList = hc.bought().stream()
                            .sorted(Comparator.comparing(ProcessedOrder.Bought::created).reversed())
                            .map(x -> x.offer().qty()).collect(Collectors.toList());

                    hashMap.put(instrument.symbol(), priceList);
                }
            }
        }
        return hashMap;
    }
}
