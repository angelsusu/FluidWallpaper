package com.cry.opengldemo5.sp;

import com.cry.opengldemo5.view.MainApplication;
import com.tencent.mmkv.MMKV;

import static com.tencent.mmkv.MMKV.MULTI_PROCESS_MODE;

public class LocalConfigManager implements ILocalConfigManager {

    private MMKV mKV;

    private LocalConfigManager() {
        MMKV.initialize(MainApplication.INSTANCE);
        mKV = MMKV.defaultMMKV(MULTI_PROCESS_MODE, null);
    }

    private static class SingletonHolder {
        public static final LocalConfigManager sInstance = new LocalConfigManager();
    }

    public static ILocalConfigManager getInstance() {
        return SingletonHolder.sInstance;
    }

    @Override
    public boolean hasKey(String key) {
        return mKV.containsKey(key);
    }

    @Override
    public long getLongValue(String key, long defValue) {
        return mKV.decodeLong(key, defValue);
    }

    @Override
    public boolean getBooleanValue(String key, boolean defValue) {
        return mKV.decodeBool(key, defValue);
    }

    @Override
    public int getIntValue(String key, int defValue) {
        return mKV.decodeInt(key, defValue);
    }

    @Override
    public float getFloatValue(String key, float defValue) {
        return mKV.decodeFloat(key, defValue);
    }

    @Override
    public String getStringValue(String key, String defValue) {
        return mKV.decodeString(key, defValue);
    }

    @Override
    public void setBooleanValue(String key, boolean value) {
        mKV.encode(key, value);
    }

    @Override
    public void setLongValue(String key, long value) {
        mKV.encode(key, value);
    }

    @Override
    public void setIntValue(String key, int value) {
        mKV.encode(key, value);
    }

    @Override
    public void setFloatValue(String key, float value) {
        mKV.encode(key, value);
    }

    @Override
    public void setStringValue(String key, String value) {
        mKV.encode(key, value);
    }
}
