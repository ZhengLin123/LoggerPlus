package com.example.loggerpluslib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.Arrays;


/**
 * Provides convenient methods to some common operations
 */
final class Utils {

  /**
   * The minimum stack trace index, starts at this class after two native calls.
   */
  private static final int MIN_STACK_OFFSET = 2;//



  private Utils() {
    // Hidden constructor.
  }

  /**
   * Returns true if the string is null or 0-length.
   *
   * @param str the string to be examined
   * @return true if str is null or zero length
   */
  static boolean isEmpty(CharSequence str) {
    return str == null || str.length() == 0;
  }

  /**
   * Returns true if a and b are equal, including if they are both null.
   * <p><i>Note: In platform versions 1.1 and earlier, this method only worked well if
   * both the arguments were instances of String.</i></p>
   *
   * @param a first CharSequence to check
   * @param b second CharSequence to check
   * @return true if a and b are equal
   * <p>
   * NOTE: Logic slightly change due to strict policy on CI -
   * "Inner assignments should be avoided"
   */
  static boolean equals(CharSequence a, CharSequence b) {
    if (a == b) return true;
    if (a != null && b != null) {
      int length = a.length();
      if (length == b.length()) {
        if (a instanceof String && b instanceof String) {
          return a.equals(b);
        } else {
          for (int i = 0; i < length; i++) {
            if (a.charAt(i) != b.charAt(i)) return false;
          }
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Copied from "android.util.Log.getStackTraceString()" in order to avoid usage of Android stack
   * in unit tests.
   *
   * @return Stack trace in form of String
   */
  static String getStackTraceString(Throwable tr) {
    if (tr == null) {
      return "";
    }

    // This is to reduce the amount of log spew that apps do in the non-error
    // condition of the network being unavailable.
    Throwable t = tr;
    while (t != null) {
      if (t instanceof UnknownHostException) {
        return "";
      }
      t = t.getCause();
    }

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    tr.printStackTrace(pw);
    pw.flush();
    return sw.toString();
  }

  static String logLevel(int value) {
    switch (value) {
      case Logger.VERBOSE:
        return "V";
      case Logger.DEBUG:
        return "D";
      case Logger.INFO:
        return "I";
      case Logger.WARN:
        return "W";
      case Logger.ERROR:
        return "E";
      case Logger.ASSERT:
        return "ASSERT";
      default:
        return "UNKNOWN";
    }
  }

  public static String toString(Object object) {
    if (object == null) {
      return "null";
    }
    if (!object.getClass().isArray()) {
      return object.toString();
    }
    if (object instanceof boolean[]) {
      return Arrays.toString((boolean[]) object);
    }
    if (object instanceof byte[]) {
      return Arrays.toString((byte[]) object);
    }
    if (object instanceof char[]) {
      return Arrays.toString((char[]) object);
    }
    if (object instanceof short[]) {
      return Arrays.toString((short[]) object);
    }
    if (object instanceof int[]) {
      return Arrays.toString((int[]) object);
    }
    if (object instanceof long[]) {
      return Arrays.toString((long[]) object);
    }
    if (object instanceof float[]) {
      return Arrays.toString((float[]) object);
    }
    if (object instanceof double[]) {
      return Arrays.toString((double[]) object);
    }
    if (object instanceof Object[]) {
      return Arrays.deepToString((Object[]) object);
    }
    return "Couldn't find a correct type for the object";
  }

  @NonNull
  static <T> T checkNotNull(@Nullable final T obj) {
    if (obj == null) {
      throw new NullPointerException();
    }
    return obj;
  }


  /**
   * 获取外部调用打Log时的方法栈
   * @return
   */
  static StackTraceElement getStackTraceElement() {
    StackTraceElement[] trace = Thread.currentThread().getStackTrace();
    int index = getStackIndex(trace);
    //防止数组下标越界
    if(index < 0 || index >= trace.length){
      index = trace.length - 1;
    }
    return trace[index];
  }


  /**
   * 获取外部调用打Log时的方法栈Index
   * @param trace
   * @return
   */
  static int getStackIndex(StackTraceElement[] trace) {
    //
    Utils.checkNotNull(trace);
    int index = getIndexFrom(trace,MIN_STACK_OFFSET);
    return index;
  }


  /**
   * 从from开始遍历，已Logger为基准找Index
   */
  private static int getIndexFrom(StackTraceElement[] trace, int from) {
    for (int i = from; i < trace.length; i++) {
      StackTraceElement e = trace[i];
      String name = e.getClassName();
      if(name.equals(Logger.class.getName())){
        return i + Logger.layerNum + 1;
      }
    }
    return 0;
  }
}
