package com.octo.android.robospice.sample.model;

/**
 * This is optimized by removing the Complex class and working directly with doubles. Less abstraction, much
 * performance.
 * 
 * @author girino
 * 
 */
public class OptimizedMandelbrotOperator implements FractalOperator {

    private static final double omega = 4.0;

    public int apply( double x0, double y0, int maxiter ) {
        double x = 0;
        double y = 0;
        int v = 0;
        for ( ; v < maxiter && x * x + y * y < omega; v++ ) {
            double t = x0 + x * x - y * y;
            y = y0 + x * y * 2f;
            x = t;
        }
        return v;
    }

}