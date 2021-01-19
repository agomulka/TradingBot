import model.*;
import model.order.Client;
import model.SubmitOrder;
import model.order.Instrument;
import model.order.ProcessedOrder;
import model.order.ValidatedOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

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
        Instruments instruments = marketPlugin.instruments();
        logger.info("returned available instruments: {}", instruments);
//
//        Submitted submitted = marketPlugin.submitted();
//        logger.info("returned submitted orders: {}", submitted);
//
//        Processed processed = marketPlugin.processed();
//        logger.info("returned processed orders: {}", processed);


//        if (instruments instanceof Instruments.Correct ic) {
//            final var instrument = ic.available().stream().findFirst().get();
//            final var history = marketPlugin.history(instrument);
//            logger.info("returned instrument {} history: {}", instrument.symbol(), history);
//
//            logger.info("Placing buy order of {} for client: {}", instrument.symbol(), client.name());
//            final var sell = new SubmitOrder.Sell("ECHO", UUID.randomUUID().toString(), 50, 50);
//
//            ValidatedOrder validatedSell = marketPlugin.sell(sell);
//            logger.info("validated sell: {}", validatedSell);
//        }
//
//        logger.info("Getting portfolio for client: {}", client.name());
//        Portfolio portfolioBefore = marketPlugin.portfolio();
//        logger.info("returned portfolio: {}", portfolioBefore);




        TimerTask sessionTask = new TimerTask() {
            @Override
            public void run() {
                new Algorithm(marketPlugin).run();
            }
        };
        new Timer().scheduleAtFixedRate(sessionTask,0,SESSION_INTERVAL);
    }
}
