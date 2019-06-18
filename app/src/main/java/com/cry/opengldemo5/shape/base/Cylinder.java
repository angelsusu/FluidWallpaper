package com.cry.opengldemo5.shape.base;

/**
 * 圆柱体
 * Created by a2957 on 2018/5/5.
 */
public class Cylinder {
    public final Point center;
    public final float radius;
    public final float height;

    public Cylinder(Point center, float radius, float height) {
        this.center = center;
        this.radius = radius;
        this.height = height;
    }
}
