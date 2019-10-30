package com.wolfgg.filterlibrary.utils;

import android.util.Log;


import com.wolfgg.filterlibrary.BuildConfig;

import java.io.PrintWriter;
import java.io.StringWriter;


public class LogHelper {

    public static boolean DEBUG = false;

    public static void d(String subTag, String msg) {
        if ((BuildConfig.DEBUG)) {
            Log.d(subTag, getLogMsg(subTag, msg));
        }
    }

    public static void i(String subTag, String msg) {
        if ((BuildConfig.DEBUG)) {
            Log.i(subTag, getLogMsg(subTag, msg));
        }
    }

    public static void w(String subTag, String msg) {
        if ((BuildConfig.DEBUG)) {
            Log.w(subTag, getLogMsg(subTag, msg));
        }
    }

    public static void w(String subTag, Throwable e) {
        if ((BuildConfig.DEBUG)) {
            Log.w(subTag, subTag, e);
        }
    }

    public static void w(String subTag, String msg, Throwable e) {
        if ((BuildConfig.DEBUG)) {
            Log.w(subTag, getLogMsg(subTag, msg), e);
        }
    }

    public static void e(String subTag, String msg) {
        if ((BuildConfig.DEBUG)) {
            Log.e(subTag, getLogMsg(subTag, msg));
        }
    }

    public static void e(String subTag, Throwable e) {
        if ((BuildConfig.DEBUG)) {
            Log.e(subTag, subTag, e);
        }
    }

    private static String getLogMsg(String subTag, String msg) {
        return "[" + subTag + "] " + msg;
    }

    public static void log(String subTag, String formmatMsg, Object... objects) {
        log(Log.DEBUG, subTag, formmatMsg, objects);
    }

    public static void log(int priority, String subTag, String formmatMsg, Object... objects) {
        if (BuildConfig.DEBUG) {
            Log.println(priority, subTag, String.format("[%s] %s", subTag, String.format(formmatMsg, objects)));
        }
    }

    public static void e(String subTag, String msg, Throwable e) {
        if ((BuildConfig.DEBUG)) {
            Log.e(subTag, getLogMsg(subTag, msg + " Exception: " + getExceptionMsg(e)));
        }
    }

    private static String getExceptionMsg(Throwable e) {
        StringWriter sw = new StringWriter(1024);
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }
}
