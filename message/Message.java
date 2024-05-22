package com.example.message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;  // try AtomicInteger

import jakarta.json.spi.JsonProvider;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;

abstract class Message {
    static final java.io.PrintStream out = System.out;
    private static AtomicLong counter = new AtomicLong();
    private static final JsonProvider jsonProvider = JsonProvider.provider();

    private static long getId() {
        return counter.incrementAndGet();
    }

    public static ByteBuffer binanceMessage(String method, Set<String> params) throws IOException {
        ByteBuffer message = null;
        try {
            JsonObjectBuilder jsonObjectBuilder = jsonProvider.createObjectBuilder();
            jsonObjectBuilder.add("method", method);
            JsonArrayBuilder jsonArrayBuilder = jsonProvider.createArrayBuilder();
            if (params != null) {
                params.forEach((param) -> {
                    jsonArrayBuilder.add(param);
                });
            }
            jsonObjectBuilder.add("params", jsonArrayBuilder.build());
            jsonObjectBuilder.add("id", getId());
            message = ByteBuffer.wrap(jsonObjectBuilder.build().toString().getBytes());
        }
        catch(Exception e) {
            throw new MessageParsingException(e);
        }
        return message;
    }

    public static ByteBuffer bybitMessage(String op, Set<String> args) throws IOException {
        ByteBuffer message = null;
        try {
            JsonObjectBuilder jsonObjectBuilder = jsonProvider.createObjectBuilder();
            jsonObjectBuilder.add("req_id", String.valueOf(getId()));
            jsonObjectBuilder.add("op", op);
            if (args != null) {
                JsonArrayBuilder jsonArrayBuilder = jsonProvider.createArrayBuilder();
                if (args != null) {
                    args.forEach((arg) -> {
                        jsonArrayBuilder.add(arg);
                    });
                }
                jsonObjectBuilder.add("args", jsonArrayBuilder.build());
            }
            String textMessage = jsonObjectBuilder.build().toString();
            message = ByteBuffer.wrap(textMessage.getBytes());
        }
        catch(Exception e) {
            throw new MessageParsingException(e);
        }
        return message;
    }
    
}