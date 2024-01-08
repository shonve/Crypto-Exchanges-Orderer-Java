package com.example.websocket;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;

import org.springframework.util.Assert;

import com.example.websocket.WebSocketClient.WebSocketClientBuilder.Exchange;
import com.example.websocket.WebSocketClient.WebSocketClientBuilder.Network;
import com.example.websocket.WebSocketClient.WebSocketClientBuilder.Channel;
import com.example.websocket.WebSocketClient.WebSocketClientBuilder.Market;


class WebSocketClientImpl {
    private static final PrintStream out = System.out;

    static AtomicLong Ids = new AtomicLong();

    private static ArrayList<WebSocketClientFacade> clients = new ArrayList<>();
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(1);
    private static final Duration DEFAULT_WAIT_TIMEOUT = Duration.ofSeconds(1);
    private static final int DEFAULT_ATTEMPTS = 3;

    private final Long id;
    private final Executor ex;
    private WebSocketClientFacade facade;
    private WebSocketClient webSocketClient;
    private WebSocket webSocket;
    private DefaultListener listener;

    private final WeakReference<WebSocketClient> webSocketClientRef;


    private class DefaultListener implements Listener {
        private PrintStream out = System.out;
        private final int DEFAULT_QUEUE_SIZE = 4;
        private Queue<ByteBuffer> pongMessageQueue = new ArrayDeque<>(DEFAULT_QUEUE_SIZE);
        private Queue<CharSequence> textMessageQueue = new ArrayDeque<>(DEFAULT_QUEUE_SIZE);
        private CompletableFuture<Void> start = new CompletableFuture<>();

        @Override 
        public void onOpen(WebSocket webSocket) {
            out.printf("Websocket has opened\n");
        }

        public void unreference(Throwable t) {
            if (t == null) {
                out.printf("Text handled successfully\n");
                return;
            }
            out.printf("Handle Text Exception: %s\n", t.getCause().getMessage());
        }

        public CompletableFuture<Void> handleTextAsync(CharSequence data, boolean last) {
            CompletableFuture<Void> cf = new CompletableFuture<>();

            return cf;
        }

        @Override 
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            out.printf("Received text data from websocket\n");
            return handleTextAsync(data, last);
            /*
            start.thenCompose(v -> handleTextAsync());
            if (last) {
                CompletableFuture<Void> finalStage = start.toCompletableFuture();
                finalStage.completeAsync(() -> null, ex);
                finalStage.whenComplete((v, t) -> unreference(t));
                start = new CompletableFuture<>();
            }
            return start;
            */
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            out.printf("Received binary data from websocket\n");
            return null;
        }

        @Override
        public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
            out.printf("Received Ping Message from the server: Ping Content: %s\n", message.toString());
            // send pong back to the client
            try {
                webSocket.sendPong(message).get();
                out.printf("Pong sent successfully\n");
            }
            catch(Exception e) {
                out.printf("Error sending pong due to: %s\n", e.getCause().getMessage());
            }
           return null;
        }

        @Override
        public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
            out.printf("Received Pong Message\n");
            pongMessageQueue.add(message);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            out.printf("Received close connection\n");
            return CompletableFuture.runAsync(null);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {}

        public ByteBuffer getPong() {
            return pongMessageQueue.peek() != null ? pongMessageQueue.remove() : null;
        }
        
    }
    
    private CompletableFuture<WebSocket> sendPingAsync(ByteBuffer message) {
        CompletableFuture<WebSocket> cf = this.webSocket.sendPing(message);
        try {
            int attempts = 0;
            if (attempts < DEFAULT_ATTEMPTS && cf.get(DEFAULT_WAIT_TIMEOUT.getSeconds(), TimeUnit.SECONDS) == null) {
                attempts += 1;
            }
            if (attempts == DEFAULT_ATTEMPTS) {
                cf.completeExceptionally(new IOException("Send Ping Timeout"));
            }
        } 
        catch(Exception e) {
            e.printStackTrace();
        }
        return cf;
    }

    private CompletableFuture<ByteBuffer> receivePongAsyn() {
        CompletableFuture<ByteBuffer> cf = new CompletableFuture<>(); 
        long deadline = System.nanoTime() + TimeUnit.NANOSECONDS.toNanos(DEFAULT_ATTEMPTS * DEFAULT_WAIT_TIMEOUT.getSeconds());
        for(;;) {
            ByteBuffer result = this.listener.getPong();
            if (result != null) {
                cf.complete(result);
                break;
            }
            if (deadline - System.nanoTime() <= 0L) {
                cf.completeExceptionally(new IOException("Pong Receive timeout"));
                break;
            }
        }

        return cf;
    }


    public CompletableFuture<ByteBuffer> sendPing(ByteBuffer message) {        
        CompletableFuture<Void> start = new CompletableFuture<>();
        CompletableFuture<ByteBuffer> cf = start.thenCompose(v -> sendPingAsync(message))
                                                .thenCompose(w -> receivePongAsyn());
        
        start.completeAsync(() -> null, ex);
        return cf;
    }

    public CompletableFuture<CharSequence> sendSubscription(CharSequence message) {
        CompletableFuture<Void> start = new CompletableFuture<>();
        CompletableFuture<ByteBuffer> cf = start.thenCompose(v -> sendSubscriptionAsync(message))
                                                .thenCompose(w -> receiveSubscriptionResponseAsyn());
        
        start.completeAsync(() -> null, ex);
        return cf;
    }

    private static final class DefaultThreadFactory implements ThreadFactory {
        private final String namePrefix;
        private final AtomicInteger threadIds = new AtomicInteger();

        public DefaultThreadFactory(long id, String name) {
            namePrefix = String.format("%s-%d-worker", name, id);
        }

        @Override
        public Thread newThread(Runnable t) {
            return new Thread(null, t, String.format("%s-%d", namePrefix, threadIds.incrementAndGet()), 0, false);
        }
    }


    private static final class SingleWebSocketClientFactory {
        WebSocketClient webSocketClient;
        WebSocketClientFacade facade;

        public WebSocketClientFacade createFacade(WebSocketClientImpl impl) {
            Assert.isNull(facade, "Facade is not null");
            facade = new WebSocketClientFacade(impl);
            return facade;
        }

        public WebSocketClient createWebSocketClient(BuilderImpl builder) {
            Assert.notNull(facade, "Facade must not be null");
            Assert.isNull(webSocketClient, "web socket client is not null");
            Exchange exchange = builder.exchange(); 

            switch (exchange) {
                case BYBIT:
                    webSocketClient = new BybitWebSocketClient(builder, this.facade);
                    break;
            
                case BINANCE:
                    webSocketClient = new BinanceWebSocketClient(builder, this.facade);
                    break;
                
                case COINBASE:
                    webSocketClient= new CoinbaseWebSocketClient(builder, this.facade);
                    break;
                
                case KRAKEN:
                    webSocketClient = new KrakenWebSocketClient(builder, this.facade);
                    break;
                
                default:
                    webSocketClient = null;
                    break;
            }
            return webSocketClient;
        }
        
    }

    private WebSocketClientImpl(BuilderImpl builder, SingleWebSocketClientFactory factory) {
        this.facade = factory.createFacade(this);
        Assert.notNull(this.facade, "Facade must not be null");
        this.webSocketClient = factory.createWebSocketClient(builder);
        Assert.notNull(this.webSocketClient, "web socket client must not be null");
        this.webSocketClientRef = new WeakReference<WebSocketClient>(this.webSocketClient);
        this.listener = new DefaultListener();
        
        HttpClient.Builder httpClientBuilder = HttpClient.newBuilder();
        httpClientBuilder.version(HttpClient.Version.HTTP_1_1);
        httpClientBuilder.connectTimeout(DEFAULT_CONNECT_TIMEOUT);
        WebSocket.Builder webSocketBuilder = httpClientBuilder.build().newWebSocketBuilder();
        webSocketBuilder.connectTimeout(DEFAULT_CONNECT_TIMEOUT);
        URL url = (this.webSocketClient instanceof AbstractWebSocketClient client) ? client.getURL() : null;
        Assert.notNull(url, "url must not be null");
        try {
            CompletableFuture<WebSocket> future = webSocketBuilder.buildAsync(url.toURI(), listener);
            this.webSocket = future.get(DEFAULT_CONNECT_TIMEOUT.getSeconds(), TimeUnit.SECONDS);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        Assert.notNull(webSocket, "websocket must not be null");
        this.id = Ids.incrementAndGet();
        String name = "";   
        if (this.webSocketClient instanceof AbstractWebSocketClient client) {
            name = client.getName();
        }
        this.ex = Executors.newCachedThreadPool(new DefaultThreadFactory(id, name));

    }

    public static WebSocketClient newWebSocketClient(BuilderImpl builder) {
        SingleWebSocketClientFactory factory = new SingleWebSocketClientFactory();
        WebSocketClientImpl impl = new WebSocketClientImpl(builder, factory);
        Assert.state(impl.webSocketClientRef.get() == factory.webSocketClient, "web socket client do not match");
        return impl.webSocketClientRef.get();
    }

}