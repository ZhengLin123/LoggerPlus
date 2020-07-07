package com.example.loggerpluslib;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * Provides a common interface to emits logs through. This is a required contract for Logger.
 *
 * @see AndroidLogAdapter
 * @see DiskLogAdapter
 */
public abstract class LogAdapter {
  
//  abstract void convertPriorityAndLog(int priority, @Nullable String tag, @NonNull String message);

  /**
   * Each log will use this pipeline
   *
   * @param priority is the log level e.g. DEBUG, WARNING
   * @param tag is the given tag for the log message.
   * @param message is the given message for the log message.
   */
  abstract void log(int priority, @Nullable String tag, @NonNull String message);


  /**
   * Used to determine whether log should be printed out or not.
   *
   * @param priority is the log level e.g. DEBUG, WARNING
   * @param tag is the given tag for the log message
   *
   * @return is used to determine if log should printed.
   *         If it is true, it will be printed, otherwise it'll be ignored.
   */
  abstract boolean isLoggable(int priority, @Nullable String tag);



}