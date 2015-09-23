package com.speedo;

import android.content.Context;
import android.util.AttributeSet;

/**
 * View for controller temp gauge.  The only different from TempGauge is the image to use
 * R.drawable.motor.
 */
public class MotorTempGauge extends TempGauge {

    public MotorTempGauge(Context context) {
        super(context, R.drawable.motor);
    }

    public MotorTempGauge(Context context, AttributeSet attrs) {
        super(context, attrs, R.drawable.motor);
    }

}
