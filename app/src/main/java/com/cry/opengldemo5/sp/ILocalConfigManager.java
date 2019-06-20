package com.cry.opengldemo5.sp;

/**
 * Created by lingyunxiao on 2018-05-01
 */
public interface ILocalConfigManager {
    boolean hasKey(String key);

    long getLongValue(String key, long defValue);

    boolean getBooleanValue(String key, boolean defValue);

    int getIntValue(String key, int defValue);

    float getFloatValue(String key, float defValue);

    String getStringValue(String key, String defValue);

    void setBooleanValue(String key, boolean value);

    void setLongValue(String key, long value);

    void setIntValue(String key, int value);

    void setFloatValue(String key, float value);

    void setStringValue(String key, String value);
}
