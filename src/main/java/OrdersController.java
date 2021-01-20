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
        MovingAveragesTasker averagesTasker = new MovingAveragesTasker(priceCollector, 5, 30);
        BuyingStrategy buyingStrategy = new BuyingStrategy(marketPlugin, averagesTasker);
        SellingStrategy sellingStrategy = new SellingStrategy(marketPlugin, averagesTasker);

        while(true){
            averagesTasker.updatePrices();
            buyingStrategy.trade();
            sellingStrategy.trade();
            //TimeUnit.SECONDS(60);
        }

        // góra albo dół do usunięcia

        Instruments instruments = marketPlugin.instruments();
        logger.info("returned available instruments: {}", instruments);

        TimerTask sessionTask = new TimerTask() {
            @Override
            public void run() {
                buyingStrategy.trade();
                sellingStrategy.trade();
            }
        };
        new Timer().scheduleAtFixedRate(sessionTask,0, SESSION_INTERVAL);
    }
}
