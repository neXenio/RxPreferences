package com.nexenio.rxpreferences;

import android.content.Context;

import androidx.annotation.NonNull;

import com.nexenio.rxpreferences.provider.EncryptedSharedPreferencesProvider;
import com.nexenio.rxpreferences.provider.PreferencesProvider;
import com.nexenio.rxpreferences.provider.SharedPreferencesProvider;
import com.nexenio.rxpreferences.provider.TrayPreferencesProvider;

public final class RxPreferences {

    private RxPreferences() {

    }

    /**
     * @deprecated Tray library is no longer actively maintained
     */
    public static PreferencesProvider createTrayPreferencesProvider(@NonNull Context context) {
        return new TrayPreferencesProvider(context);
    }

    public static PreferencesProvider createSharedPreferencesProvider(@NonNull Context context) {
        return new SharedPreferencesProvider(context);
    }

    public static PreferencesProvider createEncryptedSharedPreferencesProvider(@NonNull Context context) {
        return new EncryptedSharedPreferencesProvider(context);
    }

    public static PreferencesProvider createInMemoryPreferencesProvider(@NonNull Context context) {
        return new SharedPreferencesProvider(context);
    }

}
