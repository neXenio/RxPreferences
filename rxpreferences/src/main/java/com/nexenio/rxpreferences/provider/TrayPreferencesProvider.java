package com.nexenio.rxpreferences.provider;

import android.content.Context;

import net.grandcentrix.tray.TrayPreferences;
import net.grandcentrix.tray.core.TrayItem;

import androidx.annotation.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class TrayPreferencesProvider extends BasePreferencesProvider {

    @NonNull
    private TrayPreferences trayPreferences;

    public TrayPreferencesProvider(@NonNull Context context) {
        this(new TrayPreferences(context, context.getPackageName(), 1));
    }

    public TrayPreferencesProvider(@NonNull TrayPreferences trayPreferences) {
        this.trayPreferences = trayPreferences;
    }

    @Override
    public Observable<String> getKeys() {
        return Observable.defer(() -> Observable.fromIterable(trayPreferences.getAll()))
                .map(TrayItem::key);
    }

    @Override
    public Single<Boolean> containsKey(@NonNull String key) {
        return Single.fromCallable(() -> trayPreferences.contains(key));
    }

    @Override
    protected Maybe<String> restoreIfAvailable(@NonNull String key) {
        return Maybe.fromCallable(() -> trayPreferences.getPref(key))
                .map(TrayItem::value);
    }

    @Override
    public Completable persist(@NonNull String key, @NonNull String value) {
        return Completable.fromAction(() -> trayPreferences.put(key, value));
    }

    @Override
    public Completable delete(@NonNull String key) {
        return Completable.fromAction(() -> trayPreferences.remove(key))
                .andThen(processPreferenceChange(key, null));
    }

    @Override
    public Completable deleteAll() {
        return Completable.fromAction(() -> {
            boolean success = trayPreferences.clear();
            if (!success) {
                throw new IllegalStateException("Unable to clear tray preferences");
            }
        }).andThen(getKeys().flatMapCompletable(key -> processPreferenceChange(key, null)));
    }

    public TrayPreferences getTrayPreferences() {
        return trayPreferences;
    }

    public void setTrayPreferences(@NonNull TrayPreferences trayPreferences) {
        this.trayPreferences = trayPreferences;
    }

}
