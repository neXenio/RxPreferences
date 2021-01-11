package com.nexenio.rxpreferences.provider;

import androidx.annotation.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class WrappedPreferencesProvider implements PreferencesProvider {

    @NonNull
    protected PreferencesProvider provider;

    public WrappedPreferencesProvider(@NonNull PreferencesProvider provider) {
        this.provider = provider;
    }

    @Override
    public Observable<String> getKeys() {
        return provider.getKeys();
    }

    @Override
    public Single<Boolean> containsKey(@NonNull String key) {
        return provider.containsKey(key);
    }

    @Override
    public <Type> Single<Type> restore(@NonNull String key, @NonNull Class<Type> typeClass) {
        return provider.restore(key, typeClass);
    }

    @Override
    public <Type> Single<Type> restoreOrDefault(@NonNull String key, @NonNull Type defaultValue) {
        return provider.restoreOrDefault(key, defaultValue);
    }

    @Override
    public <Type> Observable<Type> restoreOrDefaultAndGetChanges(@NonNull String key, @NonNull Type defaultValue) {
        return provider.restoreOrDefaultAndGetChanges(key, defaultValue);
    }

    @Override
    public <Type> Maybe<Type> restoreIfAvailable(@NonNull String key, @NonNull Class<Type> typeClass) {
        return provider.restoreIfAvailable(key, typeClass);
    }

    @Override
    public <Type> Observable<Type> restoreIfAvailableAndGetChanges(@NonNull String key, @NonNull Class<Type> typeClass) {
        return provider.restoreIfAvailableAndGetChanges(key, typeClass);
    }

    @Override
    public <Type> Completable persist(@NonNull String key, @NonNull Type value) {
        return provider.persist(key, value);
    }

    @Override
    public <Type> Completable persistIfNotYetAvailable(@NonNull String key, @NonNull Type value) {
        return provider.persistIfNotYetAvailable(key, value);
    }

    @Override
    public <Type> Observable<Type> getChanges(@NonNull String key, @NonNull Class<Type> typeClass) {
        return provider.getChanges(key, typeClass);
    }

    @Override
    public Completable delete(@NonNull String key) {
        return provider.delete(key);
    }

    @Override
    public Completable deleteAll() {
        return provider.deleteAll();
    }

    @NonNull
    public PreferencesProvider getProvider() {
        return provider;
    }

    public void setProvider(@NonNull PreferencesProvider provider) {
        this.provider = provider;
    }

}
