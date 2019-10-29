package com.wolfg.javaplayer.utils;

/**
 * 这个工具类不够完成，只针对于2D图形变化
 */

public class MatrixUtils {

    /**
     * 平移 缩放 旋转 正交
     */

    /**
     * 初始化矩阵为单位矩阵
     * @param matrix
     */
    public static void initMatrix(float[] matrix) {
        for (int i = 0; i < 16; i++) {
            if (i % 5 == 0) {
                matrix[i] = 1;
            } else {
                matrix[i] = 0;
            }
        }
    }


    public static void rotateMatrixZ(float[] matrix, double angle) {
        angle = angle * (Math.PI / 180.0);
        matrix[0] = (float) Math.cos(angle);
        matrix[1] = (float) -Math.sin(angle);
        matrix[4] = (float) Math.sin(angle);
        matrix[5] = (float) Math.cos(angle);
    }


    public static void orthoM(float[] matrix, float left, float right, float bottom, float top) {
        matrix[0] = 2 / (right - left);
        matrix[3] = (right + left) / (right - left) * -1;
        matrix[5] = 2 / (top - bottom);
        matrix[7] = (top + bottom) / (top - bottom) * -1;
        matrix[10] = 1;
        matrix[11] = 1;
    }


}
