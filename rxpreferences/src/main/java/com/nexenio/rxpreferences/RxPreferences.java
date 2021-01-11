package com.nexenio.rxpreferences;

import android.content.Context;

import com.nexenio.rxpreferences.provider.PreferencesProvider;
import com.nexenio.rxpreferences.provider.SharedPreferencesProvider;
import com.nexenio.rxpreferences.provider.TrayPreferencesProvider;

import androidx.annotation.NonNull;

public final class RxPreferences {

    private RxPreferences() {

    }

    public static PreferencesProvider createTrayPreferencesProvider(@NonNull Context context) {
        return new TrayPreferencesProvider(context);
    }

    public static PreferencesProvider createSharedPreferencesProvider(@NonNull Context context) {
        return new SharedPreferencesProvider(context);
    }

    public static PreferencesProvider createInMemoryPreferencesProvider(@NonNull Context context) {
        return new SharedPreferencesProvider(context);
    }

}
