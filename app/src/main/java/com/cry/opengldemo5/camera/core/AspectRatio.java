package com.cry.opengldemo5.camera.core;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;

/**
 * DESCRIPTION:高宽比.用来筛选对应比例的相机尺寸
 * Author: Cry
 * DATE: 2018/5/9 下午10:21
 */
public class AspectRatio implements Comparable<AspectRatio>, Parcelable {
    private final static SparseArrayCompat<SparseArrayCompat<AspectRatio>> sCache
            = new SparseArrayCompat<>(16);

    private final int mX;
    private final int mY;

    public AspectRatio(int x, int y) {
        mX = x;
        mY = y;
    }

    public int getX() {
        return mX;
    }

    public int getY() {
        return mY;
    }

    public static AspectRatio of(int x, int y) {
        int gcd = gcd(x, y);
        x /= gcd;
        y /= gcd;

        SparseArrayCompat<AspectRatio> arrayX = sCache.get(x);

        if (arrayX == null) {
            arrayX = new SparseArrayCompat<>();
            AspectRatio aspectRatio = new AspectRatio(x, y);
            arrayX.put(y, aspectRatio);
            sCache.put(x, arrayX);
            return aspectRatio;
        } else {
            AspectRatio aspectRatio = arrayX.get(y);
            if (aspectRatio == null) {
                aspectRatio = new AspectRatio(x, y);
                arrayX.put(y, aspectRatio);
            }
            return aspectRatio;
        }

    }

    public boolean matches(ISize size) {
        int sizeWidth = size.getWidth();
        int sizeHeight = size.getHeight();
        int gcd = gcd(sizeWidth, sizeHeight);
        int x = sizeWidth / gcd;
        int y = sizeHeight / gcd;
        return mX == x && mY == y;
    }


    public float toFloat() {
        return (float) mX / mY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AspectRatio that = (AspectRatio) o;

        if (mX != that.mX) return false;
        return mY == that.mY;
    }

    @Override
    public int hashCode() {
        int result = mX;
        result = 31 * result + mY;
        return result;
    }

    @Override
    public String toString() {
        return mX + ":" + mY;
    }

    /**
     * x，y取反
     */
    public AspectRatio inverse() {
        //noinspection SuspiciousNameCombination
        return AspectRatio.of(mY, mX);
    }

    /*
    计算最大公约数
    */
    private static int gcd(int a, int b) {
        while (b != 0) {
            int c = b;
            b = a % b;
            a = c;
        }
        return a;
    }


    @Override
    public int compareTo(@NonNull AspectRatio another) {
        if (equals(another)) {
            return 0;
        } else if (toFloat() - another.toFloat() > 0) {
            return 1;
        } else {
            return -1;
        }
    }

    /*************************************************
     Parcelable的实现
     **************************************************/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mX);
        dest.writeInt(mY);
    }

    protected AspectRatio(Parcel in) {
        mX = in.readInt();
        mY = in.readInt();
    }

    public static final Creator<AspectRatio> CREATOR = new Creator<AspectRatio>() {
        @Override
        public AspectRatio createFromParcel(Parcel in) {
            return new AspectRatio(in);
        }

        @Override
        public AspectRatio[] newArray(int size) {
            return new AspectRatio[size];
        }
    };
}