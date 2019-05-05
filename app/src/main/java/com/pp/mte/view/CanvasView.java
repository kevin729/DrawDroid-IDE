package com.pp.mte.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.Path;

/**
 * Created by kevin on 5/02/19
 * Drawing panel to get code
 */

public class CanvasView extends View {
    private EditorActivity editor;
    private Paint paint;
    private Path path;

    public CanvasView(Context context, EditorActivity editor) {
        super(context);
        this.editor = editor;

        path = new Path();
        paint = new Paint();
        paint.setAntiAlias(false);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(50f);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float pointX = e.getX();
        float pointY = e.getY();

        //Sets the path to be drawn
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(pointX, pointY);
                return true;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(pointX, pointY);
                break;
            case MotionEvent.ACTION_UP:
                feedForward();
            default:
                return false;
        }
        postInvalidate();
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(path, paint);
    }

    public void clear() {
        path.reset();
        postInvalidate();
    }

    /**
     * Formats canvas image and sends pixels through the neural network
     */
    public void feedForward() {
        if (!path.isEmpty()) {
            int[] pixels = new int[getWidth() * getHeight()];

            //get pixel data
            Bitmap image = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(image);
            draw(canvas);

            image.getPixels(pixels, 0, getWidth(), 0, 0, getWidth(), getHeight());

            //scales and resize image for the network
            Bitmap img = shrinkImage(pixels, getWidth(), getHeight());

            if (img.getWidth() <= 50 || img.getHeight() <= 50) {
                clear();
                return;
            }

            pixels = resizeImage(img, 128);
            editor.presenter.handleGesture(normalise(pixels));
        }
    }

    /**
     * Removes whitespace
     * @param pixels of original image
     * @param screenWidth of original image width
     * @param screenHeight of original image height
     * @return shrunk Bitmap
     */
    private Bitmap shrinkImage(int[] pixels, int screenWidth, int screenHeight) {

        int minX = screenWidth, minY = screenHeight;
        int maxX = 0, maxY = 0;

        for (int y = 0; y < screenHeight; y++) {
            for (int x = 0; x < screenWidth; x++) {
                if (pixels[x+y*screenWidth] != 0) {
                    if (minX > x) {
                        minX = x;
                    }
                    if (maxX < x) {
                        maxX = x;
                    }

                    if (minY > y) {
                        minY = y;
                    }
                    if (maxY < y) {
                        maxY = y;
                    }
                }
            }
        }

        int width = maxX - minX;
        int height = maxY - minY;

        int[] newPixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                newPixels[x+y*width] = pixels[(x+minX)+(y+minY)*screenWidth];
            }
        }
        Bitmap img = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        if (width > 0 && height > 0) {
            img = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            img.setPixels(newPixels, 0, width, 0, 0, width, height);
        }

        return img;
    }

    /**
     * Sets the image to newSize x newSize
     * @param img
     * @param newSize
     * @return new sized pixel array
     */
    private int[] resizeImage(Bitmap img, int newSize) {
        Bitmap newImg = Bitmap.createScaledBitmap(img, newSize, newSize, true);

        int[] pixels = new int[newSize*newSize];
        newImg.getPixels(pixels, 0, newSize, 0, 0, newSize, newSize);

        return pixels;
    }

    /**
     * Changes pixel data to match neural network (black is 1, white is -1)
     * @param pixels
     * @return normalised pixel array
     */
    private double[] normalise(int[] pixels) {
        double[] newPixels = new double[pixels.length];
        for(int i = 0; i < pixels.length; i++) {
            if(pixels[i] != 0) {
                newPixels[i] = 1;
            } else {
                newPixels[i] = -1;
            }
        }

        return newPixels;
    }
}
