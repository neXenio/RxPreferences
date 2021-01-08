package com.nexenio.rxpreferences.serializer;

import com.google.gson.Gson;

import androidx.annotation.NonNull;
import io.reactivex.rxjava3.core.Single;

public class GsonSerializer implements Serializer {

    private Gson gson;

    public GsonSerializer() {
        this(new Gson());
    }

    public GsonSerializer(@NonNull Gson gson) {
        this.gson = gson;
    }

    @Override
    public <Type> Single<String> serializeToString(@NonNull Type value) {
        return Single.fromCallable(() -> gson.toJson(value))
                .onErrorResumeNext(throwable -> Single.error(new SerializerException("Unable to serialize value", throwable)));
    }

    @Override
    public <Type> Single<Type> deserializeFromString(@NonNull String value, @NonNull Class<Type> valueClass) {
        return Single.fromCallable(() -> gson.fromJson(value, valueClass))
                .onErrorResumeNext(throwable -> Single.error(new SerializerException("Unable to deserialize value", throwable)));
    }

    public Gson getGson() {
        return gson;
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }

}
