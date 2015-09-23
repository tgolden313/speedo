package com.speedo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import com.speedo.R;

/**
 * Rotary gauge view that shows gray, green, yellow, and red sections.
 * Also shows a label in the middle of the item being measured.
 */
public class NeedleGauge extends View {
    public static final int DEFAULT_LABEL_TEXT_SIZE_DP = 24;
    public static final int DEFAULT_LABEL_TITLE_MULTIPLER = 4;

    private double maxSpeed = 100.0;
    private double speed = 0;
    private int defaultColor = Color.rgb(150, 150, 150);
    private int majorTickStep = 20;
    private int minorTickStep = 1;
    private LabelConverter labelConverter;

    private List<ColoredRange> ranges = new ArrayList<ColoredRange>();

    private Paint backgroundPaint;
    private Paint backgroundInnerPaint;
    private Paint maskPaint;
    private Paint needlePaint;
    private Paint ticksPaint;
    private Paint txtPaint;
    private Paint titlePaint;
    private Paint colorLinePaint;
    private String title;
    private Bitmap mMask;

    public NeedleGauge(Context context) {
        super(context);
        init();
    }

    public NeedleGauge(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Set the top speed shown on the gauge.
     *
     * @param maxSpeed - new top speed.
     */
    public void setMaxSpeed(double maxSpeed) {
        if (maxSpeed <= 0)
            throw new IllegalArgumentException("Non-positive value specified as max speed.");
        this.maxSpeed = maxSpeed;
        invalidate();
    }

    /**
     * Return the currrent speed.
     *
     * @return - the current speed.
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Set the current gauge reading.
     *
     * @param speed - new reading.
     */
    public void setSpeed(double speed) {
    	if (speed != this.speed) { 
    		if (speed < 0)
    			speed = 0;
    		if (speed > maxSpeed)
    			speed = maxSpeed;
    		this.speed = speed;
    		invalidate();
    	}
    }

    /**
     * Set the current major tick step (how often a value lable shows up around the dial)
     *
     * @param majorTickStep - new step value.
     */
    public void setMajorTickStep(int majorTickStep) {
        if (majorTickStep <= 0)
            throw new IllegalArgumentException("Non-positive value specified as a major tick step.");
        this.majorTickStep = majorTickStep;
        invalidate();
    }

    /**
     * Set the current major tick step (how often a value lable shows up around the dial)
     *
     * @param minorTickStep - new step value.
     */
    public void setMinorTicks(int minorTickStep) {
        this.minorTickStep = minorTickStep;
        invalidate();
    }

    /**
     * Set LabelConverter class to use for formatting major tick labels.
     *
     * @param labelConverter - LabelConverter class.
     */
    public void setLabelConverter(LabelConverter labelConverter) {
        this.labelConverter = labelConverter;
        invalidate();
    }

    /**
     * Set title that's displayed in the middle of the gauge.  Default is "".
     *
     * @param title - new title.
     */
    public void setTitle(String title) {
    	this.title = title;
    	invalidate();
    }

    public void addColoredRange(double begin, double end, int color) {
        if (begin >= end)
            throw new IllegalArgumentException("Incorrect number range specified!");
        if (begin < - 5.0/160* maxSpeed)
            begin = - 5.0/160* maxSpeed;
        if (end > maxSpeed * (5.0/160 + 1))
            end = maxSpeed * (5.0/160 + 1);
        ranges.add(new ColoredRange(color, begin, end));
        invalidate();
    }

    public void setLabelTextSize(int labelTextSize) {
        float density = getResources().getDisplayMetrics().density;
        if (txtPaint != null) {
            txtPaint.setTextSize(Math.round(density * labelTextSize));
        }
        if (titlePaint != null) {
            titlePaint.setTextSize(Math.round(density * labelTextSize * DEFAULT_LABEL_TITLE_MULTIPLER));
        }
        invalidate();
    }

    /**
     * Draw NeedleGauge view.
     *
     * @param canvas - to draw.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Clear canvas
        canvas.drawColor(Color.TRANSPARENT);

        // Draw Metallic Arc and background
        drawBackground(canvas);

        // Draw Ticks and colored arc
        drawTicks(canvas);

        // Draw Needle
        drawNeedle(canvas);
    }

    /**
     * Draws needle for gauge that indicates current reading.
     *
     * @param canvas - to draw.
     */
    private void drawNeedle(Canvas canvas) {

        // needle length is 40% of gauge width + 10 pixels to slightly overhang
        // the arc containing the ticks.
        RectF oval = getOval(canvas, 1);
        float radius = oval.width()*0.40f + 10;

        // width of needle base is 20% of gauge width
        RectF smallOval = getOval(canvas, 0.2f);

        float angle = 10 + (float) (getSpeed()/ maxSpeed*160);
        canvas.drawLine(
                (float) (oval.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * smallOval.width()*0.5f),
                (float) (oval.centerY() - Math.sin(angle / 180 * Math.PI) * smallOval.width()*0.5f),
                (float) (oval.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * (radius)),
                (float) (oval.centerY() - Math.sin(angle / 180 * Math.PI) * (radius)),
                needlePaint
        );

        // draw small oval at base of needle
        canvas.drawArc(smallOval, 180, 180, true, backgroundPaint);
    }

    /**
     * Draws arc containing minor and major ticks.  Arc is color coded gray, green, yellow, and red.
     *
     * @param canvas - to draw
     */
    private void drawTicks(Canvas canvas) {
        // 180 degrees minus 10 of each side to accommodate labels
        float availableAngle = 160;

        float majorStep = ((float)majorTickStep * availableAngle) / ((float)maxSpeed);
        float minorStep = majorStep / (1.0f + (float)minorTickStep);

        float majorTicksLength = 30.0f;
        float minorTicksLength = majorTicksLength/2;

        RectF oval = getOval(canvas, 1);

        // arc radius is 40% of gauge width
        float radius = oval.width()*0.40f;

        // draw minor and major ticks
        float currentAngle = 10;
        double curProgress = 0;
        while (currentAngle <= 170) {

            canvas.drawLine(
                    (float) (oval.centerX() + Math.cos((180 - currentAngle) / 180 * Math.PI)*(radius-majorTicksLength/2)),
                    (float) (oval.centerY() - Math.sin(currentAngle / 180 * Math.PI)*(radius-majorTicksLength/2)),
                    (float) (oval.centerX() + Math.cos((180 - currentAngle) / 180 * Math.PI)*(radius+majorTicksLength/2)),
                    (float) (oval.centerY() - Math.sin(currentAngle / 180 * Math.PI)*(radius+majorTicksLength/2)),
                    ticksPaint
            );

            for (int i=1; i<=minorTickStep; i++) {
                float angle = currentAngle + i*minorStep;
                if (angle >= 170 + minorStep/2) {
                    break;
                }
                canvas.drawLine(
                        (float) (oval.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * radius),
                        (float) (oval.centerY() - Math.sin(angle / 180 * Math.PI) * radius),
                        (float) (oval.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * (radius + minorTicksLength)),
                        (float) (oval.centerY() - Math.sin(angle / 180 * Math.PI) * (radius + minorTicksLength)),
                        ticksPaint
                );
            }

            if (labelConverter != null) {
                canvas.save();
                canvas.rotate(180 + currentAngle, oval.centerX(), oval.centerY());
                float txtX = oval.centerX() + radius + majorTicksLength/2 + 8;
                float txtY = oval.centerY();
                canvas.rotate(+90, txtX, txtY);
                canvas.drawText(labelConverter.getLabelFor(curProgress, maxSpeed), txtX, txtY, txtPaint);
                canvas.restore();
            }

            currentAngle += majorStep;
            curProgress += majorTickStep;
        }

        // draw default gray arc
        RectF smallOval = getOval(canvas, 0.8f);
        colorLinePaint.setColor(defaultColor);
        canvas.drawArc(smallOval, 185, 170, false, colorLinePaint);

        // draw the green, yellow, and red arcs
        for (ColoredRange range: ranges) {
            colorLinePaint.setColor(range.getColor());
            canvas.drawArc(smallOval,
                    (float) (190 + range.getBegin()/ maxSpeed *160),
                    (float) ((range.getEnd() - range.getBegin())/ maxSpeed *160),
                    false, colorLinePaint);
        }
    }

    /**
     * Returns a scaled rectangle that's width is 2X it's height that fits in the canvas. Used
     * as a reference for drawing the gauge.
     *
     * @param canvas - to draw.
     * @param factor - a scale factor to apply to the canvas width.
     * @return - a rectangle that fits in the canvas that's scaled by facter.
     */
    private RectF getOval(Canvas canvas, float factor) {
        RectF oval;
        int canvasWidth = canvas.getWidth() - getPaddingLeft() - getPaddingRight();
        int canvasHeight = canvas.getHeight() - getPaddingTop() - getPaddingBottom();

        oval = new RectF(0, 0, canvasWidth*factor, canvasWidth*factor);
        oval.offset((canvasWidth-oval.width())/2 + getPaddingLeft(), (canvasHeight*2-oval.height())/2 + getPaddingTop());

        return oval;
    }

    /**
     * Draw base layer of gauge.  Includes the semi-circle of the gauge and the title.
     *
     * @param canvas - to draw.
     */
    private void drawBackground(Canvas canvas) {
        RectF oval = getOval(canvas, 1);

        Float scale = 1.25f;

        // draw base arc
        canvas.drawArc(oval, 180, 180, true, backgroundInnerPaint);

        // use bitmap to draw a fade from dark to light in the center of the arc
        Bitmap mask = Bitmap.createScaledBitmap(mMask, (int)(oval.width() * scale), (int)((oval.height() * scale) / 2.0f), true);
        canvas.drawBitmap(mask, oval.centerX() - oval.width()*scale/2.0f, oval.centerY()-oval.width()*scale/2.0f+1, maskPaint);
        
        // draw title
        float txtX = canvas.getWidth() / 2;
        float txtY = canvas.getHeight() * 7 / 10;
        canvas.drawText(title, txtX, txtY, titlePaint);
    }

    /**
     * Initialize view.
     */
    private void init() {
        if (Build.VERSION.SDK_INT >= 11 && !isInEditMode()) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        float density = getResources().getDisplayMetrics().density;

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.rgb(33, 33, 33));

        backgroundInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundInnerPaint.setStyle(Paint.Style.FILL);
        backgroundInnerPaint.setColor(Color.rgb(66, 66, 66));

        txtPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        txtPaint.setColor(Color.WHITE);
        txtPaint.setTextSize(Math.round(density * DEFAULT_LABEL_TEXT_SIZE_DP));
        txtPaint.setTextAlign(Paint.Align.CENTER);
        txtPaint.setLinearText(true);

        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(Color.rgb(33, 33, 33));
        titlePaint.setTextSize(Math.round(density * DEFAULT_LABEL_TEXT_SIZE_DP * DEFAULT_LABEL_TITLE_MULTIPLER));
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setFakeBoldText(true);
        titlePaint.setLinearText(true);

        mMask = BitmapFactory.decodeResource(getResources(), R.drawable.spot_mask);
        mMask = Bitmap.createBitmap(mMask, 0, 0, mMask.getWidth(), mMask.getHeight() / 2);

        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint.setDither(true);

        ticksPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ticksPaint.setStrokeWidth(3.0f);
        ticksPaint.setStyle(Paint.Style.STROKE);
        ticksPaint.setColor(defaultColor);

        colorLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        colorLinePaint.setStyle(Paint.Style.STROKE);
        colorLinePaint.setStrokeWidth(5);
        colorLinePaint.setColor(defaultColor);

        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setStrokeWidth(8);
        needlePaint.setStyle(Paint.Style.STROKE);
        needlePaint.setColor(Color.argb(200, 255, 0, 0));
        
        title = "";
    }

    /**
     * Handles the onMeasure callback.  Ensure view is contrained to width == 2Xheight.
     *
     * @param widthMeasureSpec - parent's width requirement.
     * @param heightMeasureSpec - parent's height requirement.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST) {
            //Must be this size
            width = widthSize;
        } else {
            width = -1;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.AT_MOST) {
            //Must be this size
            height = heightSize;
        } else {
            height = -1;
        }

        if (height >= 0 && width >= 0) {
            width = Math.min(height, width);
            height = width/2;
        } else if (width >= 0) {
            height = width/2;
        } else if (height >= 0) {
            width = height*2;
        } else {
            width = 0;
            height = 0;
        }

        // set constrained dimensions
        setMeasuredDimension(width, height);
    }

    /**
     * Declaration of LabelConverter class.  The user will define getLabelFor() method.
     * Used to format major tick labels for gauge.
     */
    public interface LabelConverter {

        String getLabelFor(double progress, double maxProgress);
    }

    /**
     * Represents a special color range for the gauge.
     */
    public static class ColoredRange {

        private int color;
        private double begin;
        private double end;

        /**
         * Constructor.
         *
         * @param color - color to use. (e.g. green, yellow, red)
         * @param begin - start reading for range.
         * @param end - end reading for range.
         */
        public ColoredRange(int color, double begin, double end) {
            this.color = color;
            this.begin = begin;
            this.end = end;
        }

        public int getColor() {
            return color;
        }

        public double getBegin() {
            return begin;
        }

        public double getEnd() {
            return end;
        }
    }
}
