package com.nexenio.rxpreferences.provider;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.File;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

/**
 * Provider that uses Androids {@link EncryptedSharedPreferences} for storing keys and values
 * encrypted.
 * <p>
 * Note taken from a <a href="https://google.github.io/tink/javadoc/tink-android/1.5.0/">library</a>
 * (AndroidKeysetManager) used for this:
 *
 * <p>Opportunistic keyset encryption with Android Keystore
 * Warning: because Android Keystore is unreliable, we strongly recommend disabling it by not
 * setting any master key URI. If a master key URI is set with AndroidKeysetManager.Builder.withMasterKeyUri(java.lang.String),
 * the keyset may be encrypted with a key generated and stored in Android Keystore.
 * <p>
 * Android Keystore is only available on Android M or newer. Since it has been found that Android
 * Keystore is unreliable on certain devices. Tink runs a self-test to detect such problems and
 * disables Android Keystore accordingly, even if a master key URI is set. You can check whether
 * Android Keystore is in use with isUsingKeystore().
 * <p>
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
    public static final String MASTER_KEY_ALIAS = MasterKey.DEFAULT_MASTER_KEY_ALIAS;

    private final String sharedPreferencesName;
    private final String masterKeyAlias;

    public EncryptedSharedPreferencesProvider(@NonNull Context context) {
        this(context, SHARED_PREFERENCES_NAME, MASTER_KEY_ALIAS);
    }

    public EncryptedSharedPreferencesProvider(@NonNull Context context, @NonNull String sharedPreferencesName, @NonNull String masterKeyAlias) {
        super(context);
        this.sharedPreferencesName = sharedPreferencesName;
        this.masterKeyAlias = masterKeyAlias;
        this.sharedPreferences = createEncryptedSharedPreferences(context).blockingGet();
    }

    protected Single<SharedPreferences> createEncryptedSharedPreferences(@NonNull Context context) {
        return Single.fromCallable(() -> {
            MasterKey masterKey = new MasterKey.Builder(context, masterKeyAlias)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .setRequestStrongBoxBacked(true)
                    .build();

            return EncryptedSharedPreferences.create(
                    context,
                    sharedPreferencesName,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        }).onErrorResumeNext(throwable -> Single.error(new PreferenceProviderException("Unable to create encrypted shared preferences", throwable)));
    }

    /**
     * Clears the underlying shared preferences in-memory and deletes the persisted files.
     * This needs to be done if the used master key is lost.
     */
    public Completable resetSharedPreferences(@NonNull Context context) {
        return Completable.fromAction(() -> context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
                .edit().clear().commit())
                .andThen(deletePreferencesFiles(context))
                .andThen(createEncryptedSharedPreferences(context)
                        .doOnSuccess(resetSharedPreferences -> this.sharedPreferences = resetSharedPreferences)
                        .ignoreElement()
                );
    }

    /**
     * Delete persisted shared preferences, so that it can't be restored on the next application
     * start.
     */
    public Completable deletePreferencesFiles(@NonNull Context context) {
        return Observable.just(
                new File(context.getApplicationInfo().dataDir + "/shared_prefs/" + sharedPreferencesName + ".xml"),
                new File(context.getApplicationInfo().dataDir + "/shared_prefs/" + sharedPreferencesName + ".xml.bak")
        ).filter(File::exists).map(File::delete).ignoreElements();
    }

}
