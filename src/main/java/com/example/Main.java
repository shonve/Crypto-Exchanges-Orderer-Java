package com.example;

import java.nio.ByteBuffer;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

import com.example.bitget.Bitget;
import com.example.bitstamp.Bitstamp;
import com.example.bybit.Bybit;
import com.example.bybit.BybitJson;
import com.example.codec.Hex;
import com.example.coinbase.Coinbase;
import com.example.exchange.Exchange;
import com.example.gateio.Gateio;
import com.example.gateio.GateioJson;
import com.example.kraken.Kraken;
import com.example.kucoin.Kucoin;
import com.example.mexc.Mexc;
import com.example.okx.Okx;
import com.example.order.Orderer;
import com.example.binance.Binance;
import com.example.json.Json;

import com.example.server.Server;

/*
 * ["SPELLUSDT", "DMAILUSDT", "TVKUSDT", "LINKUSDT", "LINK2LUSDT", "TRVLUSDT", "FIREUSDT", 
 * "REALUSDT", "KOKUSDT", "MAVIAUSDT", "LINKUSDC", "CWARUSDT", "ZETAUSDT", "FAMEUSDT", 
 * "CHRPUSDT", "BEAMUSDT", "PRIMEUSDT", "SHRAPUSDT", "FTTUSDT", "LENDSUSDT", "MYROUSDT",
 * "SLPUSDT"]
 */

public class Main {
    static final java.io.PrintStream out = System.out;
    static final Map<String, Exchange> exchanges = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("Running main file");
        if (args.length < 1) {
            System.out.println("[usage] ./Main secertFile. See run.sh file for ref");
            return;
        }
        initExchanges(args[0]);
        
        // default port for orderer is set to 8080
        Orderer orderer = new Orderer(exchanges, 8080);
        orderer.run();
        //System.out.println(bybit.getWalletBalance("USDT"));
        //Bybit bybit = (Bybit)exchanges.get("bybit");
        //Gateio gateio = (Gateio)exchanges.get("gateio");
        //bybit.openOrder("KMON", , null, null, null)

        //String[] exchanges = new String[]{"binance", "bybit", "gateio", "bitget"};
        //String[] exchanges = new String[]{"binance"};
        //update24hTickers(exchanges);
        //updatePrices("2024-02-03", exchanges);
        //testSocket();
        //testBinanceApi();
        //testBitgetApi();
        //testBybitApi();
        //bitgetTicker("LUNCUSDT");
        //binanceTicker("UTKUSDT");
        //bybitTicker("LUNCUSDT");
        //gateioTicker("LUNCUSDT");
        //testBitstampApi();
        //testCoinbaseApi();
        //testGateioApi();
        //testKrakenApi();
        //testKucoinApi();
        //testMexcApi();
        //testOkxApi();

        //test();

        //bybitTicker("LAIUSDT");
        //bybitTicker("LLUSDT");
        //bitgetTicker("LUNCUSDT");

        //testServer();

        
        /*
        Bybit bybit = (Bybit)exchanges.get("bybit");
        bybit.openOrder("PBUX", "USDT", null, null, null)

        String order = bybit.getOrder("STATUSDT", "1615332245873367808");
        out.printf(order);
        out.printf("Status: %s\n", bybit.getBybitJson().getOrderStatus(order));
        */
    
        //bybit.getSymbols((float)0.1, 1000000);

        //testBybitOrdering();
       // testGateioOrdering();
        
       /*
       Gateio gateio = new Gateio();
       String channel = "spot.login";
       String event = "api";
       String req_param = "";
       //String timestamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
       String timestamp = "1710506823";
       String message = String.format("%s\n%s\n%s\n%s", event, channel, req_param, timestamp);
       String signature = gateio.login(message);
       System.out.printf("message: %s\nsign: %s\n", message, signature);
        */

    }

    private static Exchange initExchanges(String secretFile) {
        try(FileInputStream in = new FileInputStream(secretFile)) {
            JsonArray data = Json.newJsonArray(in);
            for (int i = 0; i < data.size(); i ++) {
                JsonObject exchange = data.getJsonObject(i);
                String name = exchange.getString("name");
                JsonObject keys = exchange.getJsonObject("keys");
                String apiKey = null;
                String secretKey = null;
                String passphrase = null;
                try {
                    apiKey = keys.getString("apiKey");
                    secretKey = keys.getString("secretKey");
                    passphrase = keys.getString("passphrase");
                }
                catch(Exception e) {
                    // ignore
                }
                addExchange(name, apiKey, secretKey, passphrase);

            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private static void addExchange(String name, String apiKey, String secretKey, String passphrase) {
        name = name.toLowerCase();
        Exchange exchange = null;
        switch (name) {
            case "binance":
                exchange = new Binance(apiKey, secretKey);
                break;
            
            case "bitget":
                exchange = new Bitget(apiKey, secretKey, passphrase);
                break;
            
            case "bitstamp":
                exchange = new Bitstamp(apiKey, secretKey, passphrase);
                break;
            
            case "bybit": 
                exchange = new Bybit(apiKey, secretKey);
                break;
            
            case "coinbase": 
                exchange = new Coinbase(apiKey, secretKey, passphrase);
                break;
            
            case "gateio":
                exchange = new Gateio(apiKey, secretKey);
                break;
            
            case "kraken":
                exchange = new Kraken(apiKey, secretKey);
                break;
            
            case "kucoin":
                exchange = new Kucoin(apiKey, secretKey, passphrase);
                break;
            
            case "mexc":
                exchange = new Mexc(apiKey, secretKey, passphrase);
                break;
            
            case "okx": 
                exchange = new Okx(apiKey, secretKey, passphrase);
                break;
            
            default:
                break;
            
        }
        if (exchange != null) {
            exchanges.put(name, exchange);
        }
    }

    static void update24hTickers(String[] updateExchanges) {
        String[] symbols = null;
        float minThreshold = (float)0.4;
        float maxThreshold = (float)200;
        for(String exchangeName: updateExchanges) {
            if (exchanges.containsKey(exchangeName)) {
                Exchange exchange = exchanges.get(exchangeName);
                if (exchange instanceof Binance binance) {
                    symbols = binance.get24hPercentageAllSymbols(minThreshold, maxThreshold);
                }
                else if (exchange instanceof Bybit bybit) {
                    symbols = bybit.get24hPercentageAllSymbols(minThreshold, maxThreshold);
                }
                else if (exchange instanceof Gateio gateio) {
                    symbols = gateio.get24hPercentageAllSymbols(minThreshold, maxThreshold);
                    List<String> finalSymbols = new ArrayList<>();
                    for(String symbol: symbols) {
                        if (symbol.indexOf("5L") > -1 || symbol.indexOf("5S") > -1 || symbol.indexOf("3L") > -1 || symbol.indexOf("3S") > -1) {
                            finalSymbols.add(symbol);
                        }
                    }
                }
                else if (exchange instanceof Bitget bitget) {
                    symbols = bitget.get24hPercentageAllSymbols(minThreshold, maxThreshold);
                }
                
                if (symbols != null) {
                    //printSymbols(exchange, symbols);
                }
            }
        }
    }

    static void updatePrices(String date, String[] updateExchanges) {
        for(String exchangeName: updateExchanges) {
            if (!exchanges.containsKey(exchangeName)) {
                continue;
            }
            Exchange exchange = exchanges.get(exchangeName);
            if (exchange instanceof Binance binance) {
                binance.updatePrices(date);
            }
            else if (exchange instanceof Bybit bybit) {
                bybit.updatePrices(date);
            }
            else if (exchange instanceof Gateio gateio) {
                gateio.updatePrices(date);
                //gateio.buildSymbols();
            }
            else if (exchange instanceof Bitget bitget) {
                bitget.updatePrices(date);
            }
        }
    }

    static void printSymbols(String exchange, String[] symbols) {
        out.printf("[%s]: [", exchange);
        for(String symbol: symbols) {
            out.printf("\"%s\", ", symbol);
        }
        out.printf("]\n");
    }

    static void binanceTicker(String symbol) {
        Binance binance = (Binance)exchanges.get("binance");
        out.printf("[Binance] %s ticker: %s\n", symbol, binance.ticker(symbol));
    }

    static void bitgetTicker(String symbol) {
        Bitget bitget = (Bitget)exchanges.get("bitget");
        out.printf("[Bitget] %s ticker: %s\n", symbol, bitget.ticker(symbol));
    }

    static void bybitTicker(String symbol) {
        Bybit bybit = (Bybit)exchanges.get("bybit");
        out.printf("[Bybit] %s ticker: %s\n", symbol, bybit.ticker("spot", symbol));
    }

    static void gateioTicker(String symbol) {
        Gateio gateio = (Gateio)exchanges.get("gateio");
        out.printf("[Gateio] %s ticker: %s\n", symbol, gateio.ticker(symbol));
    }

    static void testBybitApi() {
        Bybit bybit = (Bybit)exchanges.get("bybit");
        String[] symbols = bybit.get24hPercentageAllSymbols((float)0.3, 10);
        //String[] symbols = bybit.get24hPercentageSymbols(null, (float)0.3);
        out.printf("[Bybit]: ");
        for(int i = 0; i < symbols.length; i ++) {
            out.printf("\"%s\", ", symbols[i]);
        }
        out.printf("\n\n");
    }

    static void testBitstampApi() {
        String coin = "near";
        String network = "near";
        Bitstamp bitstamp = (Bitstamp)exchanges.get("bitstamp");
        out.printf("[Bitstamp] %s coin withdraw fee: %s\n", coin, bitstamp.withdrawalFeeCoin(coin, network));
    }

    static void testCoinbaseApi() {
        Coinbase coinbase = (Coinbase)exchanges.get("coinbase");
        out.printf("[Coinbase] Products: %s\n", coinbase.products());
    }

    static void testGateioApi() {
        Gateio gateio = (Gateio)exchanges.get("gateio");
        //out.printf("[Gateio] Info: %s\n", gateio.currencyInfo("ALGO"));
        //out.printf("%s\n", gateio.ticker("BTC_USDT"));
        out.printf("%s\n", gateio.balance("BTC"));
        gateio.shutdownExecutor();
    }

    static void testKrakenApi() {
        Kraken kraken = (Kraken)exchanges.get("kraken");
        //out.printf("Signature: %s\n", kraken.getSignature());
        //out.printf("[Kraken] Asset Info: %s\n", kraken.assetInfo("XBT"));
        //out.printf("[Kraken] Asset Info: %s\n", kraken.balance());
        String symbol = "NEARUSD";
        out.printf("[Kraken] NEARUSDT ticker: %s\n", kraken.ticker(symbol));
    }

    static void testKucoinApi() {
        Kucoin kucoin = (Kucoin)exchanges.get("kucoin");
        //out.printf("[Kucoin] CurrencyInfo: %s\n", kucoin.currencyInfo("ALGO"));
        out.printf("[Kucoin] AccountInfo: %s\n", kucoin.accountInfo());
    }

    static void testMexcApi() {
        Mexc mexc = (Mexc)exchanges.get("mexc");
        out.printf("[Mexc] AccountInfo: %s\n", mexc.accountInfo());
    }

    static void testBinanceApi() {
        Binance binance = (Binance)exchanges.get("binance");
        out.printf("[Binance]: ");
        //out.printf("[Binance] Ticker: %s\n", binance.ticker("ACEUSDT"));
        //out.printf("[Binance] Ticker: %s\n", binance.ticker("MANTAUSDC"));
        //String[] symbols = new String[]{"LSKUSDT", "MANTSUSDT", "MANTAUSDC"};
        String[] symbols = binance.get24hPercentageAllSymbols((float)0.2, (float)0.3);
        if (symbols == null) {
            return;
        }
        for(int i = 0; i < symbols.length; i ++) {
            out.printf("\"%s\", ", symbols[i]);
            //out.printf("[Binance] Ticker: %s\n", binance.ticker(symbols[i]));
        }
        out.printf("\n\n");
    }

    static boolean readHeader(SocketChannel channel, ByteBuffer limit, ByteBuffer isLast, long duration) {
        limit.reset();
        isLast.reset();
        long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(duration);
        boolean result;
        try {
            for(;;) {
                if (deadline - System.currentTimeMillis() <= 0L) {
                    out.printf("Deadline passed without receving header");
                    result = false;
                    break;
                }
                if (channel.read(limit) == 4) {
                    out.print("read limit\n");
                    if (channel.read(isLast) == 1) {
                        result = true;
                        out.print("read isLast\n");
                    } else {
                        result = false;
                    }
                    break;
                };
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return result;
    }


    static void testBybitOrdering() {
        Bybit bybit = (Bybit)exchanges.get("bybit");
        BybitJson bybitJson = bybit.getBybitJson();
        //BybitOrdering ordering = new BybitOrdering(bybit, bybitJson);
        //ordering.start(172800);
        // ZENDUSDT sell orders:
        // qty=10, orderId=
        String orderId = "1641448170695432960";
        bybitJson.getOrderStatus(orderId);
        //System.out.printf("%s\n", bybit.cancelOrder("ZENDUSDT", orderId));
        //System.out.printf("%s\n", bybit.openOrder("ZENDUSDT", "sell", "43", "4.98", "USDT"));
        //bybit.openOrder("NGLUSDT", "sell", "1.79", "214", "USDT");
        // ngl qty available: 365.08
        //bybit.openOrder("NGLUSDT", "sell", "2.35", "65.08", "USDT");
        
        /*
        JsonArray orders = Json.newJsonObject(bybit.getOrders("spot")).getJsonObject("result").getJsonArray("list");
        for(int i = 0; i < orders.size(); i ++) {
            out.printf("%s\n\n", orders.getJsonObject(i).toString());
        }
        
        //System.out.printf("%s\n", bybit.ticker("spot", "VVUSDT"));
       
        //out.printf("%s\n", String.format("%.2f", qty));
        
        
        String[] symbols = new String[]{"RPKUSDT", "GSTUSDT", "AIOZUSDT"};
        for(String symbol: symbols) {
            String ticker = bybit.ticker("spot", symbol);
            out.printf("ticker: %s\n", ticker);
            //float lastPrice = bybitJson.getLastPrice(ticker);
            //out.printf("symbol: %s, price: %s\n", symbol, lastPrice);
        }  
        */
    
        //out.printf("%s\n", bybit.ticker("spot", "ZENDUSDT"));
        /*
        String symbol = "VVUSDT";
        String orderId = "1618356672689085184";
        out.printf("order status: %s\n", bybitJson.getOrderStatus(bybit.getOrder(symbol, orderId)));
        */
        bybit.shutdownExecutor();

    }

    static void testGateioOrdering() {
        Gateio gateio = (Gateio)exchanges.get("gateio");
        GateioJson gateioJson = new GateioJson();
    }

    static void testSocket() {
        String protocol = "http";
        String host = "localhost";
        int port = 8080;
        String path = "";
        String query = "";
        
        try {
            
            SocketChannel channel = SocketChannel.open();
            channel.connect(new InetSocketAddress(host, port));
            ByteBuffer limit = ByteBuffer.allocate(4);
            ByteBuffer isLast = ByteBuffer.allocate(1);
            limit.mark();
            isLast.mark();
            out.printf("Limit: %s, Capacity: %s, Position: %s\n", limit.limit(), limit.capacity(), limit.position());
            if (readHeader(channel, limit, isLast, 5)) {
                out.printf("Limit: %s, Capacity: %s, Position: %s\n", limit.limit(), limit.capacity(), limit.position());
                out.printf("%s, %s\n", limit.get(2), limit.get(3));
                //limit.reset();
                out.printf("Limit: %s, Capacity: %s, Position: %s\n", limit.limit(), limit.capacity(), limit.position());
                out.printf("%s, %s\n", limit.get(2), limit.get(3));
                int read = channel.read(limit);
                out.printf("Bytes read: %s\n", read);
                out.printf("%s, %s\n", limit.get(2), limit.get(3));

            }

        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }
}