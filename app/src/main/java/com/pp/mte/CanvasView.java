package com.pp.mte;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.graphics.Path;

/**
 * Created by kevin on 07/12/18.
 */

public class CanvasView extends View {

    public int width;
    public int height;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint paint;
    private Path path;
    private float x, y;

    public CanvasView(Context context, AttributeSet attribs) {
        super(context, attribs);

        path = new Path();

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(4f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oW, int oH) {
        super.onSizeChanged(w, h, oW, oH);
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }


    private void onStartTouch(float x, float y) {
        path.moveTo(x, y);
        this.x = x;
        this.y = y;
    }

    private void moveTouch(float x, float y) {
        float dx = Math.abs(x - this.x);
        float dy = Math.abs(y - this.y);
    }
}
