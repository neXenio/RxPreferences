package com.nexenio.rxpreferences.serializer;

import androidx.annotation.NonNull;
import io.reactivex.rxjava3.core.Single;

public interface Serializer {

    <Type> Single<String> serializeToString(@NonNull Type value);

    <Type> Single<Type> deserializeFromString(@NonNull String value, @NonNull Class<Type> valueClass);

}
