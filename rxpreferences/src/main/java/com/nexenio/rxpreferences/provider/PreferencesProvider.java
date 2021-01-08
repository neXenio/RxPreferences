package com.nexenio.rxpreferences.provider;

import androidx.annotation.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface PreferencesProvider {

    Observable<String> getKeys();

    Single<Boolean> containsKey(@NonNull String key);

    <Type> Single<Type> restore(@NonNull String key, @NonNull Class<Type> typeClass);

    <Type> Single<Type> restoreOrDefault(@NonNull String key, @NonNull Type defaultValue);

    <Type> Maybe<Type> restoreIfAvailable(@NonNull String key, @NonNull Class<Type> typeClass);

    <Type> Observable<Type> restoreIfAvailableAndGetChanges(@NonNull String key, @NonNull Class<Type> typeClass);

    <Type> Completable persist(@NonNull String key, @NonNull Type value);

    <Type> Completable persistIfNotYetAvailable(@NonNull String key, @NonNull Type value);

    <Type> Observable<Type> getChanges(@NonNull String key, @NonNull Class<Type> typeClass);

    Completable delete(@NonNull String key);

    Completable deleteAll();

}
