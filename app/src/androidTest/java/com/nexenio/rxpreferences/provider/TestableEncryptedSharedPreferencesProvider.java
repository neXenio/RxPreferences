package com.nexenio.rxpreferences.provider;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class TestableEncryptedSharedPreferencesProvider extends EncryptedSharedPreferencesProvider {

    private String fileName;

    public TestableEncryptedSharedPreferencesProvider(@NonNull Context context) {
        this(context, SHARED_PREFERENCES_NAME, MasterKey.DEFAULT_MASTER_KEY_ALIAS);
    }

    public TestableEncryptedSharedPreferencesProvider(@NonNull Context context, String fileName, String masterKeyAlias) {
        super(context);
        sharedPreferences = createEncryptedSharedPreferences(context, fileName, masterKeyAlias).blockingGet();
        this.fileName = fileName;
    }

    @Override
    protected Single<SharedPreferences> createEncryptedSharedPreferences(@NonNull Context context) {
        return Single.just(new DummySharedPreference());
    }

    protected Single<SharedPreferences> createEncryptedSharedPreferences(@NonNull Context context, String fileName, String masterKeyAlias) {
        return Single.fromCallable(() -> {
            MasterKey masterKey = new MasterKey.Builder(context, masterKeyAlias)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .setRequestStrongBoxBacked(true)
                    .build();

            return EncryptedSharedPreferences.create(
                    context,
                    fileName,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        });
    }

    @Override
    public Completable resetPreferences(@NonNull Context context) {
        return Completable.fromAction(() -> context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit())
                .andThen(deletePreferencesFile(context))
                .andThen(Completable.fromAction(() -> sharedPreferences = createEncryptedSharedPreferences(context).blockingGet()));
    }

    /**
     * Delete persisted shared preferences, so that it can't be restored on the next application
     * start.
     */
    @Override
    public Completable deletePreferencesFile(@NonNull Context context) {
        return Completable.fromAction(() -> {
            File file = new File(context.getApplicationInfo().dataDir + "/shared_prefs/" + fileName + ".xml");
            if (file.exists()) {
                if (!file.delete()) {
                    throw new IllegalStateException("Unable to delete preferences file");
                }
            }
            file = new File(context.getApplicationInfo().dataDir + "/shared_prefs/" + fileName + ".xml.bak");
            if (file.exists()) {
                if (!file.delete()) {
                    throw new IllegalStateException("Unable to delete preferences file");
                }
            }
        });
    }

    static class DummySharedPreference implements SharedPreferences {

        @Override
        public Map<String, ?> getAll() {
            return null;
        }

        @Nullable
        @Override
        public String getString(String key, @Nullable String defValue) {
            return null;
        }

        @Nullable
        @Override
        public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
            return null;
        }

        @Override
        public int getInt(String key, int defValue) {
            return 0;
        }

        @Override
        public long getLong(String key, long defValue) {
            return 0;
        }

        @Override
        public float getFloat(String key, float defValue) {
            return 0;
        }

        @Override
        public boolean getBoolean(String key, boolean defValue) {
            return false;
        }

        @Override
        public boolean contains(String key) {
            return false;
        }

        @Override
        public Editor edit() {
            return null;
        }

        @Override
        public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {

        }

        @Override
        public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {

        }

    }

}
