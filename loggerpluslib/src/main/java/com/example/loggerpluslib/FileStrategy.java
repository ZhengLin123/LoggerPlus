package com.example.loggerpluslib;

import java.io.File;

public interface FileStrategy {

    //清理旧的过期了的Log文件
    void clearOldFiles();

    String generateDefaultPath();

    File getCurrentFile();
}
