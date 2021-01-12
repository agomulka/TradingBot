import model.order.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradingBot {
    private static final Client client = new Client("clientname");
    private static final String password = "clientpassword";
    private static final Logger logger = LoggerFactory.getLogger(TradingBot.class);

    public static void main(String[] args) {
        logger.info("Starting the application");
        MarketPlugin marketPlugin = new DefaultMarketPlugin(client, password);

        OrdersController ordersController = new OrdersController(client, marketPlugin);

        ordersController.run();
    }
}
