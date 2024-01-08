package com.example.message;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.json.spi.JsonProvider;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;

abstract class Message {
    private static AtomicLong counter = new AtomicLong();
    private static final JsonProvider jsonProvider = JsonProvider.provider();

    private static long getId() {
        return counter.incrementAndGet();
    }

    public static ByteBuffer binanceMessage(String method, Set<String> params) {
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
        return ByteBuffer.wrap(jsonObjectBuilder.build().toString().getBytes(StandardCharsets.UTF_8));
    }

    public static ByteBuffer bybitMessage(String op, Set<String> args) {
        JsonObjectBuilder jsonObjectBuilder = jsonProvider.createObjectBuilder();
        jsonObjectBuilder.add("op", op);
        JsonArrayBuilder jsonArrayBuilder = jsonProvider.createArrayBuilder();
        if (args != null) {
            args.forEach((arg) -> {
                jsonArrayBuilder.add(arg);
            });
        }
        jsonObjectBuilder.add("args", jsonArrayBuilder.build());
        jsonObjectBuilder.add("req_id", getId());
        return ByteBuffer.wrap(jsonObjectBuilder.build().toString().getBytes(StandardCharsets.UTF_8));
    }
}