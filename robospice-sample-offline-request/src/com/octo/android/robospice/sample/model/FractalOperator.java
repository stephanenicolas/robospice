package com.octo.android.robospice.sample.model;

/**
 * Implement this interface to calculate the fractal escape velocity for a given fractal set (i.e. Mandelbrot).
 * 
 * @author girino
 */
public interface FractalOperator {
    /**
     * added this as a hack to how to choose julia's C.
     * 
     * @param config
     */
    public int apply( double x, double y, int maxiter );
}