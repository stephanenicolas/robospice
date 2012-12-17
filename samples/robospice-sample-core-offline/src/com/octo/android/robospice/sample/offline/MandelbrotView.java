package com.octo.android.robospice.sample.offline;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.sample.model.FractalOperator;
import com.octo.android.robospice.sample.model.OptimizedMandelbrotOperator;
import com.octo.android.robospice.sample.request.FractalRequest;

/**
 * From
 * http://www.java2s.com/Open-Source/Android/UnTagged/android-mandelbrot-set/org/girino/frac/android/MandelbrotView.
 * java.htm
 * 
 * @author sni
 */
public class MandelbrotView extends ImageView implements OnTouchListener {
    private static final int ROW_HEIGHT = 32;

    private static final int VERTICAL_MARGIN = 50;

    private static final String TAG = "Touch";

    private double x0 = 0.0;
    private double y0 = 0.0;
    private int width = 320;
    private int height = 480;
    private double scale = 100.0 * 300.0 / width;
    private float xoffset = 0;
    private float yoffset = 0;

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint;

    // The 3 states (events) which the user is trying to perform
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // these PointF objects are used to record the point(s) the user is touching
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;
    private float scaleFactor = 1.0f;

    private FractalOperator fractalOperator = new OptimizedMandelbrotOperator();

    private SpiceManager spiceManager;

    private double oldScale = scale;

    private SpiceRequest< Bitmap > fractalRequest;

    public MandelbrotView( Context context, AttributeSet attrs, int defStyle ) {
        super( context, attrs, defStyle );
        initialize();
    }

    public MandelbrotView( Context context, AttributeSet attrs ) {
        super( context, attrs );
        initialize();
    }

    public MandelbrotView( Context context ) {
        super( context );
        initialize();
    }

    private void initialize() {
        rescale( getWidth(), getHeight() );
        mBitmapPaint = new Paint( Paint.DITHER_FLAG );

        // draws first mandelbrot
        mCanvas.drawARGB( 0xff, 10, 10, 10 );

        setOnTouchListener( this );
    }

    public void setSpiceManager( SpiceManager spiceManager ) {
        this.spiceManager = spiceManager;
    }

    private void rescale( int w, int h ) {
        w = w > 0 ? w : 1;
        width = w;
        scale *= (double) w / (double) width;
        height = h > 0 ? h : 1;

        mBitmap = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888 );
        mCanvas = new Canvas( mBitmap );
    }

    public void start() {
        Log.d( "MandelbrotView", "starting..." );
        fractalRequest = new FractalRequest( x0, y0, width, height, scale, fractalOperator, mBitmap );

        // spiceManager.execute( fractalRequest, "fractal", DurationInMillis.ALWAYS, new FractalResultListener() );
        spiceManager.execute( fractalRequest, new FractalResultListener() );
        invalidate();
        Log.d( "MandelbrotView", "started..." );
    }

    public void stop() {
        Log.d( "MandelbrotView", "stoping..." );
        if ( fractalRequest != null ) {
            fractalRequest.cancel();
        }
        Log.d( "MandelbrotView", "stoped..." );
    }

    @Override
    protected void onSizeChanged( int w, int h, int oldw, int oldh ) {
        Log.d( "MandelbrotView", "onSizeChanged begin..." );
        Log.d( "MandelbrotView", "Size: " + w + "x" + h );
        super.onSizeChanged( w, h, oldw, oldh );
        stop();
        rescale( w, h );
        start();
        Log.d( "MandelbrotView", "onSizeChanged end..." );
    }

    @Override
    protected void onDraw( Canvas canvas ) {
        // avoids drawing too much
        canvas.drawBitmap( mBitmap, xoffset, yoffset, mBitmapPaint );
        Paint paintBlue = new Paint();
        paintBlue.setStyle( Paint.Style.STROKE );
        paintBlue.setStrokeWidth( 3 );
        paintBlue.setTextSize( ROW_HEIGHT );
        paintBlue.setColor( Color.WHITE );
        canvas.drawText( "X:" + x0, 10, VERTICAL_MARGIN, paintBlue );
        canvas.drawText( "Y:" + y0, 10, VERTICAL_MARGIN + 32, paintBlue );
        canvas.drawText( "scale:" + scale, 10, VERTICAL_MARGIN + 32 * 2, paintBlue );

        if ( mode == ZOOM ) {
            float w = width / scaleFactor;
            float h = height / scaleFactor;
            Paint paint = new Paint();
            paint.setStyle( Paint.Style.STROKE );
            paint.setStrokeWidth( 3 );
            paint.setTextSize( ROW_HEIGHT );
            paint.setColor( Color.RED );
            canvas.drawCircle( mid.x, mid.y, Math.min( w, h ), paint );
            canvas.drawCircle( mid.x, mid.y, 5, paint );
            canvas.drawText( "Scale:" + scale * scaleFactor, width / 2, height * 0.95f, paint );
            canvas.drawText( "Mid:" + mid.x + "," + mid.y, width / 2, height * 0.95f + ROW_HEIGHT, paint );
        }
        // stops only after drawing a last time
        invalidate();
    }

    @Override
    public boolean onTouch( View v, MotionEvent event ) {
        ImageView view = (ImageView) v;
        view.setScaleType( ImageView.ScaleType.MATRIX );
        dumpState();
        dumpEvent( event );
        // Handle touch events here...

        switch ( event.getAction() & MotionEvent.ACTION_MASK ) {
            case MotionEvent.ACTION_DOWN: // first finger down only
                start.set( event.getX(), event.getY() );
                Log.d( TAG, "mode=DRAG" ); // write to LogCat
                mode = DRAG;
                break;

            case MotionEvent.ACTION_UP: // first finger lifted

            case MotionEvent.ACTION_POINTER_UP: // second finger lifted
                if ( mode == ZOOM ) {
                    stop();
                    zoom( scaleFactor );
                    moveTo( mid.x, mid.y );
                    scaleFactor = 1.0f;
                    start();
                } else if ( mode == DRAG ) {
                    stop();
                    moveBy( event.getX(), event.getY() );
                    Log.d( TAG, "x0=" + x0 );
                    Log.d( TAG, "y0=" + y0 );
                    start();
                }

                mode = NONE;
                Log.d( TAG, "mode=NONE" );
                break;

            case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down

                oldDist = spacing( event );
                Log.d( TAG, "oldDist=" + oldDist );
                if ( oldDist > 5f ) {
                    mode = ZOOM;
                    oldScale = scale;
                    Log.d( TAG, "mode=ZOOM" );
                }
                break;

            case MotionEvent.ACTION_MOVE:

                if ( mode == DRAG ) {
                    // in the matrix of points
                } else if ( mode == ZOOM ) {
                    // pinch zooming
                    float newDist = spacing( event );
                    Log.d( TAG, "newDist=" + newDist );
                    if ( newDist > 5f ) {
                        midPoint( mid, event );
                        scaleFactor = newDist / oldDist; // setting the scaling of the
                    }
                }
                break;
        }

        return true; // indicate event was handled
    }

    private void moveTo( float x, float y ) {
        x0 += (float) ( ( x - width / 2 ) / scale );
        y0 += (float) ( ( y - height / 2 ) / scale );
    }

    private void moveBy( float x, float y ) {
        float translateX = (float) ( ( x - start.x ) / scale );
        float translateY = (float) ( ( y - start.y ) / scale );
        x0 -= translateX;
        y0 -= translateY;
    }

    /*
     * -------------------------------------------------------------------------- Method: spacing Parameters:
     * MotionEvent Returns: float Description: checks the spacing between the two fingers on touch
     * ----------------------------------------------------
     */

    private float spacing( MotionEvent event ) {
        float x = event.getX( 0 ) - event.getX( 1 );
        float y = event.getY( 0 ) - event.getY( 1 );
        return FloatMath.sqrt( x * x + y * y );
    }

    /*
     * -------------------------------------------------------------------------- Method: midPoint Parameters: PointF
     * object, MotionEvent Returns: void Description: calculates the midpoint between the two fingers
     * ------------------------------------------------------------
     */

    private void midPoint( PointF point, MotionEvent event ) {
        float x = event.getX( 0 ) + event.getX( 1 );
        float y = event.getY( 0 ) + event.getY( 1 );
        point.set( x / 2, y / 2 );
    }

    /** Show an event in the LogCat view, for debugging */
    private void dumpEvent( MotionEvent event ) {
        String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE", "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append( "event ACTION_" ).append( names[ actionCode ] );

        if ( actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP ) {
            sb.append( "(pid " ).append( action >> MotionEvent.ACTION_POINTER_ID_SHIFT );
            sb.append( ")" );
        }

        sb.append( "[" );
        for ( int i = 0; i < event.getPointerCount(); i++ ) {
            sb.append( "#" ).append( i );
            sb.append( "(pid " ).append( event.getPointerId( i ) );
            sb.append( ")=" ).append( (int) event.getX( i ) );
            sb.append( "," ).append( (int) event.getY( i ) );
            if ( i + 1 < event.getPointerCount() ) {
                sb.append( ";" );
            }
        }

        sb.append( "]" );
        Log.d( "Touch Events ---------", sb.toString() );
    }

    /** Show an event in the LogCat view, for debugging */
    private void dumpState() {
        StringBuilder sb = new StringBuilder();
        sb.append( "x0=" + x0 ).append( "\ny0=" + y0 );
        sb.append( "\nscale=" + scale ).append( "\noldScale=" + oldScale );
        sb.append( "\nscaleFactor=" + scaleFactor );
        sb.append( "\nmidPoint=" + mid.x + " " + mid.y );
        Log.d( "Fractal state ---------", sb.toString() );
    }

    public void zoom( double factor ) {
        scale *= factor;
        Log.d( TAG, "Zooming factor :" + factor );
    }

    public void reset() {
        stop();
        x0 = 0;
        y0 = 0;
        scale = 100.0 * 300.0 / width;
        start();
    }

    private class FractalResultListener implements RequestListener< Bitmap > {

        @Override
        public void onRequestFailure( SpiceException spiceException ) {

        }

        @Override
        public void onRequestSuccess( Bitmap result ) {
            mBitmap = result;
            invalidate();
        }

    }

}
