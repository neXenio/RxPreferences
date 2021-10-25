package com.nexenio.rxpreferences.provider;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class EncryptedSharedPreferencesProviderTest {

    private static final String SHARED_PREFERENCES_EXISTING = "shared_pref_test_existing";
    private static final String SHARED_PREFERENCES_NEW = "shared_pref_test_new";
    private static final String MASTER_KEY_ALIAS = "master_key_alias_test";

    private static final String ENCRYPTED_KEYS_KEY = "__androidx_security_crypto_encrypted_prefs_key_keyset__";
    private static final String ENCRYPTED_VALUES_KEY = "__androidx_security_crypto_encrypted_prefs_value_keyset__";


    private static Context context;
    private static KeyStore keyStore;

    @BeforeClass
    public static void setupClass() throws NoSuchProviderException, KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();

        keyStore = KeyStore.getInstance("AndroidKeyStore", "AndroidKeyStore");
        keyStore.load(null);
    }

    @Before
    public void setup() throws KeyStoreException {
        clear();
    }

    @AfterClass
    public static void cleanUp() throws Exception {
        clear();
    }

    /**
     * MasterKey unavailable
     * Persisted preferences unavailable
     * In-memory preferences unavailable
     * <p>
     * MasterKey available
     * Persisted preferences unavailable
     * In-memory preferences unavailable
     */
    @Test
    public void createEncryptedSharedPreferences_nothingExists_createsPreferences() {
        new EncryptedSharedPreferencesProvider(context, SHARED_PREFERENCES_NEW, MASTER_KEY_ALIAS);
    }

    /**
     * MasterKey unavailable
     * Persisted preferences available
     * In-memory preferences unavailable
     */
    @Test
    public void createEncryptedSharedPreferences_sharedPreferencesFileButNoKey_unableToRestoreOldKeys() throws KeyStoreException, FileNotFoundException {
        // create valid key and file
        String testKey = "TestKey";
        EncryptedSharedPreferencesProvider preferencesProvider = new EncryptedSharedPreferencesProvider(context, SHARED_PREFERENCES_NEW, MASTER_KEY_ALIAS);
        preferencesProvider.persist(testKey, "Value").blockingAwait();

        File file = new File(context.getApplicationInfo().dataDir + "/shared_prefs/" + SHARED_PREFERENCES_NEW + ".xml");
        Scanner scanner = new Scanner(file);
        boolean containsTestKey = false;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains("string") && !(line.contains(ENCRYPTED_KEYS_KEY) || line.contains(ENCRYPTED_VALUES_KEY))) {
                containsTestKey = true;
                break;
            }
        }
        assert containsTestKey;

        // remove key
        clearKeyStore();
        clearSharedPreferences();

        EncryptedSharedPreferencesProvider preferencesProvider1 = new EncryptedSharedPreferencesProvider(context, SHARED_PREFERENCES_NEW, MASTER_KEY_ALIAS);
        preferencesProvider1.getKeys().toList().test().assertError(SecurityException.class);
    }

    /**
     * MasterKey available
     * Persisted preferences available
     * In-memory preferences unavailable
     */
    @Test
    public void createEncryptedSharedPreferences_sharedPreferencesFileAndKey_createsPreferences() throws IOException {
        // create valid key
        new EncryptedSharedPreferencesProvider(context, SHARED_PREFERENCES_NEW, MASTER_KEY_ALIAS);

        // create valid file
        File validFile = new File(context.getApplicationInfo().dataDir + "/shared_prefs/" + SHARED_PREFERENCES_NEW + ".xml");
        File existingFile = new File(context.getApplicationInfo().dataDir + "/shared_prefs/" + SHARED_PREFERENCES_EXISTING + ".xml");

        Scanner scanner = new Scanner(validFile);
        Writer writer = new FileWriter(existingFile);
        while (scanner.hasNextLine()) {
            writer.write(scanner.nextLine());
        }
        scanner.close();
        writer.close();

        // create new preferences from existing values
        new EncryptedSharedPreferencesProvider(context, SHARED_PREFERENCES_EXISTING, MASTER_KEY_ALIAS);
    }

    /**
     * MasterKey unavailable
     * Persisted preferences unavailable
     * In-memory preferences available
     * <p>
     * MasterKey unavailable
     * Persisted preferences available
     * In-memory preferences available
     */
    @Test(expected = RuntimeException.class)
    public void createEncryptedSharedPreferences_masterKeyInMemoryButDeletedInKeystore_failsToCreatePreference() throws KeyStoreException {
        // create key and in-memory reference
        new EncryptedSharedPreferencesProvider(context, SHARED_PREFERENCES_NEW, MASTER_KEY_ALIAS);

        // remove key from key store
        clearKeyStore();

        new EncryptedSharedPreferencesProvider(context, SHARED_PREFERENCES_NEW, MASTER_KEY_ALIAS);
    }

    /**
     * MasterKey available
     * Persisted preferences unavailable
     * In-memory preferences available
     * <p>
     * MasterKey available
     * Persisted preferences available
     * In-memory preferences available
     */
    @Test
    public void createEncryptedSharedPreferences_everythingExists_createsPreference() {
        // create key, inMemoryReference and file
        new EncryptedSharedPreferencesProvider(context, SHARED_PREFERENCES_NEW, MASTER_KEY_ALIAS);

        new EncryptedSharedPreferencesProvider(context, SHARED_PREFERENCES_NEW, MASTER_KEY_ALIAS);
        deleteSharedPreferencesFiles(context, SHARED_PREFERENCES_NEW);
        new EncryptedSharedPreferencesProvider(context, SHARED_PREFERENCES_NEW, MASTER_KEY_ALIAS);
    }

    @Test
    public void clearInMemoryPreferences_unusableState_enablesCreationAgain() throws KeyStoreException {
        // create key and in-memory reference
        EncryptedSharedPreferencesProvider preferencesProvider = new EncryptedSharedPreferencesProvider(context, SHARED_PREFERENCES_NEW, MASTER_KEY_ALIAS);

        // remove key from key store
        clearKeyStore();

        Single<Integer> getKeys = Single.fromCallable(() -> new EncryptedSharedPreferencesProvider(context, SHARED_PREFERENCES_NEW, MASTER_KEY_ALIAS))
                .flatMapObservable(SharedPreferencesProvider::getKeys)
                .toList()
                .map(List::size);

        getKeys.test()
                .assertError(RuntimeException.class);

        preferencesProvider.resetSharedPreferences(context)
                .andThen(getKeys)
                .test()
                .assertValue(0);
    }

    @Test
    public void restore_deletedMasterKeyAndKeyPreferences_restoresValue() throws KeyStoreException {
        // create key and in-memory reference
        EncryptedSharedPreferencesProvider preferencesProvider = new EncryptedSharedPreferencesProvider(context, SHARED_PREFERENCES_NEW, MASTER_KEY_ALIAS);
        preferencesProvider.persist("testKey", "testValue").blockingAwait();

        clearKeyStore();
        clearSharedPreferences();

        preferencesProvider.restore("testKey", String.class)
                .test()
                .assertValue("testValue");
    }

    private static void clear() throws KeyStoreException {
        clearKeyStore();
        deleteSharedPreferencesFiles();
        clearSharedPreferences();
    }

    private static void clearKeyStore() throws KeyStoreException {
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            keyStore.deleteEntry(aliases.nextElement());
        }
    }

    private static void deleteSharedPreferencesFiles() {
        deleteSharedPreferencesFiles(context, SHARED_PREFERENCES_NEW);
        deleteSharedPreferencesFiles(context, SHARED_PREFERENCES_EXISTING);
    }

    private static void deleteSharedPreferencesFiles(@NonNull Context context, String sharedPreferencesName) {
        Observable.just(
                new File(context.getApplicationInfo().dataDir + "/shared_prefs/" + sharedPreferencesName + ".xml"),
                new File(context.getApplicationInfo().dataDir + "/shared_prefs/" + sharedPreferencesName + ".xml.bak")
        ).filter(File::exists).map(File::delete).ignoreElements().blockingAwait();
    }

    private static void clearSharedPreferences() {
        clearSharedPreferences(context.getSharedPreferences(SHARED_PREFERENCES_NEW, Context.MODE_PRIVATE));
        clearSharedPreferences(context.getSharedPreferences(SHARED_PREFERENCES_EXISTING, Context.MODE_PRIVATE));
    }

    private static void clearSharedPreferences(SharedPreferences sharedPreferences) {
        sharedPreferences.edit()
                .remove(ENCRYPTED_KEYS_KEY)
                .remove(ENCRYPTED_VALUES_KEY)
                .commit();
    }

}
