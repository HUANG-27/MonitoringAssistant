package com.example.huang.client.tool;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageTool {

    //旋转图片
    public static Bitmap rotateBitmapByDegree(Bitmap bmp, int degree) {
        if (degree == 0 || null == bmp) return bmp;
        Bitmap returnBmp = null;
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
        returnBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        if (returnBmp == null) {
            returnBmp = bmp;
        }
        if (returnBmp != bmp && !bmp.isRecycled()) {
            bmp.recycle();
        }
        return returnBmp;
    }

    //保存位图
    public static void saveImage(Bitmap bitmap, String fileName) {
        try {
            File file = new File(fileName, ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
