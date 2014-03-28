package com.eleks.rnd.warehouse.qr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CameraOverlay extends View {

    public CameraOverlay(Context context) {
        super(context);
    }

    public CameraOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
        canvas.drawText("Test Text", 10, 10, paint);

        super.onDraw(canvas);
    }

}