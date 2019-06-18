package com.cry.opengldemo5.shape.base;

/**
 * Created by a2957 on 2018/5/5.
 */

public class ShapeBuilder {

    /**
     * 创建三维的圆面
     *
     * @param circle
     * @param numbersRoundCircle
     * @param TOTAL_COMPONENT_COUNT
     * @return
     */
    public static float[] create3DCircleCoords(Circle circle, int numbersRoundCircle, int TOTAL_COMPONENT_COUNT) {
        //先计算总共需要多少个点
        int needNumber = getCircleVertexNum(numbersRoundCircle);
        //创建数组
        float[] circleColorCoord = new float[needNumber * TOTAL_COMPONENT_COUNT];
        //接下来给每个点分配数据

        //对每一组点进行赋值
        for (int numberIndex = 0; numberIndex < needNumber; numberIndex++) {
            int indexOffset = numberIndex * TOTAL_COMPONENT_COUNT;

            if (numberIndex == 0) {   //第一个点。就是圆心
                //位置
                circleColorCoord[indexOffset] = circle.center.x;
                circleColorCoord[indexOffset + 1] = circle.center.y;
                circleColorCoord[indexOffset + 2] = circle.center.z;

                //下面是颜色。给一个白色
                circleColorCoord[indexOffset + 3] = 1.f;
                circleColorCoord[indexOffset + 4] = 1.f;
                circleColorCoord[indexOffset + 5] = 1.f;
            } else if (numberIndex < needNumber - 1) {    //切分圆的点
                //需要根据半径。中心点。来结算
                int angleIndex = numberIndex - 1;
                float angleRadius = (float) (((float) angleIndex / (float) numbersRoundCircle) * Math.PI * 2f);
                float centerX = circle.center.x;
                float centerY = circle.center.y;
                float centerZ = circle.center.z;
                float radius = circle.radius;
                //圆是xz轴的切面
                float tempX = (float) (centerX + radius * Math.cos(angleRadius));
                float tempY = centerY + 0;
                float tempZ = (float) (centerZ + +radius * Math.sin(angleRadius));

                //位置

                circleColorCoord[indexOffset] = tempX;
                circleColorCoord[indexOffset + 1] = tempY;
                circleColorCoord[indexOffset + 2] = tempZ;

                //下面是颜色。给一个白色
                circleColorCoord[indexOffset + 3] = (float) (1.f * Math.cos(angleRadius));
                circleColorCoord[indexOffset + 4] = (float) (1.f * Math.sin(angleRadius));
                circleColorCoord[indexOffset + 5] = 1.f;
            } else { //最后一个点了。重复数据中的二组的位置
                //位置.index为1的点
                int copyTargetIndex = 1;
                //复制点
                circleColorCoord[indexOffset] = circleColorCoord[copyTargetIndex * TOTAL_COMPONENT_COUNT];
                circleColorCoord[indexOffset + 1] = circleColorCoord[copyTargetIndex * TOTAL_COMPONENT_COUNT + 1];
                circleColorCoord[indexOffset + 2] = circleColorCoord[copyTargetIndex * TOTAL_COMPONENT_COUNT + 2];

                circleColorCoord[indexOffset + 3] = circleColorCoord[copyTargetIndex * TOTAL_COMPONENT_COUNT + 3];
                circleColorCoord[indexOffset + 4] = circleColorCoord[copyTargetIndex * TOTAL_COMPONENT_COUNT + 4];
                circleColorCoord[indexOffset + 5] = circleColorCoord[copyTargetIndex * TOTAL_COMPONENT_COUNT + 5];
            }

        }
        return circleColorCoord;

    }

    public static float[] create3DCylinderCoords(Cylinder cylinder, int numbersRoundCircle, int TOTAL_COMPONENT_COUNT) {
        //先计算总共需要多少个点
        int needNumber = getCylinderVertexNum(numbersRoundCircle);
        //创建数组
        float[] circleColorCoord = new float[needNumber * TOTAL_COMPONENT_COUNT];
        //接下来给每个点分配数据
        float yStart = cylinder.center.y - cylinder.height / 2f;
        float yEnd = cylinder.center.y + cylinder.height / 2f;
        //对每一组点进行赋值

        //这里用大于等于。是因为还多了一个点要处理
        for (int angleIndex = 0; angleIndex <= numbersRoundCircle; angleIndex++) {
            int indexOffset = angleIndex * 2 * TOTAL_COMPONENT_COUNT;
            float angleRadius = (float) (((float) angleIndex / (float) numbersRoundCircle) * Math.PI * 2f);

            float centerX = cylinder.center.x;
            float centerZ = cylinder.center.z;
            float radius = cylinder.radius;
            float xPosition = (float) (centerX + radius * Math.cos(angleRadius));
            float zPosition = (float) (centerZ + radius * Math.sin(angleRadius));

            //下点
            circleColorCoord[indexOffset] = xPosition;
            circleColorCoord[indexOffset + 1] = yStart;
            circleColorCoord[indexOffset + 2] = zPosition;

            circleColorCoord[indexOffset + 3] = 0.5f;
            circleColorCoord[indexOffset + 4] = 0.5f;
            circleColorCoord[indexOffset + 5] = 0.5f;

            //上
            circleColorCoord[indexOffset + 6] = xPosition;
            circleColorCoord[indexOffset + 7] = yEnd;
            circleColorCoord[indexOffset + 8] = zPosition;

            circleColorCoord[indexOffset + 9] = 1f;
            circleColorCoord[indexOffset + 10] = 1.f;
            circleColorCoord[indexOffset + 11] = 1.f;

        }

        return circleColorCoord;

    }

    /*
    需要的点的个数等于 1(圆心)+切分圆的点数+1(为了闭合，切分圆的起点和终点，需要重复一次)
     */
    public static int getCircleVertexNum(int numbersRoundCircle) {
        return +1 + numbersRoundCircle + 1;
    }

    /*
  圆柱体的侧面是一个圈起来的长方形。位置顶部圆的每一个点都需要两个顶点。而且前两个顶点需要重复两次才能闭合
    */
    public static int getCylinderVertexNum(int numbersRoundCircle) {
        return (numbersRoundCircle + 1) * 2;
    }


}
