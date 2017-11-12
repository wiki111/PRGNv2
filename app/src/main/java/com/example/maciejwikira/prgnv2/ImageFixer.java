package com.example.maciejwikira.prgnv2;

import android.os.Process;
import android.os.StrictMode;

/**
 * Created by Maciej on 2017-11-12.
 */

public class ImageFixer implements Runnable {

    private Thread imageTask;

    @Override
    public void run(){
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        imageTask = Thread.currentThread();



    }


}
