import model.*;
import model.order.Client;
import model.SubmitOrder;
import model.order.Instrument;
import model.order.ProcessedOrder;
import model.order.ValidatedOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.plaf.basic.BasicButtonUI;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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
            buyingStrategy.trading();
            sellingStrategy.trading();
            //TimeUnit.SECONDS(60);
        }
//        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
//        //   BlockingQueue<HashMap<String, List<Long>>> queue = new LinkedList<>();
//    //    HashMap<String, List<Long>> hashMapBought = new HashMap<>();
//        //    HashMap<String, List<Long>> hashMapSold = new HashMap<>();
//
//        PriceCollector priceCollector = new PriceCollector(marketPlugin);
//      //  Future<HashMap<String, List<Long>>> priceHashMap = executor.submit(priceCollector);
//        executor.schedule(priceCollector, TimeUnit.SECONDS(60));
//        BuyingStrategy buyingStrategy = new BuyingStrategy(marketPlugin, priceHashMap);
    }
}




// nie potrzebne


 //   Instruments instruments = marketPlugin.instruments();
    //    logger.info("returned available instruments: {}", instruments);

  //  Submitted submitted = marketPlugin.submitted();
    //    logger.info("returned submitted orders: {}", submitted);

 //   Processed processed = marketPlugin.processed();
//    logger.info("returned processed orders: {}", processed);

//        if (instruments instanceof Instruments.Correct ic) {
//                History history;
//                for(Instrument instr : ic.available()) {
//                history = marketPlugin.history(instr);
//                List<Long> priceListBought;
//        List<Long> priceListSold;
//        HashMap<String, List<Long>> hashMapBought = new HashMap<>();
//        HashMap<String, List<Long>> hashMapSold = new HashMap<>();
//        if( history instanceof History.Correct hc) {
//        priceListBought = hc.bought().stream()
//        .sorted(Comparator.comparing(ProcessedOrder.Bought::created).reversed())
//                            .collect(Collectors.toList());
//        .map(x -> x.offer().price()).collect(Collectors.toList());
//
//        priceListSold = hc.sold().stream()
//        .sorted(Comparator.comparing(ProcessedOrder.Sold::created).reversed())
//        .map(x -> x.offer().price()).collect(Collectors.toList());
//
//        hashMapBought.put(instr.symbol(), priceListBought);
//        hashMapSold.put(instr.symbol(), priceListSold);

        //logger.info("hash map key : value", hashMapBought.keySet());
//                    for (Map.Entry<String, List<Long>> entry : hashMapBought.entrySet()) {
//                        System.out.println(entry.getKey()+" : "+entry.getValue());
//                    }

//        logger.info("returned instrument {} history bought instrument: {}", instr.symbol(), priceListBought);
//        logger.info("returned instrument {} history sold instrument: {}", instr.symbol(), priceListSold);
//        logger.info("returned instrument {} history bought instrument: {}", instr.symbol(), history);

//                    hashMapBought = list.get(0);
//                    hashMapSold = list.get(1);
//                    HashMap<String, Float> hashMapBoughtAvg = new HashMap<>();
//                    HashMap<String, Float> hashMapSoldAvg = new HashMap<>();
//
//                    for (String symbol : hashMapBought.keySet()) {
//                        Float sum = (float) 0;
//                        Float avg = (float) 0;
//                        for (Long price : hashMapBought.get(symbol)) {
//                            sum += price;
//                        }
//                        avg = sum / hashMapBought.get(symbol).size();
//                        hashMapBoughtAvg.put(symbol, avg);
//                    }
//                    for(String symbol : hashMapSold.keySet()){
//                        Float sum = (float) 0;
//                        Float avg = (float) 0;
//                        for(Long price : hashMapSold.get(symbol)){
//                            sum += price;
//                        }
//                        avg = sum / hashMapSold.get(symbol).size();
//                        hashMapSoldAvg.put(symbol, avg);
//                    }
//                    logger.info("bought");
//                    logger.info("hash map key : value", hashMapBoughtAvg.keySet());
//                    for (Map.Entry<String, Float> entry : hashMapBoughtAvg.entrySet()) {
//                        System.out.println(entry.getKey() + " : " + entry.getValue());
//                    }
//
//
//                    logger.info("sold");
//                    logger.info("hash map key : value", hashMapSoldAvg.keySet());
//                    for (Map.Entry<String, Float> entry : hashMapSoldAvg.entrySet()) {
//                        System.out.println(entry.getKey() + " : " + entry.getValue());
//                    }
        // hashMap.put(instr.symbol(), priceList);
//        }
 //       }

//
//            final var instrument =
//                    ic.available().stream().filter(instr -> instr.symbol().equals("ULMA")).findFirst().get();
//              //      ic.available().stream().findFirst().get();
//
//            final var history = marketPlugin.history(instrument);
//
//            logger.info("returned instrument {} history: {}", instrument.symbol(), history);

        //    logger.info("Placing buy order of {} for client: {}", instrument.symbol(), client.name());
        //    final var buy = new SubmitOrder.Buy(instrument.symbol(), UUID.randomUUID().toString(), 30, 50);
        //    ValidatedOrder validatedBuy = marketPlugin.buy(buy);
        //    logger.info("validated buy: {}", validatedBuy);
//            final var sell = new SubmitOrder.Sell(instrument.symbol(), UUID.randomUUID().toString(), 30, 50);
//            ValidatedOrder validatedSell = marketPlugin.sell(sell);
//            logger.info("validated sell: {}", validatedSell);
  //      }

//        logger.info("Getting portfolio for client: {}", client.name());
//        Portfolio portfolioBefore = marketPlugin.portfolio();
//        logger.info("returned portfolio: {}", portfolioBefore);
////
//        logger.info("Getting portfolio for client: {}", client.name());
//        Portfolio portfolioAfter = marketPlugin.portfolio();
//        logger.info("returned portfolio: {}", portfolioAfter);
