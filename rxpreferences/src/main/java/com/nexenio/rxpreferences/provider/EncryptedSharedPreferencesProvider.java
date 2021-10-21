package com.nexenio.rxpreferences.provider;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * Provider that uses Androids {@link EncryptedSharedPreferences} for storing keys and values
 * encrypted.
 *
 * Note taken from a <a href="https://google.github.io/tink/javadoc/tink-android/1.5.0/">library</a>
 * (AndroidKeysetManager) used for this:
 *
 * <p>Opportunistic keyset encryption with Android Keystore
 * Warning: because Android Keystore is unreliable, we strongly recommend disabling it by not
 * setting any master key URI. If a master key URI is set with AndroidKeysetManager.Builder.withMasterKeyUri(java.lang.String),
 * the keyset may be encrypted with a key generated and stored in Android Keystore.
 *
 * Android Keystore is only available on Android M or newer. Since it has been found that Android
 * Keystore is unreliable on certain devices. Tink runs a self-test to detect such problems and
 * disables Android Keystore accordingly, even if a master key URI is set. You can check whether
 * Android Keystore is in use with isUsingKeystore().
 *
 * When Android Keystore is disabled or otherwise unavailable, keysets will be stored in cleartext.
 * This is not as bad as it sounds because keysets remain inaccessible to any other apps running on
 * the same device. Moreover, as of July 2020, most active Android devices support either full-disk
 * encryption or file-based encryption, which provide strong security protection against key theft
 * even from attackers with physical access to the device. Android Keystore is only useful when you
 * want to require user authentication for key use, which should be done if and only if you're
 * absolutely sure that Android Keystore is working properly on your target devices.</p>
 *
 * @see <a href="https://github.com/google/tink/issues/413"/>
 * @see <a href="https://github.com/google/tink/issues/504"/>
 * @see <a href="https://issuetracker.google.com/issues/164901843?pli=1"/>
 * @see <a href="https://issuetracker.google.com/issues/158234058?pli=1"/>
 */
public class EncryptedSharedPreferencesProvider extends SharedPreferencesProvider {

    public static final String SHARED_PREFERENCES_NAME = "encrypted_shared_preferences";

    public EncryptedSharedPreferencesProvider(@NonNull Context context) {
        super(context);
        sharedPreferences = createEncryptedSharedPreferences(context).blockingGet();
    }

    protected Single<SharedPreferences> createEncryptedSharedPreferences(@NonNull Context context) {
        return Single.fromCallable(() -> {
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
        }).onErrorResumeNext(throwable -> Single.error(new PreferenceProviderException("Unable to create encrypted shared preferences", throwable)));
    }

    /**
     * Clear preferences in-memory and in the files. This should be done if the master key used is
     * lost or if you wish to remove preferences.
     */
    public Completable resetPreferences(@NonNull Context context) {
        return Completable.fromAction(() -> context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
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
    public Completable deletePreferencesFile(@NonNull Context context) {
        return Completable.fromAction(() -> {
            File file = new File(context.getApplicationInfo().dataDir + "/shared_prefs/" + SHARED_PREFERENCES_NAME + ".xml");
            if (file.exists()) {
                if (!file.delete()) {
                    throw new IllegalStateException("Unable to delete preferences file");
                }
            }
            file = new File(context.getApplicationInfo().dataDir + "/shared_prefs/" + SHARED_PREFERENCES_NAME + ".xml.bak");
            if (file.exists()) {
                if (!file.delete()) {
                    throw new IllegalStateException("Unable to delete preferences file");
                }
            }
        });
    }

}
