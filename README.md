RoboSpice
=========

RoboSpice is a modular android library that eases the development of Data-Driven Android applications.

RoboSpice
* executes asynchronously network requests that will return Pojos (ex: REST requests)
* cache results (in Json, or Xml, or flat text files, or binary files)
* notifies your activities (or any other context) of the result of the network request if they are still alive
* doesn't notify your activities of the result if they are not alive anymore
* is strongly typed ! 

Here is a small example (extracted from the sample app) : 

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

````


And the request class for the Pojo simply looks like : 

```java
public final class WeatherRequest extends RestContentRequest< WeatherResult > {

    private String baseUrl;

    public WeatherRequest( String zipCode ) {
        super( WeatherResult.class );
        this.baseUrl = String.format( "http://www.myweather2.com/developer/forecast.ashx?uac=AQmS68n6Ku&query=%s&output=json", zipCode );
    }

    @Override
    public WeatherResult loadDataFromNetwork() throws RestClientException {
        Log.d( getClass().getName(), "Call web service " + baseUrl );
        return getRestTemplate().getForObject( baseUrl, WeatherResult.class );
    }

}

```

And the request listener classes are implemented as inner classes of the activity class : 

```java
    // ============================================================================================
    // INNER CLASSES of the activity
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


