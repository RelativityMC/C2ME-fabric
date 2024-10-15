package com.ishland.c2me.opts.dfc.common.ast.spline;

public class SplineSupport {

    public static int findRangeForLocation(float[] locations, float x) {
        int min = 0;
        int i = locations.length;

        while (i > 0) {
            int j = i / 2;
            int k = min + j;
            if (x < locations[k]) {
                i = j;
            } else {
                min = k + 1;
                i -= j + 1;
            }
        }

        return min - 1;
    }

    public static float sampleOutsideRange(float point, float[] locations, float value, float[] derivatives, int i) {
        float f = derivatives[i];
        return f == 0.0F ? value : value + f * (point - locations[i]);
    }

}
