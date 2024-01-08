package com.example.websocket;

import java.io.PrintStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.util.Assert;

import com.example.message.Ping;
import com.example.message.Subscribe;
import com.example.util.Utils;

abstract class AbstractWebSocketClient implements WebSocketClient {
    String name = "Abstract";
    String protocol;
    String mainnetHost;
    String testnetHost;
    String version;
    URL url;
    WebSocketClientFacade facade;
    SubscriptionSet subscriptions;
    final int DEFAULT_PORT = 443;
    final static PrintStream out = System.out;
    final static ReadWriteFactory readWriteFactory = new ReadWriteFactory();

    final static class Message<T> {
        T message;
        Boolean last;

        Message(T message, Boolean last) {
            this.message = message;
            this.last = last;
        }

        public T getMessage() {
            return this.message;
        }

        public boolean isLast() {
            return this.last;
        }
    }

    final static class DefaultThreadFactory implements ThreadFactory {
        final String namePrefix;
        final AtomicInteger Ids = new AtomicInteger();

        public DefaultThreadFactory()
    }

    final static class ReadWriteFactory {
        private final int capacity = 8096;  // factory can store 8096 bytes
        private final Queue<Message<CharSequence>> textMessageQueue = new ArrayDeque<>();
        private final Queue<Message<ByteBuffer>> binaryMessageQueue = new ArrayDeque<>();
        private int textLimit = 0;
        private int binaryLimit = 0;

        public boolean writeText(CharSequence message, boolean last) {
            int bytesStored = binaryLimit + textLimit;
            if (bytesStored + message.length() > capacity) {
                return false;
            }
            textLimit += message.length();
            textMessageQueue.add(new Message<CharSequence>(message, last));
            return true;
        }

        public boolean writeBinary(ByteBuffer message, boolean last) {
            int bytesStored = binaryLimit + textLimit;
            if (bytesStored + message.limit() > capacity) {
                return false;
            }
            binaryLimit += message.limit();
            binaryMessageQueue.add(new Message<ByteBuffer>(message, last));
            return true;
        }

        public boolean textAvailable() {
            return textMessageQueue.size() > 0;
        }

        public boolean binaryAvailable() {
            return binaryMessageQueue.size() > 0;
        }

        public Message<CharSequence> readText() {
            if (textMessageQueue.size() > 0) {
                Message<CharSequence> message = textMessageQueue.remove();
                textLimit -= message.getMessage().length();
                return message;
            }
            return null;
        }

        public Message<ByteBuffer> readBinary() {
            if (binaryMessageQueue.size() > 0) {
                Message<ByteBuffer> message = binaryMessageQueue.remove();
                binaryLimit -= message.getMessage().limit();
                return message;
            }
            return null;
        }

    }

    public URL getURL() {
        return this.url;
    }

    public String getName() {
        return this.name;
    }

    public abstract String orderBookStream(String symbol, int level);
    
    public abstract String tradeStream(String symbol);

    public boolean subscribe(SubscriptionSet subscriptions) {
        //this.subscriptions = subscriptions;
        CharSequence message = Subscribe.of(this.name, subscriptions);
        Assert.notNull(message, String.format("%s cannot subscribe", this.name));
        CompletableFuture<CharBuffer> cf = this.facade.subscribe(message);

    }

    public static WebSocketClientBuilder newWebSocketClientBuilder() {
        return new BuilderImpl();
    }

    protected boolean removeSubscription(String subscription) {
        if (facade != null) {
            return true;
        }
        return false;
    }

    public void sendPing() {
        ByteBuffer message = Ping.of(this.name);
        Assert.notNull(message, String.format("Ping message can not be resolved for exchange %s\n", this.name));
        String result = 
        this.facade.ping(message) != null ? 
        String.format("Pong received\n") : 
        String.format("Pong not received\n");
        out.print(result);
    }

    public SubscriptionSetBuilder newSubscriptionSetBuilder() {
        return new SubscriptionSetBuilderImpl(this::orderBookStream, this::tradeStream);
    }

}