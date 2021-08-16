[![Travis](https://img.shields.io/travis/neXenio/RxPreferences/master.svg)](https://travis-ci.org/neXenio/RxPreferences/builds) [![GitHub release](https://img.shields.io/github/release/neXenio/RxPreferences.svg)](https://github.com/neXenio/RxPreferences/releases) [![JitPack](https://img.shields.io/jitpack/v/neXenio/RxPreferences.svg)](https://jitpack.io/#neXenio/RxPreferences/) [![Codecov](https://img.shields.io/codecov/c/github/nexenio/RxPreferences.svg)](https://codecov.io/gh/neXenio/RxPreferences) [![license](https://img.shields.io/github/license/neXenio/RxPreferences.svg)](https://github.com/neXenio/RxPreferences/blob/master/LICENSE)

# RxPreferences

This library provides an [RxJava][rxjava] interface for working with preferences (key-value pairs), as well as some commonly used implementations.

## Usage

### Integration

You can get the latest artifacts from [JitPack][jitpack]:

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.neXenio:RxPreferences:1.2.0'
}
```

### `PreferencesProvider`

The [PreferencesProvider][preferencesprovider] interface allows you to persist, restore or delete key-value pairs of any type. There are different implementations available:

- `SharedPreferencesProvider` uses [SharedPreferences][sharedpreferences]. It's what you'd normally use in simple apps.
- `EncryptedSharedPreferencesProvider` uses [EncryptedSharedPreferences][encryptedsharedpreferences]. Wraps the `SharedPreferences` class and automatically encrypts keys and values
- `InMemoryPreferencesProvider` uses a simple `HashMap`. It's very fast but data is not actually persisted to disk. Useful for testing purposes.

The most important methods are:

- `Completable persist(String key, Type value)`
- `Single<Type> restore(String key, Class<Type> typeClass)`
- `Observable<Type> getChanges(String key, Class<Type> typeClass)`
- `Completable delete(String key)`

There are also some convenience methods available, they are documented [here][preferencesprovider].

### `Serializer`

The [Serializer][serializer] interface is used by a `PreferencesProvider` to serialize the values that you want to persist, and to deserialize the values that you want to restore. All `PreferencesProvider` implementations use a simple `GsonSerializer` by default. Setting a custom serializer is not required, but might be useful if you want use custom type adapters:

```java
// create a Gson instance with some custom stuff
Gson gson = new GsonBuilder()
        .excludeFieldsWithoutExposeAnnotation()
        .registerTypeAdapter(Bitmap.class, new BitmapTypeAdapter())
        .create();

// create a preferences provider instance and set the Gson serializer
TrayPreferencesProvider trayPreferencesProvider = new TrayPreferencesProvider(context);
trayPreferencesProvider.setSerializer(new GsonSerializer(gson));
```

[releases]: https://github.com/neXenio/RxPreferences/releases
[jitpack]: https://jitpack.io/#neXenio/RxPreferences/
[rxjava]: https://github.com/ReactiveX/RxJava
[tray]: https://github.com/grandcentrix/tray
[gson]: https://github.com/google/gson
[sharedpreferences]: https://developer.android.com/training/data-storage/shared-preferences
[encryptedsharedpreferences]: https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences
[preferencesprovider]: rxpreferences/src/main/java/com/nexenio/rxpreferences/provider/PreferencesProvider.java
[serializer]: rxpreferences/src/main/java/com/nexenio/rxpreferences/serializer/Serializer.java
