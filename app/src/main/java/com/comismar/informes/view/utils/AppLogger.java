package com.comismar.informes.view.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AppLogger {

    private static final String LOG_FILE_NAME = "app_logs.txt";

    public static void logError(Context context, String source, String message, Throwable throwable) {
        log(context, "ERROR", source, message, throwable);
    }

    public static void logInfo(Context context, String source, String message) {
        log(context, "INFO ", source, message, null);
    }

    public static void logSuccess(Context context, String source, String message) {
        log(context, "SUCCESS", source, message, null);
    }

    private static synchronized void log(Context context, String level, String source, String message, Throwable throwable) {
        if (context == null) return;
        File file = new File(context.getFilesDir(), LOG_FILE_NAME);
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        StringBuilder line = new StringBuilder();
        line.append(timestamp).append(" | ").append(level).append(" | ").append(source).append(" | ").append(message);
        if (throwable != null && throwable.getMessage() != null) {
            line.append(" | ex=").append(throwable.getMessage());
        }
        line.append("\n");
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(line.toString());
        } catch (Exception ignored) {
        }
    }

    public static synchronized String readLogs(Context context) {
        if (context == null) return "";
        File file = new File(context.getFilesDir(), LOG_FILE_NAME);
        if (!file.exists()) {
            return context.getString(com.comismar.informes.R.string.no_logs);
        }
        StringBuilder out = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line).append('\n');
            }
        } catch (Exception e) {
            return context.getString(com.comismar.informes.R.string.error_reading_logs, e.getMessage());
        }
        return out.length() == 0 ? context.getString(com.comismar.informes.R.string.no_logs) : out.toString();
    }

    public static synchronized void clearLogs(Context context) {
        if (context == null) return;
        File file = new File(context.getFilesDir(), LOG_FILE_NAME);
        if (file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }
}
