package com.nexenio.rxpreferences.serializer;

import com.nexenio.rxpreferences.RxPreferencesException;

public class SerializerException extends RxPreferencesException {

    public SerializerException() {
    }

    public SerializerException(String message) {
        super(message);
    }

    public SerializerException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializerException(Throwable cause) {
        super(cause);
    }

}
