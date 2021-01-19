//import model.Instruments;
//import model.Portfolio;
//import model.SubmitOrder;
//import model.order.Instrument;
//import model.order.ProcessedOrder;
//import model.order.ValidatedOrder;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.*;
//import java.util.concurrent.Callable;
//                                                          MOVE TO STRATEGY
//                                                                NIEPOTRZEBNE !!
//public class Algorithm  {
//    private static final Logger logger = LoggerFactory.getLogger(PriceCollector.class);
//    private MarketPlugin marketPlugin;
//    HashMap<String, List<Long>> hashMapBought = new HashMap<>();
//    HashMap<String, List<Long>> hashMapSold = new HashMap<>();
//    Queue<HashMap<String, List<Long>>> queue;
//    List<HashMap<String, List<Long>>> list;
//    HashMap<String, Float> hashMapBoughtAvg = new HashMap<>();
//    HashMap<String, Float> hashMapSoldAvg = new HashMap<>();
//
//    public Algorithm(MarketPlugin marketPlugin, List<HashMap<String, List<Long>>> list, Queue<HashMap<String, List<Long>>> queue) {
//        this.marketPlugin = marketPlugin;
//        this.list = list;
//        this.queue = queue;
//    }
//
//
//    public HashMap<String, Long> run() throws Exception {
//
//
//        //calculate average
//        hashMapBought = list.get(0);
//        hashMapSold = list.get(1);
//
//        for(String symbol : hashMapBought.keySet()){
//            Float sum = (float) 0;
//            Float avg = (float) 0;
//            for(Long price : hashMapBought.get(symbol)){
//                sum += price;
//            }
//            avg = sum / hashMapBought.get(symbol).size();
//            hashMapBoughtAvg.put(symbol, avg);
//        }
//
//        for(String symbol : hashMapSold.keySet()){
//            Float sum = (float) 0;
//            Float avg = (float) 0;
//            for(Long price : hashMapSold.get(symbol)){
//                sum += price;
//            }
//            avg = sum / hashMapSold.get(symbol).size();
//            hashMapSoldAvg.put(symbol, avg);
//        }
//
//
//        //deciding to buy/sell on Moving average  (średnia krocząca)
//        //zakładam że cena z poprzedniego dnia to cena następna na liście
//        //              (istnieje przypadek że jest z tego samego dnia)
//        Boolean signalToBuy = false;
//        Boolean signalToSold = false;
//
//        for(String symbol : hashMapBought.keySet()) {
//            Float averagePrice = hashMapBoughtAvg.get(symbol);
//            List<Long> longs = hashMapBought.get(symbol);
//            Float closingPrice = longs.get(0).floatValue();
//            Float yesterdayPrice = longs.get(1).floatValue();
//            if(averagePrice <= closingPrice && averagePrice > yesterdayPrice){
//                signalToBuy = true;
//            }
//        }
//
//        for(String symbol : hashMapSold.keySet()) {
//            Float averagePrice = hashMapSoldAvg.get(symbol);
//            List<Long> longs = hashMapSold.get(symbol);
//            Float closingPrice = longs.get(0).floatValue();
//            Float yesterdayPrice = longs.get(1).floatValue();
//            if(averagePrice >= closingPrice && averagePrice < yesterdayPrice){
//                signalToSold = true;
//            }
//        }
//
//
//
//        //dywersyfikacja portfela -> DiversificationSignal
//        Queue<SubmitOrder.Buy> queueToBuy = new LinkedList<SubmitOrder.Buy>();
//        Queue<SubmitOrder.Sell> queueToSell = new LinkedList<SubmitOrder.Sell>();
//
//        int instrumentNumber = hashMapBought.keySet().size();
//        int percent = 100/instrumentNumber;
//        int maxPortfolio = 10000;
//        Long portfolioValue;
//        Portfolio portfolio = marketPlugin.portfolio();
//        if (portfolio instanceof Portfolio.Current pc) {
//            portfolioValue = pc.cash();
//
//            for (String symbol : hashMapBought.keySet()) {
//                List<Long> longs = hashMapBought.get(symbol);
//                Float closingPrice = longs.get(0).floatValue();
//                float quality = (maxPortfolio * percent) / (100 * closingPrice);  //numbers of share to buy
//                if( signalToBuy == true){
//                    String tradeID = UUID.randomUUID().toString();
//                    Double q = Math.floor(quality);
//                    long qq = q.longValue();
//                    Double cp = Math.floor(closingPrice);
//                    long cpp = cp.longValue();
//                    SubmitOrder.Buy order = new SubmitOrder.Buy(symbol, tradeID, qq, cpp);
//                    queueToBuy.add(order);
//                }
//            }
//        }
//
//
////dywersyfikacja dla sprzedaży
//
//        if (portfolio instanceof Portfolio.Current pc) {
//            for (String symbol : hashMapSold.keySet()) {
//                List<Long> longs = hashMapSold.get(symbol);
//                Float closingPrice = longs.get(0).floatValue();
//                float quality = (maxPortfolio * percent) / (100 * closingPrice);  //numbers of share to sell
//
//                if( signalToSold == true){
//                    String tradeID = UUID.randomUUID().toString();
//                    Double q = Math.floor(quality);
//                    long qq = q.longValue();
//                    Double cp = Math.floor(closingPrice);
//                    long cpp = cp.longValue();
//                    SubmitOrder.Sell order = new SubmitOrder.Sell(symbol, tradeID, qq, cpp);
//                    queueToSell.add(order);
//                }
//            }
//        }
//
//
//        //  final var buy = new SubmitOrder.Buy(instrument.symbol(), UUID.randomUUID().toString(), 30, 50);
//        //    ValidatedOrder validatedBuy = marketPlugin.buy(buy);
//        //    logger.info("validated buy: {}", validatedBuy);
//
//        //obsługa kolejki zleceń
//        Long portfolioValue2;
//        for (SubmitOrder.Buy item: queueToBuy) {
//            Portfolio portfolio2 = marketPlugin.portfolio();
//            if (portfolio2 instanceof Portfolio.Current pc) {
//                portfolioValue2 = pc.cash();
//                if (item.bid() * item.qty() < portfolioValue2) {
//                    ValidatedOrder validatedBuy = marketPlugin.buy(item);
//                }
//            }
//        }
//
//        Long portfolioValueToSell;
//        for (SubmitOrder.Sell item: queueToSell) {
//            Portfolio portfolio2 = marketPlugin.portfolio();
//            if (portfolio2 instanceof Portfolio.Current pc) {
//                portfolioValue2 = pc.cash();
//                if (item.ask() * item.qty() < portfolioValue2) {
//                    ValidatedOrder validatedBuy = marketPlugin.sell(item);
//                }
//            }
//        }
//
//
//        //submission order (if sygnalToDo = true and DiversificationSignal = true)
//
//
//        return null;
//    }
//
//
//
//
//
//
//

//
//
//
//
//}
