package com.example.loggerplus;

import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.loggerpluslib.AndroidLogAdapter;
import com.example.loggerpluslib.CrashHandler;
import com.example.loggerpluslib.CsvFormatStrategy;
import com.example.loggerpluslib.DiskLogAdapter;
import com.example.loggerpluslib.DiskLogStrategy;
import com.example.loggerpluslib.FormatStrategy;
import com.example.loggerpluslib.Logger;
import com.example.loggerpluslib.PrettyFormatStrategy;

import java.io.File;


/**
 * @Deseription
 * @Author linzheng
 * @Time 2020/6/8 11:30
 */
public class LogUtil {

    private static String logDiskPath;

    public static void init(Context context){

        Logger.layerNum = 1;

        //打日志到Logcat
        FormatStrategy prettyFormat = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
                .methodCount(0)         // (Optional) How many method line to show. Default 2
                .methodOffset(5)        // (Optional) Hides internal method calls up to offset. Default 5
//                .logStrategy(null) // (Optional) Changes the log strategy to print out. Default LogCat
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(prettyFormat));

        //打日志到文件
        FormatStrategy csvFormat = CsvFormatStrategy.newBuilder()
                .logStrategy(new DiskLogStrategy(getLogDiskPath(context)) ) // (Optional) Changes the log strategy to print out. Default LogCat
                .build();
        Logger.addLogAdapter(new DiskLogAdapter(csvFormat));

        LogUtil.D("Logger日志初始化完成，日志文件路径:----%s", logDiskPath);



        CrashHandler.getInstance().init();
    }



    private static String getLogDiskPath(Context context) {
        if(logDiskPath == null || logDiskPath.equals("") || logDiskPath.isEmpty()){
            initDiskPath(context);
        }
        return logDiskPath;
    }

    private static void initDiskPath(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            logDiskPath = context.getExternalCacheDir().getPath() + File.separator + "logger";
        } else {
            logDiskPath = context.getCacheDir().getPath()         + File.separator + "logger";
        }
    }


    public static File getLogDiskDir() {
        return new File(logDiskPath);
    }

    public static String getLogDiskPath() {
        return logDiskPath;
    }

    /**
     *
     */
    public static void v(String msg) {
        Logger.V(msg);
    }

    public static void d(String msg) {
        Logger.D(msg);
    }

    public static void i(String msg) {
        Logger.I(msg);
    }

    public static void w(String msg) {
        Logger.W(msg);
    }

    public static void e(String msg) {
        Logger.E(msg);
    }

    /**
     *
     */
    public static void v(String tag, String msg) {
        Logger.v(tag, msg);
    }

    public static void d(String tag, String msg) {
        Logger.d(tag, msg);
    }

    public static void i(String tag, String msg) {
        Logger.i(tag, msg);
    }

    public static void w(String tag, String msg) {
        Logger.w(tag, msg);
    }

    public static void e(String tag, String msg) {
        Logger.e(tag, msg);
    }

    /**
     *
     */
    public static void V(@NonNull String message, @Nullable Object... args) {
        Logger.V(message, args);
    }
    public static void D(@NonNull String message, @Nullable Object... args) {
        Logger.D(message, args);
    }
    public static void I(@NonNull String message, @Nullable Object... args) {
        Logger.I(message, args);
    }
    public static void W(@NonNull String message, @Nullable Object... args) {
        Logger.W(message, args);
    }
    public static void E(@NonNull String message, @Nullable Object... args) {
        Logger.E(message, args);
    }


    /**
     *
     */
    public static void json(@Nullable String jsonStr) {
        Logger.json(jsonStr);
    }

    public static void xml(@Nullable String xml) {
        Logger.xml(xml);
    }

}
