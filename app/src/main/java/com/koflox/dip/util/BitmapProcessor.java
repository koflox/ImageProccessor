package com.koflox.dip.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

public class BitmapProcessor {

    public static void drawHistogram(Bitmap image, ImageView ivRgbHistogram, ImageView ivLumHistogram, Context c)
    {
        if (image != null)
        {
            int width = 768, height = 600;
            Bitmap bmp = Bitmap.createBitmap(image);
            Bitmap rgbHistogram = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Bitmap lumHistogram = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            int[] r = new int[256];
            int[] g = new int[256];
            int[] b = new int[256];
            int[] lum = new int[256];
            int i, j;
            int color;
            for (i = 0; i < bmp.getWidth(); ++i)
                for (j = 0; j < bmp.getHeight(); ++j) {
                    color = bmp.getPixel(i, j);
                    ++r[Color.red(color)];
                    ++g[Color.green(color)];
                    ++b[Color.blue(color)];

                    int lumIndex = (int) (0.3 * Color.red(color) + 0.59 * Color.green(color) + 0.11 * Color.blue(color));
                    ++lum[lumIndex];
                }

            for (i = 0; i < lum.length; i++) {
                Log.d("Logos", "lum[i] is " + lum[i]);
            }
            int max = 0;
            for (i = 0; i < 256; ++i) {
                if (r[i] > max) max = r[i];
                if (g[i] > max) max = g[i];
                if (b[i] > max) max = b[i];
                if (lum[i] > max) max = lum[i];
            }
            double point = (double) max / height;
            for (i = 0; i < width - 3; ++i) {
                for (j = height - 1; j > height - r[i / 3] / point; --j) {
                    rgbHistogram.setPixel(i, j, Color.RED);
                }
                ++i;
                for (j = height - 1; j > height - g[i / 3] / point; --j) {
                    rgbHistogram.setPixel(i, j, Color.GREEN);
                }
                ++i;
                for (j = height - 1; j > height - b[i / 3] / point; --j) {
                    rgbHistogram.setPixel(i, j, Color.BLUE);
                }
            }
            for (i = 0; i < width - 3; ++i) {
                for (j = height - 1; j > height - lum[i / 3] / point; --j) {
                    lumHistogram.setPixel(i, j, Color.BLACK);
                }
            }
            ivRgbHistogram.setImageBitmap(rgbHistogram);
            ivLumHistogram.setImageBitmap(lumHistogram);
        } else {
            Toast.makeText(c, "Choose a picture", Toast.LENGTH_SHORT).show();
        }
    }

    public static void linearContrastProcess(Bitmap image, ImageView iv, int NewMax, int NewMin, Context c) {
        if (image != null) {
            int[] lum = new int[256];
            int oldMin;
            int oldMax = 0;
            for (int i = 0; i < image.getWidth(); ++i)
                for (int j = 0; j < image.getHeight(); ++j) {
                    int color = image.getPixel(i, j);
                    int lumIndex = (int) (0.3 * Color.red(color) + 0.59 * Color.green(color) + 0.11 * Color.blue(color));
                    ++lum[lumIndex];
                }

            for (int i = 0; i < lum.length; i++) {
                oldMax = (i > oldMax && lum[i] != 0) ? i : oldMax;
            }
            oldMin = oldMax;
            for (int i = 0; i < lum.length; i++) {
                oldMin = (i < oldMin && lum[i] != 0) ? i : oldMin;
            }

            Bitmap orig = Bitmap.createBitmap(image);
            Bitmap bmp = Bitmap.createBitmap(orig.getWidth(), orig.getHeight(), Bitmap.Config.ARGB_8888);
            for (int i = 0; i < orig.getWidth(); i++)
            {
                for (int j = 0; j < orig.getHeight(); j++)
                {
                    int oldColor = orig.getPixel(i, j);

                    int newR = (NewMax - NewMin) * (Color.red(oldColor) - oldMin) / (oldMax - oldMin) + NewMin;
                    if (newR > 255) newR = 255;
                    if (newR < 0) newR = 0;

                    int newG = (NewMax - NewMin) * (Color.green(oldColor) - oldMin) / (oldMax - oldMin) + NewMin;
                    if (newG > 255) newG = 255;
                    if (newG < 0) newG = 0;

                    int newB = (NewMax - NewMin) * (Color.blue(oldColor) - oldMin) / (oldMax - oldMin) + NewMin;
                    if (newB > 255) newB = 255;
                    if (newB < 0) newB = 0;

                    bmp.setPixel(i, j, Color.argb(255, newR, newG, newB));
                }
            }

            iv.setImageBitmap(bmp);
        } else {
            Toast.makeText(c, "Choose a picture", Toast.LENGTH_SHORT).show();
        }

    }

    public static void smooth(Bitmap image, ImageView iv, double[][] matrix, Context c) {
        if (image != null) {
            Bitmap orig = Bitmap.createBitmap(image);
            Bitmap bmp = Bitmap.createBitmap(orig.getWidth(), orig.getHeight(), Bitmap.Config.ARGB_8888);


            double sum = 0d;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    sum += matrix[i][j];
                }
            }

            for (int i = 1; i < orig.getWidth() - 1; i++)
            {
                for (int j = 1; j < orig.getHeight() - 1; j++)
                {
                    bmp.setPixel(i, j, getNewColor(orig, i, j, matrix, sum));
                }
            }
            iv.setImageBitmap(bmp);
        } else {
            Toast.makeText(c, "Choose a picture", Toast.LENGTH_SHORT).show();
        }
    }

    private static int getNewColor(Bitmap bmp, int x, int y, double[][] matrix, double sum)
    {
        double rValue = 0;
        double gValue = 0;
        double bValue = 0;

        for (int i = -1; i < 2; i++)
        {
            for (int j = -1; j < 2; j++)
            {
                int color = bmp.getPixel(x + i, y + j);
                double coef = matrix[i + 1][j + 1] / sum;
                rValue += Color.red(color) * coef;
                gValue += Color.green(color) * coef;
                bValue += Color.blue(color) * coef;
            }
        }

        if (rValue > 255) rValue = 255;
        if (rValue < 0) rValue = 0;
        if (gValue > 255)gValue = 255;
        if (gValue < 0)gValue = 0;
        if (bValue > 255)bValue = 255;
        if (bValue < 0) bValue = 0;

        return Color.argb(255, (int) rValue, (int) gValue, (int) bValue);
    }

    public static void edgesFiler(Bitmap image, ImageView iv, Context c, int limit) {
        if (image != null) {
            Bitmap orig = Bitmap.createBitmap(image);
            Bitmap bmp = Bitmap.createBitmap(orig.getWidth(), orig.getHeight(), Bitmap.Config.ARGB_8888);

            for (int i = 1; i < orig.getWidth() - 1; i++)
            {
                for (int j = 1; j < orig.getHeight() - 1; j++)
                {
                    if (getF(orig, i, j) < limit)
                    {
                        bmp.setPixel(i, j, Color.BLACK);
                    }
                    else
                    {
                        bmp.setPixel(i, j, Color.WHITE);
                    }
                }
            }
            iv.setImageBitmap(bmp);
        } else {
            Toast.makeText(c, "Choose a picture", Toast.LENGTH_SHORT).show();
        }
    }

    private static double getF(Bitmap bmp, int x, int y)
    {
        int[][] xOperator = { { -1, 0, 1 }, { -2, 0, 2 }, { -1, 0, 1 } };
        int[][] yOperator = { { -1, -2, -1 }, { 0, 0, 0 }, { 1, 2, 1 } };

        int z1 = GetBrightness(bmp.getPixel(x - 1, y - 1));
        int z2 = GetBrightness(bmp.getPixel(x, y - 1));
        int z3 = GetBrightness(bmp.getPixel(x + 1, y - 1));
        int z4 = GetBrightness(bmp.getPixel(x - 1, y));
        int z5 = GetBrightness(bmp.getPixel(x, y));
        int z6 = GetBrightness(bmp.getPixel(x + 1, y));
        int z7 = GetBrightness(bmp.getPixel(x - 1, y + 1));
        int z8 = GetBrightness(bmp.getPixel(x, y + 1));
        int z9 = GetBrightness(bmp.getPixel(x + 1, y + 1));

        int gx = (z7 * xOperator[2][0] + z8 * xOperator[2][1] + z9 * xOperator[2][2])
        - (z1 * xOperator[0][0] + z2 * xOperator[0][1] + z3 * xOperator[0][2]);
        int gy = (z3 * yOperator[0][2] + z6 * yOperator[1][2] + z9 * yOperator[2][2]
        - (z1 * yOperator[0][0] + z4 * yOperator[1][0] + z7 * yOperator[2][0]));

        double f = Math.sqrt(Math.pow(gx, 2) + Math.pow(gy, 2));
        return f;
    }

    private static int GetBrightness(int color)
    {
        return (int) (0.3 * Color.red(color) + 0.59 * Color.green(color) + 0.11 * Color.blue(color));
    }

    public static void pasteurize(Bitmap image, ImageView iv, Context c, int level) {
        if (image != null) {
            Bitmap orig = Bitmap.createBitmap(image);
            Bitmap bmp = Bitmap.createBitmap(orig.getWidth(), orig.getHeight(), Bitmap.Config.ARGB_8888);

            for (int i = 0; i < orig.getWidth(); i++)
            {
                for (int j = 0; j < orig.getHeight(); j++)
                {
                    int oldColor = orig.getPixel(i, j);

                    int newR = ((Color.red(oldColor) / level) * level);
                    if (newR > 255) newR = 255;
                    if (newR < 0) newR = 0;

                    int newG = ((Color.green(oldColor) / level) * level);
                    if (newG > 255) newG = 255;
                    if (newG < 0) newG = 0;

                    int newB = ((Color.blue(oldColor) / level) * level);
                    if (newB > 255) newB = 255;
                    if (newB < 0) newB = 0;

                    bmp.setPixel(i, j, Color.argb(255, newR, newG, newB));
                }
            }
            iv.setImageBitmap(bmp);
        } else {
            Toast.makeText(c, "Choose a picture", Toast.LENGTH_SHORT).show();
        }
    }

}
