package com.nexenio.rxpreferences.provider;

import com.nexenio.rxpreferences.RxPreferencesException;

public class PreferenceProviderException extends RxPreferencesException {

    public PreferenceProviderException() {
    }

    public PreferenceProviderException(String message) {
        super(message);
    }

    public PreferenceProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public PreferenceProviderException(Throwable cause) {
        super(cause);
    }

}
