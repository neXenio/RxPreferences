package com.nexenio.rxpreferences.provider;

import com.nexenio.rxpreferences.serializer.Serializer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class InMemoryPreferencesProvider extends BasePreferencesProvider {

    @NonNull
    protected final Map<String, String> values;

    public InMemoryPreferencesProvider() {
        this.values = new HashMap<>();
    }

    public InMemoryPreferencesProvider(@NonNull Serializer serializer) {
        super(serializer);
        this.values = new HashMap<>();
    }

    @Override
    protected Maybe<String> restoreIfAvailable(@NonNull String key) {
        return Maybe.fromCallable(() -> values.get(key));
    }

    @Override
    public Completable persist(@NonNull String key, String value) {
        return Completable.fromAction(() -> {
            synchronized (values) {
                values.put(key, value);
            }
        });
    }

    @Override
    public Observable<String> getKeys() {
        return Observable.defer(() -> {
            Collection<String> keys;
            synchronized (values) {
                keys = Collections.synchronizedCollection(values.keySet());
            }
            return Observable.fromIterable(keys);
        });
    }

    @Override
    public Single<Boolean> containsKey(@NonNull String key) {
        return Single.fromCallable(() -> values.containsKey(key));
    }

    @Override
    public Completable delete(@NonNull String key) {
        return Completable.fromAction(() -> {
            synchronized (values) {
                values.remove(key);
            }
        });
    }

}
