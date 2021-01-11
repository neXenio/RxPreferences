package com.nexenio.rxpreferences.provider;

import androidx.annotation.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface PreferencesProvider {

    /**
     * Should emit all available preference keys and complete.
     */
    Observable<String> getKeys();

    /**
     * Should emit {@code true} if a preference with the specified key exists, {@code false}
     * otherwise.
     */
    Single<Boolean> containsKey(@NonNull String key);

    /**
     * Should emit the previously persisted value for the specified key.
     *
     * Should emit a {@link PreferenceProviderException} if no value is available or the value has
     * the wrong type.
     */
    <Type> Single<Type> restore(@NonNull String key, @NonNull Class<Type> typeClass);

    /**
     * Should emit the previously persisted value for the specified key, or the specified default if
     * no value is available.
     *
     * Should emit a {@link PreferenceProviderException} if a value is available but has the wrong
     * type.
     */
    <Type> Single<Type> restoreOrDefault(@NonNull String key, @NonNull Type defaultValue);

    /**
     * Should emit the previously persisted value for the specified key, or complete empty of no
     * value is available.
     *
     * Should emit a {@link PreferenceProviderException} if a value is available but has the wrong
     * type.
     */
    <Type> Maybe<Type> restoreIfAvailable(@NonNull String key, @NonNull Class<Type> typeClass);

    /**
     * Convenience method that should combine {@link #restoreIfAvailable(String, Class)} and {@link
     * #getChanges(String, Class)}.
     */
    <Type> Observable<Type> restoreIfAvailableAndGetChanges(@NonNull String key, @NonNull Class<Type> typeClass);

    /**
     * Should persist the specified value for the specified key.
     */
    <Type> Completable persist(@NonNull String key, @NonNull Type value);

    /**
     * Should persist the specified value for the specified key if no value is available.
     */
    <Type> Completable persistIfNotYetAvailable(@NonNull String key, @NonNull Type value);

    /**
     * Should emit the current value every time a value is persisted for the specified key. Should
     * never complete.
     */
    <Type> Observable<Type> getChanges(@NonNull String key, @NonNull Class<Type> typeClass);

    /**
     * Should delete the value for the specified key. Should do nothing if no value is available.
     */
    Completable delete(@NonNull String key);

    /**
     * Should delete the values for every available key.
     */
    Completable deleteAll();

}
