import model.Instruments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OrdersController {
    private static final long SESSION_INTERVAL = 60;
    private static final Logger logger = LoggerFactory.getLogger(OrdersController.class);

    private final MarketPlugin marketPlugin;

    private MovingAveragesTasker averagesTasker;
    private BuyingStrategy buyingStrategy;
    private SellingStrategy sellingStrategy;

    public OrdersController(MarketPlugin marketPlugin) {
        this.marketPlugin = marketPlugin;
    }

    public void run() {
        Collector collector = new Collector(marketPlugin);
        averagesTasker = new MovingAveragesTasker(collector, 5, 30, 10);
        buyingStrategy = new BuyingStrategy(marketPlugin, averagesTasker);
        sellingStrategy = new SellingStrategy(marketPlugin, averagesTasker);

        Instruments instruments = marketPlugin.instruments();
        logger.info("returned available instruments: {}", instruments);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(this::doOneGo, 0, SESSION_INTERVAL, TimeUnit.SECONDS);
    }

    public void doOneGo() {
        averagesTasker.updatePrices();
        averagesTasker.updateAmounts();
        buyingStrategy.trade();
        sellingStrategy.trade();
    }
}
