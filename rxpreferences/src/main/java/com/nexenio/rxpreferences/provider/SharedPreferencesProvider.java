package com.nexenio.rxpreferences.provider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

@SuppressWarnings("SynchronizeOnNonFinalField")
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
        return Observable.defer(() -> {
            Set<String> keys;
            synchronized (sharedPreferences) {
                keys = new HashSet<>(sharedPreferences.getAll().keySet());
            }
            return Observable.fromIterable(keys);
        });
    }

    @Override
    public Single<Boolean> containsKey(@NonNull String key) {
        return Single.fromCallable(() -> {
            boolean containsKey;
            synchronized (sharedPreferences) {
                containsKey = sharedPreferences.contains(key);
            }
            return containsKey;
        });
    }

    @Override
    protected Maybe<String> restoreIfAvailable(@NonNull String key) {
        return Maybe.fromCallable(() -> {
            String value;
            synchronized (sharedPreferences) {
                value = sharedPreferences.getString(key, null);
            }
            return value;
        });
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public Completable persist(@NonNull String key, @NonNull String value) {
        return Completable.fromAction(() -> {
            synchronized (sharedPreferences) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(key, value);
                editor.commit();
            }
        });
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public Completable delete(@NonNull String key) {
        return Completable.fromAction(() -> {
            synchronized (sharedPreferences) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove(key);
                editor.commit();
            }
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
