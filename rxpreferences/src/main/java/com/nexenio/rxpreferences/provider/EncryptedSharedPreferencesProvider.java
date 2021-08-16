package com.nexenio.rxpreferences.provider;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Provider that uses Androids {@link EncryptedSharedPreferences} for storing keys and values encrypted.
 */
public class EncryptedSharedPreferencesProvider extends SharedPreferencesProvider {

    public static final String SHARED_PREFERENCES_NAME = "encrypted_shared_preferences";

    public EncryptedSharedPreferencesProvider(@NonNull Context context) throws GeneralSecurityException, IOException {
        super(context);
        sharedPreferences = createEncryptedSharedPreferences(context);
    }

    public EncryptedSharedPreferencesProvider(@NonNull SharedPreferences sharedPreferences) {
        super(sharedPreferences);
    }

    private SharedPreferences createEncryptedSharedPreferences(@NonNull Context context) throws GeneralSecurityException, IOException {
        MasterKey masterKey = new MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .setRequestStrongBoxBacked(true)
                .build();

        return EncryptedSharedPreferences.create(
                context,
                SHARED_PREFERENCES_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

}
