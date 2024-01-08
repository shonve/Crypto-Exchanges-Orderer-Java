package com.example.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.springframework.util.Assert;

public class FilterBody {
    private static final int DEFAULT_CHUNK_SIZE = 1024;

    public static String read(InputStream body) throws IOException {
        int size = body.available() > DEFAULT_CHUNK_SIZE ? DEFAULT_CHUNK_SIZE : body.available();
        if (size <= 0) {
            return "";
        }
        InputStreamReader reader = new InputStreamReader(body, StandardCharsets.UTF_8);
        char data[] = new char[size];
        int charsRead = reader.read(data, 0, size);
        Assert.state(charsRead == size, "Data read must be equal to the expected size");
        body.skip(size);
        return (new StringBuilder()).append(data).toString();
    }

}