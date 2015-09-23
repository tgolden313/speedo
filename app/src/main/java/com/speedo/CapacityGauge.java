package com.speedo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * A simple gauge consisting of 10 vertical boxes color coded from green to red to
 * indicate 100 to 0% capacity.  All ccapacity reduces, the percentage unavailable
 * is colored a dark gray.
 */
public class CapacityGauge extends View {

    // capacity percentage [0-100]
    private int capacity;

    // paint objects for each cell when full
    private Paint[] cellColors;

    // paint to use for unavailable ccapacity
    private Paint emptyColor;

    public CapacityGauge(Context context) {
        super(context);
        init();
    }

    public CapacityGauge(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initialize view object.  Default capacity is 100%.
     */
    private void init() {
    	capacity = 100;
    	cellColors = new Paint[10];

    	for (int i=0; i<10; i++) {
    		cellColors[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
    		cellColors[i].setStyle(Paint.Style.FILL);
    	}
    	
		cellColors[0].setColor(Color.rgb(0,   255, 0));
		cellColors[1].setColor(Color.rgb(40,  240, 0));
		cellColors[2].setColor(Color.rgb(80,  230, 0));
		cellColors[3].setColor(Color.rgb(130, 220, 0));
		cellColors[4].setColor(Color.rgb(180, 210, 0));
		cellColors[5].setColor(Color.rgb(220, 220, 0));
		cellColors[6].setColor(Color.rgb(215, 150, 0));
		cellColors[7].setColor(Color.rgb(230, 100, 0));
		cellColors[8].setColor(Color.rgb(245, 50,  0));
		cellColors[9].setColor(Color.rgb(255, 0,   0));

		emptyColor = new Paint(Paint.ANTI_ALIAS_FLAG);
		emptyColor.setStyle(Paint.Style.FILL);
		emptyColor.setColor(Color.rgb(33, 33, 33));
    }

    /**
     * Sets new capacity.
     *
     * @param cap - new capacity [0-100]
     */
    public void setCapacity(int cap) {
    	if (cap != capacity) {
    		capacity = cap;
    		if (capacity < 0) {
    			capacity = 0;
    		}
    		if (capacity > 100) {
    			capacity = 100;
    		}
    		invalidate();
    	}
    }

    /**
     * Returns current capacity.
     *
     * @return - current capacity [0-100]
     */
    public int getCapacity() {
    	return capacity;
    }

    /**
     * Draw view.  Draws available capacity cells first, then unavailable.
     *
     * @param canvas - to draw.
     */
	@Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Clear canvas
        canvas.drawColor(Color.TRANSPARENT);

        final int canvasWidth = canvas.getWidth() - getPaddingLeft() - getPaddingRight();
        final int canvasHeight = canvas.getHeight() - getPaddingTop() - getPaddingBottom();

        int cellPadding = 4;
        int cellHeight = (canvasHeight / 10) - cellPadding;

        // number of complete available cells
        int numFullCells = capacity / 10;

        // draw complete available cells
        int cellNum;
        for (cellNum=9; cellNum>=(10-numFullCells); cellNum--) {
        	canvas.drawRect(0.0f,
        					0.0f + cellNum*(cellHeight+cellPadding),
        					(float)canvasWidth,
        					(float)cellNum*(cellHeight+cellPadding) + cellHeight,
        					cellColors[cellNum]);
        }
        // draw all unavailable capacity cells (including partial cell if applicable)
        for (; cellNum>=0; cellNum--) {
        	canvas.drawRect(0.0f,
        					0.0f + cellNum*(cellHeight+cellPadding),
        					(float)canvasWidth,
        					(float)cellNum*(cellHeight+cellPadding) + cellHeight,
        					emptyColor);
        }
        
        // draw the available amount in the partial cell where the percentage
        // currently lies
        cellNum = 10-numFullCells-1;
        if (cellNum >= 0) {
        	int cellLevel = capacity - numFullCells*10;
        	canvas.drawRect(0.0f,
        					(cellHeight*(10-cellLevel)/10) + cellNum*(cellHeight+cellPadding),
        					(float)canvasWidth,
        					(float)cellNum*(cellHeight+cellPadding) + cellHeight,
        					cellColors[cellNum]);
        }
    }
}
