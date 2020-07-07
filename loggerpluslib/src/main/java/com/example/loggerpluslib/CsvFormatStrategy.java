package com.example.loggerpluslib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * CSV formatted file logging for Android.
 * Writes to CSV the following data:
 * epoch timestamp, ISO8601 timestamp (human-readable), log level, tag, log message.
 */
public class CsvFormatStrategy implements FormatStrategy {

  private static final String NEW_LINE = System.getProperty("line.separator");
  private static final String NEW_LINE_REPLACEMENT = " <br> ";
  private static final String SEPARATOR = ",";
  private static final String DOUBLE_QUOTES = "\"";

  @NonNull
  private final Date date;
  @NonNull
  private final SimpleDateFormat dateFormat;
  @NonNull
  private final LogStrategy logStrategy;
  @Nullable
  private final String tag;

  private CsvFormatStrategy(@NonNull Builder builder) {
    Utils.checkNotNull(builder);

    date = builder.date;
    dateFormat = builder.dateFormat;
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

    date.setTime(System.currentTimeMillis());

    StringBuilder builder = new StringBuilder();

    // machine-readable date/time
    builder.append(Long.toString(date.getTime()));

    // human-readable date/time
    builder.append(SEPARATOR);
    builder.append(dateFormat.format(date));

    // level
    builder.append(SEPARATOR);
    builder.append(Utils.logLevel(priority));

    // tag
    builder.append(SEPARATOR);
    builder.append(tag);

    // message
    builder.append(SEPARATOR);
//    if (message.contains(NEW_LINE)) {
//      // a new line would break the CSV format, so we replace it here
//      message = message.replaceAll(NEW_LINE, NEW_LINE_REPLACEMENT);
//    }
//    /**
//     * 方式一：为了能在Excel中显示正确格式，替换掉message中的英文逗号和双引号并在message两端加双引号
//     * 但会破坏原来数据，待优化
//     */
//    if (message.contains(SEPARATOR)) {
//      message = message.replaceAll(SEPARATOR, "，");
//    }
//    if (message.contains(DOUBLE_QUOTES)) {
//      message = message.replaceAll(DOUBLE_QUOTES, "“");
//    }
//    builder.append(DOUBLE_QUOTES + message + DOUBLE_QUOTES);
    /**
     * 方式二：仅在message两端加双引号，Excel打开格式会不正确，因此需要使用另外的CSV软件打开
     */
    builder.append(DOUBLE_QUOTES + message + DOUBLE_QUOTES);

    // new line
    builder.append(NEW_LINE);

    logStrategy.log(priority, tag, builder.toString());
  }

  @Nullable
  private String formatTag(@Nullable String tag) {
    if (!Utils.isEmpty(tag) && !Utils.equals(this.tag, tag)) {
      return this.tag + "-" + tag;
    }
    return this.tag;
  }

  public static final class Builder {

    Date date;
    SimpleDateFormat dateFormat;
    LogStrategy logStrategy;
    String tag = "PRETTY_LOGGER";

    private Builder() {
    }

    @NonNull
    public Builder date(@Nullable Date val) {
      date = val;
      return this;
    }

    @NonNull
    public Builder dateFormat(@Nullable SimpleDateFormat val) {
      dateFormat = val;
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
    public CsvFormatStrategy build() {
      if (date == null) {
        date = new Date();
      }
      if (dateFormat == null) {
        dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS", Locale.UK);
      }
      if (logStrategy == null) {
        logStrategy = new DiskLogStrategy();
      }
      return new CsvFormatStrategy(this);
    }
  }
}
