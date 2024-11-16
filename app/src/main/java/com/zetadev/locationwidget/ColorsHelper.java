package com.zetadev.locationwidget;

import android.content.Context;
import android.util.TypedValue;

import androidx.core.graphics.ColorUtils;

public class ColorsHelper {



    public static int getPrimarycolor(Context context, float opacity) {
        TypedValue typedValue = new TypedValue();
        // Resolve the colorPrimary attribute in the current theme
        context.getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
        int colorPrimary = typedValue.data;


        // Apply the desired opacity (opacity is between 0 and 1)
        int alpha = (int) (opacity * 255);  // Convert opacity to alpha value (0-255)
        return ColorUtils.setAlphaComponent(colorPrimary, alpha);
    }


    public static int getSecondarycolor(Context context, float opacity) {
        TypedValue typedValue = new TypedValue();
        // Resolve the colorPrimary attribute in the current theme
        context.getTheme().resolveAttribute(android.R.attr.colorSecondary, typedValue, true);
        int colorPrimary = typedValue.data;


        // Apply the desired opacity (opacity is between 0 and 1)
        int alpha = (int) (opacity * 255);  // Convert opacity to alpha value (0-255)
        return ColorUtils.setAlphaComponent(colorPrimary, alpha);
    }

    public static int getPrimaryDarkcolor(Context context, float opacity) {
        TypedValue typedValue = new TypedValue();
        // Resolve the colorPrimary attribute in the current theme
        context.getTheme().resolveAttribute(android.R.attr.colorPrimaryDark, typedValue, true);
        int colorPrimary = typedValue.data;


        // Apply the desired opacity (opacity is between 0 and 1)
        int alpha = (int) (opacity * 255);  // Convert opacity to alpha value (0-255)
        return ColorUtils.setAlphaComponent(colorPrimary, alpha);
    }





}


