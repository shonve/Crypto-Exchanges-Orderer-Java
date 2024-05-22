package com.example.json;

import java.io.IOException;

class JsonParsingException extends IOException {

    public JsonParsingException(Throwable e) {
        super(e);
    }
}