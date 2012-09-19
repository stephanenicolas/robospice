RoboSpice
=========

RoboSpice is a modular android library that eases the development of Data-Driven Android applications.

RoboSpice
* executes asynchronously network requests that will return Pojos (ex: REST requests)
* cache results (in Json, or Xml, or flat text files, or binary files)
* notifies your activities (or any other context) of the result of the network request if they are still alive
* doesn't notify your activities of the result if they are not alive anymore
* is strongly typed ! 

Here is a small example : 

````java
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        //a simple string request
        loremRequest = new SimpleTextRequest( "http://www.loremipsum.de/downloads/original.txt" );
        //a pojo request
        weatherRequest = new WeatherRequest( "75000" );
        //a binary request, here, a drawable
        imageRequest = new SmallBinaryRequest( "http://earthobservatory.nasa.gov/blogs/elegantfigures/files/2011/10/globe_west_2048.jpg" );

        //execute requests, check if the result is not cache, if it isn't, fetch remote data and cache it
        //each request has it own listener
        getContentManager().execute( loremRequest, "lorem.txt", DurationInMillis.ONE_DAY, new LoremRequestListener() );
        getContentManager().execute( weatherRequest, "75000.weather", DurationInMillis.ONE_DAY, new WeatherRequestListener() );
        getContentManager().execute( imageRequest, "logo", DurationInMillis.ONE_DAY, new ImageRequestListener() );
        
}

    // ============================================================================================
    // INNER CLASSES
    // ============================================================================================


    public final class LoremRequestListener implements RequestListener< String > {

        @Override
        public void onRequestFailure( ContentManagerException contentManagerException ) {
            Toast.makeText( SampleContentActivity.this, "failure", Toast.LENGTH_SHORT ).show();
        }

        @Override
        public void onRequestSuccess( final String result ) {
            Toast.makeText( SampleContentActivity.this, "success", Toast.LENGTH_SHORT ).show();
            String originalText = mLoremTextView.getText().toString();
            mLoremTextView.setText( originalText + result );
        }
    }

    public final class WeatherRequestListener implements RequestListener< WeatherResult > {

        @Override
        public void onRequestFailure( ContentManagerException contentManagerException ) {
            Toast.makeText( SampleContentActivity.this, "failure", Toast.LENGTH_SHORT ).show();
        }

        @Override
        public void onRequestSuccess( final WeatherResult result ) {
            Toast.makeText( SampleContentActivity.this, "success", Toast.LENGTH_SHORT ).show();
            String originalText = mCurrentWeatherTextView.getText().toString();
            mCurrentWeatherTextView.setText( originalText + result.toString() );
        }
    }

    public final class ImageRequestListener implements RequestListener< InputStream > {

        @Override
        public void onRequestFailure( ContentManagerException contentManagerException ) {
            Toast.makeText( SampleContentActivity.this, "failure", Toast.LENGTH_SHORT ).show();
        }

        @Override
        public void onRequestSuccess( final InputStream result ) {
            Bitmap bitmap = BitmapFactory.decodeStream( result );
            BitmapDrawable drawable = new BitmapDrawable( bitmap );
            Toast.makeText( SampleContentActivity.this, "success", Toast.LENGTH_SHORT ).show();
            mImageTextView.setBackgroundDrawable( drawable );
            mImageTextView.setText( "" );
        }
    }
````
