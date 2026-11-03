import java.util.HashMap;
import java.util.Map;

public class Task2Runner {
    // Old system gives data as one text string.
    static class LegacyStockFeed {
        public String fetchRawTickerPacket(String ticker) {
            if (ticker.equalsIgnoreCase("AAPL"))
                return "TICKER=AAPL;BID=182.50;ASK=183.50;STATUS=OPEN";
            if (ticker.equalsIgnoreCase("GOOGL"))
                return "TICKER=GOOGL;BID=164.20;ASK=164.80;STATUS=OPEN";
            return "TICKER=" + ticker + ";BID=0;ASK=0;STATUS=UNKNOWN";
        }
    }

    interface StockProvider {
        double getMarketPrice(String ticker);
        String getStatus(String ticker);
    }

    // Adapter changes old text data into the interface we need.
    static class StockFeedAdapter implements StockProvider {
        private LegacyStockFeed feed;
        StockFeedAdapter(LegacyStockFeed feed) { this.feed = feed; }

        private Map<String, String> parse(String ticker) {
            Map<String, String> result = new HashMap<>();
            String[] parts = feed.fetchRawTickerPacket(ticker).split(";");
            for (String part : parts) {
                String[] pair = part.split("=");
                if (pair.length == 2) result.put(pair[0], pair[1]);
            }
            return result;
        }

        public double getMarketPrice(String ticker) {
            Map<String, String> data = parse(ticker);
            double bid = Double.parseDouble(data.get("BID"));
            double ask = Double.parseDouble(data.get("ASK"));
            return (bid + ask) / 2;
        }

        public String getStatus(String ticker) {
            return parse(ticker).getOrDefault("STATUS", "UNKNOWN");
        }
    }

    // Proxy saves the last price for 1.5 seconds.
    static class CachedStockProxy implements StockProvider {
        private StockProvider provider;
        private Map<String, Double> cache = new HashMap<>();
        private Map<String, Long> times = new HashMap<>();
        private long ttl;

        CachedStockProxy(StockProvider provider, long ttl) {
            this.provider = provider;
            this.ttl = ttl;
        }

        public double getMarketPrice(String ticker) {
            long now = System.currentTimeMillis();
            if (cache.containsKey(ticker) && now - times.get(ticker) < ttl) {
                System.out.println("[CACHE HIT] " + ticker);
                return cache.get(ticker);
            }
            System.out.println("[NETWORK FETCH] " + ticker);
            double price = provider.getMarketPrice(ticker);
            cache.put(ticker, price);
            times.put(ticker, now);
            return price;
        }

        public String getStatus(String ticker) {
            return provider.getStatus(ticker);
        }
    }

    public static void main(String[] args) throws Exception {
        StockProvider adapter = new StockFeedAdapter(new LegacyStockFeed());
        StockProvider proxy = new CachedStockProxy(adapter, 1500);

        System.out.println(">>> TASK 2: ADAPTER + PROXY <<<");
        System.out.printf("AAPL Price: $%.2f%n", proxy.getMarketPrice("AAPL"));
        System.out.printf("AAPL Price: $%.2f%n", proxy.getMarketPrice("AAPL"));
        Thread.sleep(1600);
        System.out.printf("AAPL Price: $%.2f%n", proxy.getMarketPrice("AAPL"));
    }
}
