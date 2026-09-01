package com.comismar.informes.view.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSettings {

    private static final String PREFS_NAME = "comismar_settings";
    private static final String KEY_RECIPIENT_EMAIL = "recipient_email";
    private static final String KEY_NAVAL_EMAIL = "naval_email";
    private static final String KEY_CARGO_EMAIL = "cargo_email";
    private static final String KEY_AUTO_SEND_EMAIL = "auto_send_email";
    private static final String KEY_REMEMBER_LOGIN = "remember_login";
    private static final String DEFAULT_RECIPIENT_EMAIL = "geninfor.comismar@gmail.com";
    private static final String DEFAULT_NAVAL_EMAIL = "cascos@comismar.es";
    private static final String DEFAULT_CARGO_EMAIL = "nacional@comismar.es";

    public static String getRecipientEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_RECIPIENT_EMAIL, DEFAULT_RECIPIENT_EMAIL);
    }

    public static void setRecipientEmail(Context context, String email) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_RECIPIENT_EMAIL, email).apply();
    }

    public static String getNavalEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_NAVAL_EMAIL, DEFAULT_NAVAL_EMAIL);
    }

    public static void setNavalEmail(Context context, String email) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_NAVAL_EMAIL, email).apply();
    }

    public static String getCargoEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_CARGO_EMAIL, DEFAULT_CARGO_EMAIL);
    }

    public static void setCargoEmail(Context context, String email) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_CARGO_EMAIL, email).apply();
    }

    public static boolean isAutoSendEmailEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_AUTO_SEND_EMAIL, true);
    }

    public static void setAutoSendEmailEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_AUTO_SEND_EMAIL, enabled).apply();
    }

    public static boolean isRememberLoginEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_REMEMBER_LOGIN, false);
    }

    public static void setRememberLoginEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_REMEMBER_LOGIN, enabled).apply();
    }
}
