package com.nexenio.rxpreferences;

import android.content.Context;

import com.nexenio.rxpreferences.provider.PreferencesProvider;
import com.nexenio.rxpreferences.provider.TrayPreferencesProvider;

import androidx.annotation.NonNull;

public final class RxPreferences {

    private RxPreferences() {

    }

    public static PreferencesProvider createTrayProvider(@NonNull Context context) {
        return new TrayPreferencesProvider(context);
    }

}
