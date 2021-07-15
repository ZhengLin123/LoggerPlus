package com.example.loggerpluslib;

//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


//import static com.orhanobut.logger.Utils.checkNotNull;/

/**
 * Draws borders around the given log message along with additional information such as :
 *
 * <ul>
 *   <li>Thread information</li>
 *   <li>Method stack trace</li>
 * </ul>
 *
 * <pre>
 *  ┌──────────────────────────
 *  │ Method stack history
 *  ├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄
 *  │ Thread information
 *  ├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄
 *  │ Log message
 *  └──────────────────────────
 * </pre>
 *
 * <h3>Customize</h3>
 * <pre><code>
 *   FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
 *       .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
 *       .methodCount(0)         // (Optional) How many method line to show. Default 2
 *       .methodOffset(7)        // (Optional) Hides internal method calls up to offset. Default 5
 *       .logStrategy(customLog) // (Optional) Changes the log strategy to print out. Default LogCat
 *       .tag("My custom tag")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
 *       .build();
 * </code></pre>
 */
public class PrettyFormatStrategy implements FormatStrategy {

  /**
   * Android's max limit for a log entry is ~4076 bytes,
   * so 4000 bytes is used as chunk size since default charset
   * is UTF-8
   */
  private static final int CHUNK_SIZE = 4000;


  /**
   * Drawing toolbox
   */
  private static final char TOP_LEFT_CORNER = '┌';
  private static final char BOTTOM_LEFT_CORNER = '└';
  private static final char MIDDLE_CORNER = '├';
  private static final char HORIZONTAL_LINE = '│';
  private static final String DOUBLE_DIVIDER = "────────────────────────────────────────────────────────";
  private static final String SINGLE_DIVIDER = "┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄";
  private static final String TOP_BORDER = TOP_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
  private static final String BOTTOM_BORDER = BOTTOM_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
  private static final String MIDDLE_BORDER = MIDDLE_CORNER + SINGLE_DIVIDER + SINGLE_DIVIDER;

  private final int methodCount;
  private final int methodOffset;
  private final boolean showThreadInfo;
  @NonNull
  private final LogStrategy logStrategy;
  @Nullable
  private final String tag;

  private PrettyFormatStrategy(@NonNull Builder builder) {
    Utils.checkNotNull(builder);

    methodCount = builder.methodCount;
    methodOffset = builder.methodOffset;
    showThreadInfo = builder.showThreadInfo;
    logStrategy = builder.logStrategy;
    tag = builder.tag;
  }

  @NonNull
  public static Builder newBuilder() {
    return new Builder();
  }

  @Override
  public void log(int priority, @Nullable String onceOnlyTag, @NonNull String message) {
    Utils.checkNotNull(message);

//    String tag = formatTag(onceOnlyTag);
    String tag = onceOnlyTag;

    logTopBorder(priority, tag);
    logHeaderContent(priority, tag, methodCount);

    //get bytes of message with system's default charset (which is UTF-8 for Android)
    byte[] bytes = message.getBytes();
    int length = bytes.length;
    if (length <= CHUNK_SIZE) {
      if (methodCount > 0) {
        logDivider(priority, tag);
      }
      logContent(priority, tag, message);
      logBottomBorder(priority, tag);
      return;
    }
    if (methodCount > 0) {
      logDivider(priority, tag);
    }
    for (int i = 0; i < length; i += CHUNK_SIZE) {
      int count = Math.min(length - i, CHUNK_SIZE);
      //create a new String with system's default charset (which is UTF-8 for Android)
      logContent(priority, tag, new String(bytes, i, count));
    }
    logBottomBorder(priority, tag);
  }

  private void logTopBorder(int logType, @Nullable String tag) {
    logChunk(logType, tag, TOP_BORDER);
  }

  @SuppressWarnings("StringBufferReplaceableByString")
  private void logHeaderContent(int logType, @Nullable String tag, int methodCount) {
    //打印线程信息
    if (showThreadInfo) {
      logChunk(logType, tag, HORIZONTAL_LINE + " Thread: " + Thread.currentThread().getName());
      logDivider(logType, tag);
    }
    //打印方法栈
    if(methodCount > 0){
      printMethodStack(logType, tag);
    }
  }

  /**
   * 打印从stackDownIndex到stackUpIndex的方法栈
   */
  private void printMethodStack(int logType, String tag) {
    StackTraceElement[] trace = Thread.currentThread().getStackTrace();
    int stackUpIndex = Utils.getStackIndex(trace);
    int stackDownIndex = stackUpIndex + methodCount - 1;
    String level = "";
    for (int i = stackDownIndex; i >= stackUpIndex; i--) {
      if(i >= trace.length || i < 0){
        continue;
      }
      StackTraceElement element = trace[i];
      StringBuilder builder = new StringBuilder();
      builder.append(HORIZONTAL_LINE)
              .append(' ')
              .append(level)
              .append(getSimpleClassName(element.getClassName()))
              .append(".")
              .append(element.getMethodName())
              .append(" ")
              .append(" (")
              .append(element.getFileName())
              .append(":")
              .append(element.getLineNumber())
              .append(")");
      level += "   ";
      logChunk(logType, tag, builder.toString());
    }
  }

  private void logBottomBorder(int logType, @Nullable String tag) {
    logChunk(logType, tag, BOTTOM_BORDER);
  }

  private void logDivider(int logType, @Nullable String tag) {
    logChunk(logType, tag, MIDDLE_BORDER);
  }

  private void logContent(int logType, @Nullable String tag, @NonNull String chunk) {
    Utils.checkNotNull(chunk);

    String[] lines = chunk.split(System.getProperty("line.separator"));
    for (String line : lines) {
      logChunk(logType, tag, HORIZONTAL_LINE + " " + line);
    }
  }

  private void logChunk(int priority, @Nullable String tag, @NonNull String chunk) {
    Utils.checkNotNull(chunk);

    logStrategy.log(priority, tag, chunk);
  }

  private String getSimpleClassName(@NonNull String name) {
    Utils.checkNotNull(name);

    int lastIndex = name.lastIndexOf(".");
    return name.substring(lastIndex + 1);
  }

  @Nullable
  private String formatTag(@Nullable String tag) {
    if (!Utils.isEmpty(tag) && !Utils.equals(this.tag, tag)) {
      return this.tag + "-" + tag;
    }
    return this.tag;
  }

  public static class Builder {
    int methodCount = 0;
    int methodOffset = 0;
    boolean showThreadInfo = true;
    @Nullable
    LogStrategy logStrategy;
    @Nullable
    String tag = "PRETTY_LOGGER";

    private Builder() {
    }

    @NonNull
    public Builder methodCount(int val) {
      methodCount = val;
      return this;
    }

    @NonNull
    public Builder methodOffset(int val) {
      methodOffset = val;
      return this;
    }

    @NonNull
    public Builder showThreadInfo(boolean val) {
      showThreadInfo = val;
      return this;
    }

    @NonNull
    public Builder logStrategy(@Nullable LogStrategy val) {
      logStrategy = val;
      return this;
    }

    @NonNull
    public Builder tag(@Nullable String tag) {
      this.tag = tag;
      return this;
    }

    @NonNull
    public PrettyFormatStrategy build() {
      if (logStrategy == null) {
        logStrategy = new LogcatLogStrategy();
      }
      return new PrettyFormatStrategy(this);
    }
  }

}
