package com.example.websocket;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.springframework.util.Assert;


class WebSocketClientFacade {
    private static final PrintStream out = System.out;
    private final WebSocketClientImpl impl;

    public WebSocketClientFacade(WebSocketClientImpl impl) {
        Assert.notNull(impl, "web socket client impl must not be null");
        this.impl = impl;
    }

    // client sends ping, and gets pong back in response
    public ByteBuffer ping(ByteBuffer message) {
        CompletableFuture<ByteBuffer> cf = this.impl.sendPing(message);
        ByteBuffer result = null;
        try {
            result = cf.get();
        }
        catch(Exception e) {
            out.printf("%s\n", e.getCause().getMessage());
        }
        return result;
    }

    public CharSequence subscribe(CharSequence message) {
        CompletableFuture<CharSequence> cf = this.impl.sendSubscription(message);
        CharSequence result = null;
        try {
            result = cf.get();
        }
        catch(Exception e) {
            out.printf("%s\n", e.getCause().getMessage());
        }
        return result;
    }

}