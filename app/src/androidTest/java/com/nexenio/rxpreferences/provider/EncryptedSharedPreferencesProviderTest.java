package com.nexenio.rxpreferences.provider;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.After;
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

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;
import io.reactivex.rxjava3.core.Single;

public class EncryptedSharedPreferencesProviderTest {

    private static final String SHARED_PREFERENCES_EXISTING = "shared_pref_test_existing";
    private static final String SHARED_PREFERENCES_NEW = "shared_pref_test_new";
    private static final String MASTER_KEY_ALIAS = "master_key_alias_test";

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

    @After
    public void cleanUp() throws KeyStoreException {
        clear();
    }

    // Comment notes: MasterKey available / preferencesFile available / in-memory preferences available

    // FFF/TFF
    @Test
    public void createEncryptedSharedPreferences_nothingExists_createsPreferences() {
        new TestableEncryptedSharedPreferencesProvider(context, SHARED_PREFERENCES_NEW, MASTER_KEY_ALIAS);
    }

    // FTF
    @Test
    public void createEncryptedSharedPreferences_sharedPreferencesFileButNoKey_unableToRestoreOldKeys() throws KeyStoreException, FileNotFoundException {
        // create valid key and file
        String testKey = "TestKey";
        EncryptedSharedPreferencesProvider preferencesProvider = new TestableEncryptedSharedPreferencesProvider(context, SHARED_PREFERENCES_NEW, MASTER_KEY_ALIAS);
        preferencesProvider.persist(testKey, "Value").blockingAwait();

        File file = new File(context.getApplicationInfo().dataDir + "/shared_prefs/" + SHARED_PREFERENCES_NEW + ".xml");
        Scanner myReader = new Scanner(file);
        boolean containsTestKey = false;
        while (myReader.hasNextLine()) {
            String line = myReader.nextLine();
            if (line.contains("string") && !(line.contains("__androidx_security_crypto_encrypted_prefs_key_keyset__") || line.contains("__androidx_security_crypto_encrypted_prefs_value_keyset__"))) {
                containsTestKey = true;
                break;
            }
        }
        assert containsTestKey;

        // remove key
        clearKeyStore();
        removeBlockingInMemoryPreferences();

        EncryptedSharedPreferencesProvider preferencesProvider1 = new TestableEncryptedSharedPreferencesProvider(context, SHARED_PREFERENCES_NEW, MASTER_KEY_ALIAS);
        preferencesProvider1.getKeys().toList().test().assertError(SecurityException.class);
    }

    // TTF
    @Test
    public void createEncryptedSharedPreferences_sharedPreferencesFileAndKey_createsPreferences() throws IOException {
        // create valid key
        new TestableEncryptedSharedPreferencesProvider(context, SHARED_PREFERENCES_NEW, MASTER_KEY_ALIAS);

        // create valid file
        File validFile = new File(context.getApplicationInfo().dataDir + "/shared_prefs/" + SHARED_PREFERENCES_NEW + ".xml");
        File existingFile = new File(context.getApplicationInfo().dataDir + "/shared_prefs/" + SHARED_PREFERENCES_EXISTING + ".xml");

        Scanner myReader = new Scanner(validFile);
        Writer writer = new FileWriter(existingFile);
        while (myReader.hasNextLine()) {
            writer.write(myReader.nextLine());
        }
        myReader.close();
        writer.close();

        // create new preferences from existing values
        new TestableEncryptedSharedPreferencesProvider(context, SHARED_PREFERENCES_EXISTING, MASTER_KEY_ALIAS);
    }

    // FFT/FTT
    @Test(expected = RuntimeException.class)
    public void createEncryptedSharedPreferences_masterKeyInMemoryButDeletedInKeystore_failsToCreatePreference() throws KeyStoreException {
        // create key and in-memory reference
        new TestableEncryptedSharedPreferencesProvider(context, SHARED_PREFERENCES_NEW, MASTER_KEY_ALIAS);

        // remove key from key store
        clearKeyStore();

        new TestableEncryptedSharedPreferencesProvider(context, SHARED_PREFERENCES_NEW, MASTER_KEY_ALIAS);
    }

    // TFT/TTT
    @Test
    public void createEncryptedSharedPreferences_everythingExists_createsPreference() {
        // create key, inMemoryReference and file
        new TestableEncryptedSharedPreferencesProvider(context, SHARED_PREFERENCES_NEW, MASTER_KEY_ALIAS);

        new TestableEncryptedSharedPreferencesProvider(context, SHARED_PREFERENCES_NEW, MASTER_KEY_ALIAS);
        deleteFile(context, SHARED_PREFERENCES_NEW);
        new TestableEncryptedSharedPreferencesProvider(context, SHARED_PREFERENCES_NEW, MASTER_KEY_ALIAS);
    }

    @Test
    public void clearInMemoryPreferences_unusableState_enablesCreationAgain() throws KeyStoreException {
        // create key and in-memory reference
        EncryptedSharedPreferencesProvider preferencesProvider = new TestableEncryptedSharedPreferencesProvider(context, SHARED_PREFERENCES_NEW, MASTER_KEY_ALIAS);

        // remove key from key store
        clearKeyStore();

        Single<Integer> getKeys = Single.fromCallable(() -> new TestableEncryptedSharedPreferencesProvider(context, SHARED_PREFERENCES_NEW, MASTER_KEY_ALIAS))
                .flatMapObservable(SharedPreferencesProvider::getKeys)
                .toList()
                .map(List::size);

        getKeys.test()
                .assertError(RuntimeException.class);

        preferencesProvider.resetPreferences(context)
                .andThen(getKeys)
                .test()
                .assertValue(0);
    }

    @Test
    public void restore_deletedMasterKeyAndKeyPreferences_restoresValue() throws KeyStoreException {
        // create key and in-memory reference
        EncryptedSharedPreferencesProvider preferencesProvider = new TestableEncryptedSharedPreferencesProvider(context, SHARED_PREFERENCES_NEW, MASTER_KEY_ALIAS);
        preferencesProvider.persist("testKey", "testValue").blockingAwait();

        clearKeyStore();
        removeBlockingInMemoryPreferences();

        preferencesProvider.restore("testKey", String.class)
                .test()
                .assertValue("testValue");
    }

    private void clear() throws KeyStoreException {
        clearKeyStore();
        deleteFiles();
        clearInMemoryPreferences();
    }

    private static void clearKeyStore() throws KeyStoreException {
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            keyStore.deleteEntry(aliases.nextElement());
        }
    }

    private static void deleteFiles() {
        deleteFile(context, SHARED_PREFERENCES_NEW);
        deleteFile(context, SHARED_PREFERENCES_EXISTING);
    }

    private static void deleteFile(@NonNull Context context, String preferencesFileName) {
        File file = new File(context.getApplicationInfo().dataDir + "/shared_prefs/" + preferencesFileName + ".xml");
        if (file.exists()) {
            if (!file.delete()) {
                throw new IllegalStateException("Unable to delete preferences file");
            }
        }
        file = new File(context.getApplicationInfo().dataDir + "/shared_prefs/" + preferencesFileName + ".xml.bak");
        if (file.exists()) {
            if (!file.delete()) {
                throw new IllegalStateException("Unable to delete preferences file");
            }
        }
    }

    private static void clearInMemoryPreferences() {
        removeBlockingInMemoryPreference(context.getSharedPreferences(SHARED_PREFERENCES_NEW, Context.MODE_PRIVATE));
        removeBlockingInMemoryPreference(context.getSharedPreferences(SHARED_PREFERENCES_EXISTING, Context.MODE_PRIVATE));
    }

    private static void clearInMemoryPreference(SharedPreferences sharedPreferences) {
        sharedPreferences
                .edit()
                .clear()
                .commit();
    }

    private static void removeBlockingInMemoryPreferences() {
        removeBlockingInMemoryPreference(context.getSharedPreferences(SHARED_PREFERENCES_NEW, Context.MODE_PRIVATE));
        removeBlockingInMemoryPreference(context.getSharedPreferences(SHARED_PREFERENCES_EXISTING, Context.MODE_PRIVATE));
    }

    private static void removeBlockingInMemoryPreference(SharedPreferences sharedPreferences) {
        sharedPreferences
                .edit()
                .remove("__androidx_security_crypto_encrypted_prefs_key_keyset__")
                .remove("__androidx_security_crypto_encrypted_prefs_value_keyset__")
                .commit();
    }

}
