import model.order.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TradingBot {
    private static final Client client = new Client("client03");
    private static final String password = "Jwnkq3JA";
    private static final Logger logger = LoggerFactory.getLogger(TradingBot.class);

    public static void main(String[] args) {
        logger.info("Starting the application");
        MarketPlugin marketPlugin = new DefaultMarketPlugin(client, password);

        OrdersController ordersController = new OrdersController(marketPlugin);

        ordersController.run();
    }
}
