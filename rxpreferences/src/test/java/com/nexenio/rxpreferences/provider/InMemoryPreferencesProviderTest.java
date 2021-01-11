package com.nexenio.rxpreferences.provider;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.observers.TestObserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InMemoryPreferencesProviderTest {

    private PreferencesProvider preferencesProvider;

    @Before
    public void setUp() {
        preferencesProvider = new InMemoryPreferencesProvider();
    }

    @Test
    public void getKeys_keysAvailable_emitsKeys() {
        List<String> expectedKeys = Observable.range(1, 5)
                .map(String::valueOf)
                .toList()
                .blockingGet();

        Observable.fromIterable(expectedKeys)
                .flatMapCompletable(key -> preferencesProvider.persist(key, key))
                .andThen(preferencesProvider.getKeys())
                .toList()
                .test()
                .assertValue(keys -> {
                    assertEquals(expectedKeys.size(), keys.size());
                    for (String key : keys) {
                        assertTrue(expectedKeys.contains(key));
                    }
                    return true;
                })
                .assertComplete();
    }

    @Test
    public void getKeys_noKeysAvailable_completesEmpty() {
        preferencesProvider.getKeys()
                .test()
                .assertNoValues()
                .assertComplete();
    }

    @Test
    public void containsKey_keyAvailable_emitsTrue() {
        preferencesProvider.persist("1", 1)
                .andThen(preferencesProvider.containsKey("1"))
                .test()
                .assertValue(true)
                .assertComplete();
    }

    @Test
    public void containsKey_keyNotAvailable_emitsFalse() {
        preferencesProvider.persist("1", 1)
                .andThen(preferencesProvider.containsKey("2"))
                .test()
                .assertValue(false)
                .assertComplete();
    }

    @Test
    public void restore_keyAvailable_emitsValue() {
        preferencesProvider.persist("1", 1)
                .andThen(preferencesProvider.restore("1", Integer.class))
                .test()
                .assertValue(1)
                .assertComplete();
    }

    @Test
    public void restore_keyNotAvailable_emitsError() {
        preferencesProvider.persist("1", 1)
                .andThen(preferencesProvider.restore("2", Integer.class))
                .test()
                .assertError(PreferenceProviderException.class);
    }

    @Test
    public void restoreOrDefault_keyAvailable_emitsValue() {
        preferencesProvider.persist("1", 1)
                .andThen(preferencesProvider.restoreOrDefault("1", 2))
                .test()
                .assertValue(1)
                .assertComplete();
    }

    @Test
    public void restoreOrDefault_keyNotAvailable_emitsDefault() {
        preferencesProvider.persist("1", 1)
                .andThen(preferencesProvider.restoreOrDefault("2", 2))
                .test()
                .assertValue(2)
                .assertComplete();
    }

    @Test
    public void restoreOrDefaultAndGetChanges_keyAvailable_emitsValue() {
        preferencesProvider.persist("1", 1)
                .andThen(preferencesProvider.persist("1", 2))
                .andThen(preferencesProvider.restoreOrDefaultAndGetChanges("1", 3))
                .test()
                .assertValue(2)
                .assertNotComplete();
    }

    @Test
    public void restoreOrDefaultAndGetChanges_keyAvailableAndChanges_emitsValueAndChanges() {
        preferencesProvider.persist("1", 1)
                .andThen(preferencesProvider.persist("1", 2))
                .blockingAwait();

        TestObserver<Integer> testObserver = preferencesProvider.restoreOrDefaultAndGetChanges("1", 3)
                .test()
                .assertValue(2)
                .assertNotComplete();

        preferencesProvider.persist("1", 4)
                .blockingAwait();

        testObserver.assertValues(2, 4)
                .assertNotComplete();
    }

    @Test
    public void restoreOrDefaultAndGetChanges_keyNotAvailable_emitsDefault() {
        preferencesProvider.restoreOrDefaultAndGetChanges("1", 1)
                .test()
                .assertValue(1)
                .assertNotComplete();
    }

    @Test
    public void restoreIfAvailable_keyAvailable_emitsValue() {
        preferencesProvider.persist("1", 1)
                .andThen(preferencesProvider.restoreIfAvailable("1", Integer.class))
                .test()
                .assertValue(1)
                .assertComplete();
    }

    @Test
    public void restoreIfAvailable_keyNotAvailable_completesEmpty() {
        preferencesProvider.persist("1", 1)
                .andThen(preferencesProvider.restoreIfAvailable("2", Integer.class))
                .test()
                .assertNoValues()
                .assertComplete();
    }

    @Test
    public void restoreIfAvailableAndGetChanges_keyAvailable_emitsValue() {
        preferencesProvider.persist("1", 1)
                .andThen(preferencesProvider.persist("1", 2))
                .andThen(preferencesProvider.restoreIfAvailableAndGetChanges("1", Integer.class))
                .test()
                .assertValue(2)
                .assertNotComplete();
    }

    @Test
    public void restoreIfAvailableAndGetChanges_keyAvailableAndChanges_emitsValueAndChanges() {
        preferencesProvider.persist("1", 1)
                .andThen(preferencesProvider.persist("1", 2))
                .blockingAwait();

        TestObserver<Integer> testObserver = preferencesProvider.restoreIfAvailableAndGetChanges("1", Integer.class)
                .test()
                .assertValue(2)
                .assertNotComplete();

        preferencesProvider.persist("1", 3)
                .blockingAwait();

        testObserver.assertValues(2, 3)
                .assertNotComplete();
    }

    @Test
    public void restoreIfAvailableAndGetChanges_keyNotAvailable_emitsNothing() {
        preferencesProvider.restoreIfAvailableAndGetChanges("1", Integer.class)
                .test()
                .assertNoValues()
                .assertNotComplete();
    }

    @Test
    public void persist_validValue_completes() {
        preferencesProvider.persist("1", 1)
                .andThen(preferencesProvider.restore("1", Integer.class))
                .test()
                .assertValue(1)
                .assertComplete();
    }

    @Test
    public void persistIfNotYetAvailable_keyNotAvailable_completes() {
        preferencesProvider.persistIfNotYetAvailable("1", 1)
                .andThen(preferencesProvider.restore("1", Integer.class))
                .test()
                .assertValue(1)
                .assertComplete();
    }

    @Test
    public void persistIfNotYetAvailable_keyAvailable_completesWithoutPersisting() {
        preferencesProvider.persist("1", 1)
                .andThen(preferencesProvider.persistIfNotYetAvailable("1", 2))
                .andThen(preferencesProvider.restore("1", Integer.class))
                .test()
                .assertValue(1)
                .assertComplete();
    }

    @Test
    public void getChanges_noChanges_emitsNothing() {
        preferencesProvider.getChanges("1", Integer.class)
                .test()
                .assertNoValues()
                .assertNotComplete();
    }

    @Test
    public void getChanges_changesAvailable_emitsChanges() {
        TestObserver<Integer> testObserver = preferencesProvider.persist("1", 1)
                .andThen(preferencesProvider.getChanges("1", Integer.class))
                .test()
                .assertNoValues()
                .assertNotComplete();

        preferencesProvider.persist("1", 2)
                .blockingAwait();

        testObserver.assertValue(2)
                .assertNotComplete();
    }

    @Test
    public void delete_keyAvailable_completes() {
        preferencesProvider.persist("1", 1)
                .andThen(preferencesProvider.delete("1"))
                .andThen(preferencesProvider.containsKey("1"))
                .test()
                .assertValue(false)
                .assertComplete();
    }

    @Test
    public void delete_keyNotAvailable_completes() {
        preferencesProvider.delete("1")
                .andThen(preferencesProvider.containsKey("1"))
                .test()
                .assertValue(false)
                .assertComplete();
    }

    @Test
    public void deleteAll_keysAvailable_completes() {
        preferencesProvider.persist("1", 1)
                .andThen(preferencesProvider.deleteAll())
                .andThen(preferencesProvider.getKeys())
                .test()
                .assertNoValues()
                .assertComplete();
    }

    @Test
    public void deleteAll_noKeysAvailable_completes() {
        preferencesProvider.deleteAll()
                .andThen(preferencesProvider.getKeys())
                .test()
                .assertNoValues()
                .assertComplete();
    }

}