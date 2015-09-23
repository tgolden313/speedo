package com.speedo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Simple temp gauge consists of drawing the current temperature over a provided image.
 */
public class TempGauge extends View {

    // current temperature in fahrenheit
    private int temp;

    private int labelSize;
    private Paint txtPaint;
    private Paint imagePaint;
    private Bitmap image;

    public TempGauge(Context context, int bitmapResource) {
        super(context);
        init(bitmapResource);
    }

    public TempGauge(Context context, AttributeSet attrs, int bitmapResource) {
        super(context, attrs);
        init(bitmapResource);
    }

    /**
     * Initialize view.  Default temperature is 0 degF.
     *
     * @param bitmapResource - bitmap to use for view.
     */
    private void init(int bitmapResource) {
        float density = getResources().getDisplayMetrics().density;

    	temp = 0;
    	labelSize = 24;
    	
        image = BitmapFactory.decodeResource(getResources(), bitmapResource);

        imagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        imagePaint.setDither(true);

        txtPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        txtPaint.setColor(Color.BLACK);
        txtPaint.setTextSize(Math.round(labelSize * density));
        txtPaint.setTextAlign(Paint.Align.CENTER);
        txtPaint.setLinearText(true);
    }

    /**
     * Set new temperature.
     *
     * @param temp - new temperature in fahrenheit.
     */
    public void setTemp(int temp) {
    	if (temp != this.temp) {
    		this.temp = temp;
    		invalidate();
    	}
    }

    /**
     * Change text size of temperature data.  Default is 24.
     *
     * @param labelSize - new label size.
     */
    public void setLabelTextSize(int labelSize) {
        float density = getResources().getDisplayMetrics().density;

        this.labelSize = labelSize;
        txtPaint.setTextSize(Math.round(labelSize * density));
    	invalidate();
    }

    /**
     * Draw view.
     *
     * @param canvas - to draw.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Bitmap scaledImage = Bitmap.createScaledBitmap(image, canvas.getWidth(), canvas.getHeight(), true);
        canvas.drawBitmap(scaledImage, 0, 0, imagePaint);

        // draw temp text
        String tempString = "" + temp + "\u00b0" + "F";
        float txtX = canvas.getWidth() / 2;
        float txtY = canvas.getHeight() * 6 / 10;
        canvas.drawText(tempString, txtX, txtY, txtPaint);
    }
}
