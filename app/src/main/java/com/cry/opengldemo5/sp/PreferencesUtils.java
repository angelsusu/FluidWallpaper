package com.cry.opengldemo5.sp;

public final class PreferencesUtils {

    private PreferencesUtils() {
        throw new IllegalAccessError();
    }

    public static boolean getBoolean(String key, boolean defValue) {
        return LocalConfigManager.getInstance().getBooleanValue(key, defValue);
    }

    public static void putBoolean(String key, boolean state) {
        LocalConfigManager.getInstance().setBooleanValue(key, state);
    }

    public static String getString(String key, String defValue) {
        return LocalConfigManager.getInstance().getStringValue(key, defValue);
    }

    public static void putString(String key, String value) {
        LocalConfigManager.getInstance().setStringValue(key, value);
    }

    public static int getInt(String key, int defValue) {
        return LocalConfigManager.getInstance().getIntValue(key, defValue);
    }

    public static void putInt(String key, int value) {
        LocalConfigManager.getInstance().setIntValue(key, value);
    }

    public static void putLong(String key, long value) {
        LocalConfigManager.getInstance().setLongValue(key, value);
    }

    public static long getLong(String key, Long defValue) {
        return LocalConfigManager.getInstance().getLongValue(key, defValue);
    }

    public static float getFloat(String key, float defValue) {
        return LocalConfigManager.getInstance().getFloatValue(key, defValue);
    }

    public static void putFloat(String key, float value) {
        LocalConfigManager.getInstance().setFloatValue(key, value);
    }

    public static boolean contains(String key) {
        return LocalConfigManager.getInstance().hasKey(key);
    }
}
