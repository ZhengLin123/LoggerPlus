package com.example.loggerpluslib;

//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
import android.util.Log;


/**
 * LogCat implementation for {@link LogStrategy}
 *
 * This simply prints out all logs to Logcat by using standard {@link Log} class.
 */
public class LogcatLogStrategy implements LogStrategy {

  static final String DEFAULT_TAG = "NO_TAG";

  @Override
  public void log(int priority, String tag, String message) {
    Utils.checkNotNull(message);

    if (tag == null) {
      tag = DEFAULT_TAG;
    }

    Log.println(priority, tag, message);
  }
}
