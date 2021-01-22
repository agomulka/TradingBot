import model.Portfolio;
import model.order.Client;

public interface TradingStrategy {
    void trade();
    boolean checkIfNotSubmitted(String symbol, Long qty, Long price);

}
