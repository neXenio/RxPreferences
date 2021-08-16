package com.nexenio.rxpreferences.provider;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class SharedPreferencesProvider extends BasePreferencesProvider {

    @NonNull
    protected SharedPreferences sharedPreferences;

    public SharedPreferencesProvider(@NonNull Context context) {
        this(context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE));
    }

    public SharedPreferencesProvider(@NonNull SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public Observable<String> getKeys() {
        return Observable.defer(() -> Observable.fromIterable(sharedPreferences.getAll().keySet()));
    }

    @Override
    public Single<Boolean> containsKey(@NonNull String key) {
        return Single.fromCallable(() -> sharedPreferences.contains(key));
    }

    @Override
    protected Maybe<String> restoreIfAvailable(@NonNull String key) {
        return Maybe.fromCallable(() -> sharedPreferences.getString(key, null));
    }

    @Override
    public Completable persist(@NonNull String key, @NonNull String value) {
        return Completable.fromAction(() -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, value);
            editor.commit();
        });
    }

    @Override
    public Completable delete(@NonNull String key) {
        return Completable.fromAction(() -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(key);
            editor.commit();
        }).andThen(processPreferenceChange(key, null));
    }

    @NonNull
    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public void setSharedPreferences(@NonNull SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

}
