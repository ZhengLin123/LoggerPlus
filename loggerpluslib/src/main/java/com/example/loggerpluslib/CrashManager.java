/**
 * @2018 UBTECH Robotics Corp. All rights reserved (C)
 **/
package com.example.loggerpluslib;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * <p><strong>主要用来拦截APP UI线程以及后台线程未经捕捉的Exception导致的崩溃，
 * *          并且拦截之后程序依然存活。需要放在Application里初始化。</strong></p>
 *
 * @Author qinicy
 * @Date 2018/12/6
 **/
public class CrashManager {
    public static final String TAG = "UBTCrashManager";
    private static CrashManager mInstance;
    private boolean isStart;
    private boolean isInited;
    private Thread.UncaughtExceptionHandler mSystemUncaughtHandler;

    private ArrayList<String> mExceptionFilter;

    private boolean mEnable = true;

    private CrashManager() {
    }

    private static class SingletonHolder {
        private final static CrashManager instance = new CrashManager();
    }

    public static CrashManager getInstance() {
        return CrashManager.SingletonHolder.instance;
    }


    public CrashManager init(Context pContext) {
        if (isInited)
            return this;
        mExceptionFilter = new ArrayList<>();
        mSystemUncaughtHandler = new SystemUncaughtHandler(Thread.getDefaultUncaughtExceptionHandler());
        // the following handler is used to catch exceptions thrown in background threads
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtHandler(new Handler()));
        isInited = true;
        return this;
    }


    public CrashManager addExceptionFilterTag(String pTag) {
        if (mExceptionFilter == null)
            mExceptionFilter = new ArrayList<>();
        mExceptionFilter.add(pTag);
        return this;
    }

    public CrashManager addExceptionFilterTag(ArrayList<String> pFilterTagList) {
        if (mExceptionFilter == null)
            mExceptionFilter = new ArrayList<>();
        if (pFilterTagList == null)
            return this;
        mExceptionFilter.addAll(pFilterTagList);
        return this;
    }

    /**
     * 此方法的调用时机是:放在Appcalition的onCreate()里的所有初始化的代码之后,为什么呢?这方法内部有个死循环looper.loop().
     */
    public void startCatcher() {
        if (!isInited) {
            Logger.E("UBTCrashManager haven't be init yet!");
            return;
        }

        if (isStart) {
            return;
        }
        isStart = true;

        /*
        Q:为什么这里使用while(true)不会导致UI线程堵塞呢?
        A:因为所有的界面刷新其实都是通过looper去一个个通知执行的.下面的代码:
        进入到while内部后,就会在Looper.loop()死循环,只有在catch到exception的时候,
        才会跳出while内部重新执行第二遍while(true).而我们的UI刷新都是在Looper.loop()里完成,所以不会堵塞UI线程.
        */

        while (mEnable) {
            try {
                Looper.loop();
                Thread.setDefaultUncaughtExceptionHandler(mSystemUncaughtHandler);
                throw new RuntimeException("UBTCrashManager:Main thread loop unexpectedly exited!");
            } catch (BackgroundException e) {
                Logger.E( "BackgroundException:" + e.getMessage());
                handleUnCatchException(e.getCause(), false);
            } catch (Throwable e) {
                Logger.E( "Throwable:" + e.getMessage());
                handleUnCatchException(e, true);

            }
        }
    }

    private void throwToSystemHandle(Throwable e) {
        mSystemUncaughtHandler.uncaughtException(Thread.currentThread(), e);
    }

    private void handleUnCatchException(Throwable e, boolean isUiThread) {
        if (mExceptionFilter == null)
            return;

        String eStr = e2Str(e);
        if (eStr == null)
            return;
        Logger.E( "UBTCrashManager catch an exception:\n" + eStr);
        Iterator<String> iterator = mExceptionFilter.iterator();
        boolean filterSuccess = false;
        while (iterator.hasNext()) {
            String filterTag = iterator.next();
//            LOG.e("Filter Tag:" + filterTag);
            if (filterTag == null)
                continue;
            filterSuccess = eStr.contains(filterTag);
            if (filterSuccess)
                break;
        }

        //所有的错误上报为自定义错误,所以会导致友盟重复上报.
        if (!filterSuccess) {
            throwToSystemHandle(e);
        }
    }


    private String e2Str(Throwable t) {

        if (t == null)
            return null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            t.printStackTrace(new PrintStream(baos));
            baos.close();
        } catch (IOException pE) {
            pE.printStackTrace();
        }
        return baos.toString();
    }

    private class SystemUncaughtHandler implements Thread.UncaughtExceptionHandler {

        private Thread.UncaughtExceptionHandler mSourceSystemUncaughtExceptionHandler;

        SystemUncaughtHandler(Thread.UncaughtExceptionHandler sourceSystemUncaughtExceptionHandler) {
            mSourceSystemUncaughtExceptionHandler = sourceSystemUncaughtExceptionHandler;
        }

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            mSourceSystemUncaughtExceptionHandler.uncaughtException(t, e);
        }
    }

    /**
     * This handler catches exceptions in the background threads and propagates them to the UI thread
     */
    static class UncaughtHandler implements Thread.UncaughtExceptionHandler {

        private final Handler mHandler;

        UncaughtHandler(Handler handler) {
            mHandler = handler;
        }

        public void uncaughtException(Thread thread, final Throwable e) {
            Logger.E( "Caught the exception in the background " + thread + " propagating it to the UI thread, e:" + e);
            final int tid = Process.myTid();
            final Thread t = thread;
            mHandler.post(new Runnable() {
                public void run() {
                    throw new BackgroundException(e, tid, t);
                }
            });
        }
    }

    /**
     * Wrapper class for exceptions caught in the background
     */
    static class BackgroundException extends RuntimeException {

        final int tid;
        final Thread mThread;

        /**
         * @param e       original exception
         * @param tid     id of the thread where exception occurred
         * @param pThread thread of the throwable where exception occurred
         */
        BackgroundException(Throwable e, int tid, Thread pThread) {
            super(e);
            this.tid = tid;
            this.mThread = pThread;
        }
    }

}
