package com.example.loggerpluslib;

//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

//import static com.orhanobut.logger.Logger.ASSERT;
//import static com.orhanobut.logger.Logger.DEBUG;
//import static com.orhanobut.logger.Logger.ERROR;
//import static com.orhanobut.logger.Logger.INFO;
//import static com.orhanobut.logger.Logger.VERBOSE;
//import static com.orhanobut.logger.Logger.WARN;
//import static com.orhanobut.logger.Utils.checkNotNull;


class LoggerPrinter implements Printer {

  /**
   * It is used for json pretty print
   */
  private static final int JSON_INDENT = 2;

  /**
   * Provides one-time used tag for the log message
   */
  private final ThreadLocal<String> localTag = new ThreadLocal<>();

  private final List<LogAdapter> logAdapters = new ArrayList<>();

  @Override
  public Printer t(String tag) {
    if (tag != null) {
      localTag.set(tag);
    }
    return this;
  }

  @Override
  public void addAdapter(@NonNull LogAdapter adapter) {
    logAdapters.add(Utils.checkNotNull(adapter));
  }

  @Override
  public void clearLogAdapters() {
    logAdapters.clear();
  }


  /**
   * 自定义Tag + Log信息
   */
  @Override
  public void v(@NonNull String customTag, @Nullable String message) {
    log(Logger.VERBOSE, customTag, null, message);
  }
  @Override
  public void d(@NonNull String customTag, @Nullable String message) {
    log(Logger.DEBUG, customTag, null, message);
  }
  @Override
  public void i(@NonNull String customTag, @Nullable String message) {
    log(Logger.INFO, customTag, null, message);
  }
  @Override
  public void w(@NonNull String customTag, @Nullable String message) {
    log(Logger.WARN, customTag, null, message);
  }
  @Override
  public void e(@NonNull String customTag, @Nullable String message) {
    log(Logger.ERROR, customTag, null, message);
  }


  /**
   * Log信息 + 参数
   */
  @Override
  public void v(@NonNull String message, @Nullable Object... args) {
    log(Logger.VERBOSE, null, null, message, args);
  }
  @Override
  public void d(@NonNull String message, @Nullable Object... args) {
    log(Logger.DEBUG, null,null, message, args);
  }
  @Override
  public void i(@NonNull String message, @Nullable Object... args) {
    log(Logger.INFO, null,null, message, args);
  }
  @Override
  public void w(@NonNull String message, @Nullable Object... args) {
    log(Logger.WARN,null, null, message, args);
  }
  @Override
  public void e(@Nullable Throwable throwable, @NonNull String message, @Nullable Object... args) {
    log(Logger.ERROR, null, throwable, message, args);
  }


  @Override
  public void d(@Nullable Object object) {
    log(Logger.DEBUG, null,null, Utils.toString(object));
  }

  @Override
  public void e(@NonNull String message, @Nullable Object... args) {
    e(null, message, args);
  }

  @Override
  public void wtf(@NonNull String message, @Nullable Object... args) {
    log(Logger.ASSERT, null, null, message, args);
  }

  @Override
  public void json(@Nullable String json) {
    if (Utils.isEmpty(json)) {
      d("Empty/Null json content");
      return;
    }
    try {
      json = json.trim();
      if (json.startsWith("{")) {
        JSONObject jsonObject = new JSONObject(json);
        String message = jsonObject.toString(JSON_INDENT);
        d(message);
        return;
      }
      if (json.startsWith("[")) {
        JSONArray jsonArray = new JSONArray(json);
        String message = jsonArray.toString(JSON_INDENT);
        d(message);
        return;
      }
      e("Invalid Json");
    } catch (JSONException e) {
      e("Invalid Json");
    }
  }

  @Override
  public void xml(@Nullable String xml) {
    if (Utils.isEmpty(xml)) {
      d("Empty/Null xml content");
      return;
    }
    try {
      Source xmlInput = new StreamSource(new StringReader(xml));
      StreamResult xmlOutput = new StreamResult(new StringWriter());
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      transformer.transform(xmlInput, xmlOutput);
      d(xmlOutput.getWriter().toString().replaceFirst(">", ">\n"));
    } catch (TransformerException e) {
      e("Invalid xml");
    }
  }

  /**
   * This method is synchronized in order to avoid messy of logs' order.
   */
  private synchronized void log(int priority,
                                @Nullable String customTag,
                                @Nullable Throwable throwable,
                                @NonNull String msg,
                                @Nullable Object... args) {
    //
    String tag = generateTag(customTag);
    //
    String message = createMessage(msg, args);
    //
    log(priority, tag, message, throwable);
  }

  private String generateTag(String customTag) {
    //
    if( !TextUtils.isEmpty(customTag) ){
      return "[" + customTag + "]";
    }
    //
    StackTraceElement stackTraceElement = Utils.getStackTraceElement();
    return generateClassInfo(stackTraceElement) + generateMethodInfo(stackTraceElement);
  }

  private String generateClassInfo(StackTraceElement stackTraceElement) {
    if (stackTraceElement == null)
      return "UBT";
    String className = stackTraceElement.getClassName();
    if (className != null) {
      int index = className.lastIndexOf(".");
      if (className.length() - 1 >= index + 1) {
        return className.substring(index + 1);
      } else {
        return "UBT";
      }
    }
    return "UBT";
  }

  private static String generateMethodInfo(StackTraceElement stackTraceElement) {
    if (stackTraceElement == null)
      return "";
    int line = stackTraceElement.getLineNumber();
    String method = stackTraceElement.getMethodName();
    return "[" + method + "]" + "[" + line + "]";
  }

  /**
   * @return the appropriate tag based on local or global
   */
  @Nullable
  private String getTag() {
    String tag = localTag.get();
    if (tag != null) {
      localTag.remove();
      return tag;
    }
    return null;
  }

  @NonNull
  private String createMessage(@NonNull String message, @Nullable Object... args) {
    return args == null || args.length == 0 ? message : String.format(message, args);
  }

  @Override
  public synchronized void log(int priority,
                               @Nullable String tag,
                               @Nullable String message,
                               @Nullable Throwable throwable) {

    if(message == null){
      message = Utils.getStackTraceString(throwable);
    }else if(throwable != null){
      message = message + " : " + Utils.getStackTraceString(throwable);
    }
    if (Utils.isEmpty(message)) {
      message = "Empty/NULL log message";
    }

    for (LogAdapter adapter : logAdapters) {
      if (adapter.isLoggable(priority, tag)) {
        adapter.log(priority, tag, message);
      }
    }
  }
}
