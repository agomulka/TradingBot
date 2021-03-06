import model.order.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Runner for the daytrading.
 */
public class TradingBot {
    private static final Client client = new Client("client");
    private static final String password;
    private static final Logger logger = LoggerFactory.getLogger(TradingBot.class);

    public static void main(String[] args) {
        logger.info("Starting the application");
        MarketPlugin marketPlugin = new DefaultMarketPlugin(client, password);

        Settings.readFile("parameters.json");

        OrdersController ordersController = new OrdersController(marketPlugin);

        ordersController.run();
    }
}
