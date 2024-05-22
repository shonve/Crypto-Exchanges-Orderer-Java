package com.example.message;

import java.io.IOException;

class MessageParsingException extends IOException {

    public MessageParsingException(Throwable e) {
        super(e);
    }

    public MessageParsingException(String reason) {
        super(reason);
    }
}