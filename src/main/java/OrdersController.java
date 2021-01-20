import model.*;
import model.order.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Timer;
import java.util.TimerTask;

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
            buyingStrategy.trade();
            sellingStrategy.trade();
            //TimeUnit.SECONDS(60);
        }

        // tu od Kasii poczatek, wyzej od Oli
        // ogolnie do zmiany

        Instruments instruments = marketPlugin.instruments();
        logger.info("returned available instruments: {}", instruments);

        TimerTask sessionTask = new TimerTask() {
            @Override
            public void run() {
                new Algorithm(marketPlugin).run();
            }
        };
        new Timer().scheduleAtFixedRate(sessionTask,0,SESSION_INTERVAL);
    }
}
