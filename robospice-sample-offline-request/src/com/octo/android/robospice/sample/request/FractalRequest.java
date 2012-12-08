package com.octo.android.robospice.sample.request;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.sample.model.FractalOperator;

public final class FractalRequest extends SpiceRequest< Bitmap > {

    private Bitmap bitmap;
    private Canvas canvas;
    private Paint mBitmapPaint;
    private double x0 = 0.0;
    private double y0 = 0.0;
    private int width = 320;
    private int height = 480;
    private double scale = 100.0 * 300.0 / width;
    private FractalOperator fractalOperator;

    public FractalRequest( double x0, double y0, int width, int height, double scale, FractalOperator fractalOperator, Bitmap bitmap ) {
        super( Bitmap.class );
        mBitmapPaint = new Paint( Paint.DITHER_FLAG );
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.x0 = x0;
        this.y0 = y0;
        this.fractalOperator = fractalOperator;
        this.bitmap = bitmap;

        canvas = new Canvas( bitmap );
    }

    @Override
    public Bitmap loadDataFromNetwork() {
        runMandelbrot( width, height, scale, x0, y0, fractalOperator, bitmap );
        return bitmap;
    }

    public static double hscale( int i, int width, double scale, double x0 ) {
        return ( i - width / 2 ) / scale + x0;
    }

    public static double vscale( int j, int height, double scale, double y0 ) {
        return ( j - height / 2 ) / scale + y0;
    }

    public void runMandelbrot( int width, int height, double scale, double x0, double y0, FractalOperator fractalOperator, Bitmap bitmap ) {
        for ( int d = 8; d > 0; d /= 2 ) {
            for ( int j = 0; j < height; j += d ) {
                for ( int i = 0; i < width; i += d ) {
                    int v = fractalOperator.apply( hscale( i, width, scale, x0 ), vscale( j, height, scale, y0 ), 40 );

                    int r = v == 40 ? 0 : (int) ( v * 0xFF / 40.0 );
                    int g = v == 40 ? 0 : (int) ( 255 * Math.log( v + 1 ) / Math.log( 41 ) );
                    // int g = (v == 40)?0:(int)(255 * Math.pow(v,0.5)/6.325);
                    int b = v == 40 ? 0 : (int) ( v * 0xFF / 40.0 );

                    mBitmapPaint.setARGB( 0xFF, r, g, b );
                    if ( d == 1 ) {
                        canvas.drawPoint( i, j, mBitmapPaint );
                    } else {
                        canvas.drawRect( i, j, i + d, j + d, mBitmapPaint );
                    }
                    if ( isCancelled() ) {
                        Log.d( "MandelbrotView", "forced stop..." );
                        return;
                    }
                }
            }
        }
    }

}
