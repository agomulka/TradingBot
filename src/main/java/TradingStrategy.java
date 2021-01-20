import model.order.Client;

public interface TradingStrategy {
    void trade();
    boolean checkIfNotSubmitted(String symbol, Long qualityLong, Long closingPrice);
}
