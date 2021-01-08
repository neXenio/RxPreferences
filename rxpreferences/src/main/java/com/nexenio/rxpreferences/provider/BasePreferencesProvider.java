package com.nexenio.rxpreferences.provider;

import com.nexenio.rxpreferences.serializer.GsonSerializer;
import com.nexenio.rxpreferences.serializer.Serializer;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.PublishSubject;

public abstract class BasePreferencesProvider implements PreferencesProvider {

    @NonNull
    protected Serializer serializer;

    @NonNull
    @SuppressWarnings("rawtypes")
    protected final Map<String, PublishSubject> changePublishers;

    public BasePreferencesProvider() {
        this(new GsonSerializer());
    }

    public BasePreferencesProvider(@NonNull Serializer serializer) {
        this.serializer = serializer;
        this.changePublishers = new HashMap<>();
    }

    @Override
    public <Type> Single<Type> restore(@NonNull String key, @NonNull Class<Type> typeClass) {
        return restoreIfAvailable(key, typeClass)
                .switchIfEmpty(Single.error(new PreferenceProviderException("No preference available with key: " + key)));
    }

    @Override
    public <Type> Single<Type> restoreOrDefault(@NonNull String key, @NonNull Type defaultValue) {
        return restoreIfAvailable(key, (Class<Type>) defaultValue.getClass())
                .defaultIfEmpty(defaultValue);
    }

    @Override
    public <Type> Maybe<Type> restoreIfAvailable(@NonNull String key, @NonNull Class<Type> typeClass) {
        return restoreIfAvailable(key)
                .flatMapSingle(serializedValue -> serializer.deserializeFromString(serializedValue, typeClass))
                .onErrorResumeNext(throwable -> Maybe.error(new PreferenceProviderException("Unable to restore preference for key: " + key, throwable)));
    }

    protected abstract Maybe<String> restoreIfAvailable(@NonNull String key);

    @Override
    public <Type> Observable<Type> restoreIfAvailableAndGetChanges(@NonNull String key, @NonNull Class<Type> typeClass) {
        return restoreIfAvailable(key, typeClass)
                .toObservable()
                .mergeWith(getChanges(key, typeClass))
                .distinctUntilChanged();
    }

    @Override
    public <Type> Completable persist(@NonNull String key, @NonNull Type value) {
        return Single.defer(() -> serializer.serializeToString(value))
                .flatMapCompletable(serializedValue -> persist(key, serializedValue))
                .onErrorResumeNext(throwable -> Completable.error(new PreferenceProviderException("Unable to persist preference for key: " + key, throwable)))
                .andThen(processPreferenceChange(key, value));
    }

    public abstract Completable persist(@NonNull String key, String value);

    @Override
    public <Type> Completable persistIfNotYetAvailable(@NonNull String key, @NonNull Type value) {
        return containsKey(key)
                .flatMapCompletable(containsKey -> {
                    if (containsKey) {
                        return Completable.complete();
                    } else {
                        return persist(key, value);
                    }
                });
    }

    @Override
    public <Type> Observable<Type> getChanges(@NonNull String key, @NonNull Class<Type> typeClass) {
        return getOrCreateChangePublishSubject(key)
                .map(publishSubject -> (Observable<Type>) publishSubject)
                .flatMapObservable(publishSubject -> publishSubject);
    }

    protected <Type> Completable processPreferenceChange(@NonNull String key, @Nullable Type value) {
        return Completable.defer(() -> {
            if (value == null) {
                // preference has been deleted
                return Completable.complete();
            } else {
                // preference has been persisted
                return notifyChangePublishSubjectIfAvailable(key, value);
            }
        }).onErrorResumeNext(throwable -> Completable.error(new PreferenceProviderException("Unable to process preference change for key: " + key, throwable)));
    }

    protected <Type> Completable notifyChangePublishSubjectIfAvailable(@NonNull String key, @NonNull Type value) {
        return Completable.fromAction(() -> {
            PublishSubject<Type> publishSubject;
            synchronized (changePublishers) {
                publishSubject = changePublishers.get(key);
            }
            if (publishSubject != null) {
                publishSubject.onNext(value);
            }
        });
    }

    @SuppressWarnings("rawtypes")
    protected Single<PublishSubject> getOrCreateChangePublishSubject(@NonNull String key) {
        return Single.fromCallable(() -> {
            PublishSubject publishSubject;
            synchronized (changePublishers) {
                if (!changePublishers.containsKey(key)) {
                    publishSubject = PublishSubject.create();
                    changePublishers.put(key, publishSubject);
                } else {
                    publishSubject = changePublishers.get(key);
                }
            }
            return publishSubject;
        });
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public void setSerializer(@NonNull Serializer serializer) {
        this.serializer = serializer;
    }

}
