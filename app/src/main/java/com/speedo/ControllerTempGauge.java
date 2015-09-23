package com.speedo;

import android.content.Context;
import android.util.AttributeSet;

/**
 * View for controller temp gauge.  The only different from TempGauge is the image to use
 * R.drawable.controller.
 */
public class ControllerTempGauge extends TempGauge {

    public ControllerTempGauge(Context context) {
        super(context, R.drawable.controller);
    }

    public ControllerTempGauge(Context context, AttributeSet attrs) {
        super(context, attrs, R.drawable.controller);
    }
}
