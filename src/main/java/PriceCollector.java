import model.History;
import model.Instruments;
import model.order.Instrument;
import model.order.ProcessedOrder;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

// history bought == history sold
// pobieramy jedną z nich, bo są takie same
// zwracamy hashMape<Symbol instrumentu, lista cen>

// pobrana jest historia w całości, gotowa do dalszych przeróbek

// generalnie gotowe

public class PriceCollector {
    private final MarketPlugin marketPlugin;

    public PriceCollector(MarketPlugin marketPlugin) {
        this.marketPlugin = marketPlugin;
    }

    public HashMap<String, List<Long>> collectPrices() {
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
                            .map(x -> x.offer().price()).collect(Collectors.toList());

                    hashMap.put(instrument.symbol(), priceList);
                }
            }
        }
        return hashMap;
    }
}
