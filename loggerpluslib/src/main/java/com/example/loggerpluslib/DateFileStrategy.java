package com.example.loggerpluslib;

import android.os.Environment;


import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateFileStrategy implements FileStrategy {

    private final String PATTERN = "yyyy-MM-dd";
    private final String SUFFIX =  ".csv";

    private String folderPath;

    public DateFileStrategy(String folderPath) {
        if(folderPath == null){
            folderPath = generateDefaultPath();
        }
        this.folderPath = folderPath;
        clearOldFiles();
    }

    @Override
    public void clearOldFiles() {
        File directory = new File(folderPath);

        if (!directory.exists()) {
            Logger.W(directory + " does not exist");
            return;
        }

        if (!directory.isDirectory()) {
            Logger.W(directory + " is not a directory");
            return;
        }

        File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            return ;
        }

        for (File file : files) {
            String dateStr = file.getName().replace(SUFFIX, "");
            Date date = null;
            try {
                date = stringToDate(dateStr);
            } catch (ParseException e) {
                file.delete();//文件格式不对则删除
                e.printStackTrace();
            }
            if(!isLatestWeek(date)){
                file.delete();//超出七天的日志文件删除
            }
        }

    }

    @Override
    public String generateDefaultPath() {
        String diskPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        return diskPath + File.separatorChar + "logger";
    }

    @Override
    public File getCurrentFile() {
        Utils.checkNotNull(folderPath);
        //
        File folder = new File(folderPath);
        if (!folder.exists()) {
            //TODO: What if folder is not created, what happens then?
            folder.mkdirs();
        }
        //
        File currentFile = new File(folder, dateToString() + SUFFIX);
        return currentFile;
    }

    public String dateToString() {
        Date now = new Date();
        String date = new SimpleDateFormat(PATTERN, Locale.getDefault()).format(now);
        return date;
    }

    public Date stringToDate(String str) throws ParseException {
        DateFormat format = new SimpleDateFormat(PATTERN);
        Date date = null;
        date = format.parse(str);
        return date;
    }

    //判断某个时间是否是在当前时间的七天之内
    public boolean isLatestWeek(Date date){
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();  //得到日历
        calendar.setTime(now);//把当前时间赋给日历
        calendar.add(Calendar.DAY_OF_MONTH, -7);  //设置为7天前
        Date before7days = calendar.getTime();   //得到7天前的时间
        if(before7days.getTime() < date.getTime()){
            return true;
        }else{
            return false;
        }
    }


}
