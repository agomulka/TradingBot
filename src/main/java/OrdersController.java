import model.*;
import model.order.Client;
import model.SubmitOrder;
import model.order.Instrument;
import model.order.ProcessedOrder;
import model.order.ValidatedOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.plaf.basic.BasicButtonUI;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class OrdersController {

    // TODO: could this be retrieved from the market at the time of registration
    private static final long SESSION_INTERVAL = 60000;
    private static final Logger logger = LoggerFactory.getLogger(OrdersController.class);

    private final Client client;
    private final MarketPlugin marketPlugin;

    public OrdersController(Client client, MarketPlugin marketPlugin) {
        this.client = client;
        this.marketPlugin = marketPlugin;
    }

    public void run() {
        PriceCollector priceCollector = new PriceCollector(marketPlugin);
        BuyingStrategy buyingStrategy;
        SellingStrategy sellingStrategy;
        while(true){
            HashMap<String, List<Long>> hashMap = priceCollector.run();
            buyingStrategy = new BuyingStrategy(marketPlugin, hashMap);
            sellingStrategy = new SellingStrategy(marketPlugin, hashMap);
            buyingStrategy.trading();
            sellingStrategy.trading();
            //TimeUnit.SECONDS(60);
        }
//        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
//        //   BlockingQueue<HashMap<String, List<Long>>> queue = new LinkedList<>();
//    //    HashMap<String, List<Long>> hashMapBought = new HashMap<>();
//        //    HashMap<String, List<Long>> hashMapSold = new HashMap<>();
//
//        PriceCollector priceCollector = new PriceCollector(marketPlugin);
//      //  Future<HashMap<String, List<Long>>> priceHashMap = executor.submit(priceCollector);
//        executor.schedule(priceCollector, TimeUnit.SECONDS(60));
//        BuyingStrategy buyingStrategy = new BuyingStrategy(marketPlugin, priceHashMap);
    }
}
