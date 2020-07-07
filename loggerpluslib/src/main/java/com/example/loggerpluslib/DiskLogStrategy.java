package com.example.loggerpluslib;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import com.orhanobut.logger.LogStrategy;


/**
 * Abstract class that takes care of background threading the file log operation on Android.
 * implementing classes are free to directly perform I/O operations there.
 *
 * Writes all logs to the disk with CSV format.
 */
public class DiskLogStrategy implements LogStrategy {

  private FileStrategy fileStrategy;


  @NonNull
  private final Handler handler;

  public DiskLogStrategy() {
    this(null);
  }

  public DiskLogStrategy(String logDiskPath) {

    fileStrategy = new DateFileStrategy(logDiskPath);
    //
    HandlerThread ht = new HandlerThread("AndroidFileLogger");
    ht.start();
    handler = new DiskLogStrategy.WriteHandler(ht.getLooper(), fileStrategy);
  }

  @Override
  public void log(int level, @Nullable String tag, @NonNull String message) {
    Utils.checkNotNull(message);

    // do nothing on the calling thread, simply pass the tag/msg to the background thread
    handler.sendMessage(handler.obtainMessage(level, message));
  }

  static class WriteHandler extends Handler {

    @NonNull
    private final FileStrategy fileStrategy;

    WriteHandler(@NonNull Looper looper, @NonNull FileStrategy fileStrategy) {
      super(Utils.checkNotNull(looper));
      //
      this.fileStrategy = Utils.checkNotNull(fileStrategy);
    }

    @SuppressWarnings("checkstyle:emptyblock")
    @Override
    public void handleMessage(@NonNull Message msg) {
      String content = (String) msg.obj;

      FileWriter fileWriter = null;
      File logFile = fileStrategy.getCurrentFile();

      try {
        fileWriter = new FileWriter(logFile, true);

        writeLog(fileWriter, content);

        fileWriter.flush();
        fileWriter.close();
      } catch (IOException e) {
        if (fileWriter != null) {
          try {
            fileWriter.flush();
            fileWriter.close();
          } catch (IOException e1) { /* fail silently */ }
        }
      }
    }

    /**
     * This is always called on a single background thread.
     * Implementing classes must ONLY write to the fileWriter and nothing more.
     * The abstract class takes care of everything else including close the stream and catching IOException
     *
     * @param fileWriter an instance of FileWriter already initialised to the correct file
     */
    private void writeLog(@NonNull FileWriter fileWriter, @NonNull String content) throws IOException {
      Utils.checkNotNull(fileWriter);
      Utils.checkNotNull(content);

      fileWriter.append(content);
    }

  }
}
