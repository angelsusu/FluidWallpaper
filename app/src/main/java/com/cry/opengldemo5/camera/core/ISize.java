package com.cry.opengldemo5.camera.core;

import android.support.annotation.NonNull;
import android.util.ArrayMap;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * DESCRIPTION:
 * Author: Cry
 * DATE: 2018/5/9 下午10:46
 */

public class ISize implements Comparable<ISize> {
    private final int mWidth;
    private final int mHeight;

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public ISize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ISize iSize = (ISize) o;

        if (mWidth != iSize.mWidth) return false;
        return mHeight == iSize.mHeight;
    }

    @Override
    public int hashCode() {
        int result = mWidth;
        result = 31 * result + mHeight;
        return result;
    }

    @Override
    public int compareTo(@NonNull ISize another) {
        return this.mHeight * this.mWidth - another.mWidth * another.mHeight;
    }

    @Override
    public String toString() {
        return mWidth + "x" + mHeight;
    }


    /**
     * 这个map是根据 比例AspectRadio来存放，对应比例的尺寸的。同时，尺寸按照大小排列
     */
    public static class ISizeMap {
        private final ArrayMap<AspectRatio, SortedSet<ISize>> mRatioSizeSets = new ArrayMap<>();

        public boolean add(ISize size) {
            for (AspectRatio aspectRatio : mRatioSizeSets.keySet()) {
                if (aspectRatio.matches(size)) {
                    SortedSet<ISize> iSizes = mRatioSizeSets.get(aspectRatio);
                    if (iSizes.contains(size)) {
                        return false;
                    } else {
                        iSizes.add(size);
                        return true;
                    }
                }
            }

            //没有找到当前的尺寸的话
            SortedSet<ISize> sizes = new TreeSet<>();
            sizes.add(size);
            mRatioSizeSets.put(AspectRatio.of(size.getWidth(), size.getHeight()), sizes);
            return true;
        }

        public void remove(AspectRatio ratio) {
            mRatioSizeSets.remove(ratio);
        }

        public Set<AspectRatio> ratios() {
            return mRatioSizeSets.keySet();
        }

        public SortedSet<ISize> sizes(AspectRatio ratio) {
            return mRatioSizeSets.get(ratio);
        }

        public void clear(){
            mRatioSizeSets.clear();
        }

        public boolean isEmpty(){
           return mRatioSizeSets.isEmpty();
        }
    }
}
