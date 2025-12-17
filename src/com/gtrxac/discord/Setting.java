package com.gtrxac.discord;

import javax.microedition.lcdui.*;

public class Setting implements Strings {
    String label;
    int maxValue;
    int value;
    String[] valueLabels;
    Image[] icons;

    private static final int[] boolValueLabels = { SETTING_VALUE_OFF, SETTING_VALUE_ON };

    Setting(int labelKey, int maxValue, int value, Object valueLabels, Object icons) {
        this.label = Locale.get(labelKey);
        this.maxValue = maxValue;
        this.value = value;

        if (valueLabels instanceof int[]) {
            int[] valueLabelsArr = (int[]) valueLabels;
            this.valueLabels = new String[valueLabelsArr.length];

            for (int i = 0; i < valueLabelsArr.length; i++) {
                this.valueLabels[i] = Locale.get(valueLabelsArr[i]);
            }
        } else {
            this.valueLabels = (String[]) valueLabels;
        }

        if (icons instanceof Image[]) {
            this.icons = (Image[]) icons;
        } else {
            // it should be one icon, so create array with that one icon 
            this.icons = new Image[] { (Image) icons };
        }
    }

    Setting(int labelKey, int maxValue, int value, Object icons) {
        this(labelKey, maxValue, value, boolValueLabels, icons);
    }

    /**
     * dummy setting that is not displayed in the menu and just stores and keeps its given value
     */
    Setting(int value) {
        this.value = value;
    }
}