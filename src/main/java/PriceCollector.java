import model.History;
import model.Instruments;
import model.order.Instrument;
import model.order.ProcessedOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

// history bought == history sold
// pobieramy jedną z nich, bo są takie same
// zwracamy hashMape<Symbol instrumentu, lista cen>

public class PriceCollector {
    private final MarketPlugin marketPlugin;
    HashMap<String, List<Long>> hashMap = new HashMap<>();
    BlockingQueue<HashMap<String, List<Long>>> queue = new LinkedBlockingQueue<>();

    public PriceCollector(MarketPlugin marketPlugin) {
        this.marketPlugin = marketPlugin;
    }

    public HashMap<String, List<Long>> run() {

        //download prices, sort them and put into hashMap
        Instruments instruments = marketPlugin.instruments();
        List<Long> priceList;

        if (instruments instanceof Instruments.Correct ic) {
            History history;
            for (Instrument instr : ic.available()) {
                history = marketPlugin.history(instr);
                if (history instanceof History.Correct hc) {
                    priceList = hc.bought().stream()
                            .sorted(Comparator.comparing(ProcessedOrder.Bought::created).reversed())
                            .map(x -> x.offer().price()).collect(Collectors.toList());

                    hashMap.put(instr.symbol(), priceList);
                }
            }
        }
        return hashMap;
    }
}
