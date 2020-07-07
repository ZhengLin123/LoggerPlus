package com.example.loggerpluslib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A proxy interface to enable additional operations.
 * Contains all possible Log message usages.
 */
public interface Printer {

  Printer t(@Nullable String tag);

  void addAdapter(@NonNull LogAdapter adapter);

  void clearLogAdapters();


  //兼容自带tag的
  void v(@NonNull String tag, @Nullable String msg);

  void d(@NonNull String tag, @Nullable String msg);

  void i(@NonNull String tag, @Nullable String msg);

  void w(@NonNull String tag, @Nullable String msg);

  void e(@NonNull String tag, @Nullable String msg);


  //
  void v(@NonNull String message, @Nullable Object... args);

  void d(@NonNull String message, @Nullable Object... args);

  void i(@NonNull String message, @Nullable Object... args);

  void w(@NonNull String message, @Nullable Object... args);

  void e(@NonNull String message, @Nullable Object... args);

  //
  void d(@Nullable Object object);

  void e(@Nullable Throwable throwable, @NonNull String message, @Nullable Object... args);

  void wtf(@NonNull String message, @Nullable Object... args);

  /**
   * Formats the given json content and print it
   */
  void json(@Nullable String json);

  /**
   * Formats the given xml content and print it
   */
  void xml(@Nullable String xml);

  void log(int priority, @Nullable String tag, @Nullable String message, @Nullable Throwable throwable);

}
