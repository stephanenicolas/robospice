RoboSpice
=========

RoboSpice is a modular android library that eases the development of Data-Driven Android applications.

RoboSpice
* executes asynchronously (in an AndroidService) network requests that will return POJOs (ex: REST requests)
* cache results (in Json, or Xml, or flat text files, or binary files)
* notifies your activities (or any other context) of the result of the network request if they are still alive
* doesn't notify your activities of the result if they are not alive anymore
* is strongly typed ! 

Here is a small example (extracted from the sample app) : 

````java
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        //a pojo request
        weatherRequest = new WeatherRequest( "75000" );

        //execute a request, 
        //check if the result is not in cache,
        //if it isn't, fetch remote data and cache it
        //each request has it own listener
        getContentManager().execute( weatherRequest, "75000.weather", DurationInMillis.ONE_DAY, new WeatherRequestListener() );        
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

````

IDE Configuration to use RoboSpice in your App
==============================================

* In Eclipse :: simply add the following jars to your libs folder :
** robospice-x.x.x.jar
** robospice-json-x.x.x.jar
** robospice-spring-android-x.x.x.jar
* In Eclipse using Maven Android Plugin :: //TODO
* In IntelliJ :: //TODO

Project Configuration to use RoboSpice
======================================

Once all the jars are part of your project, you need to follow 2 configuration steps : 

1. First, create a subclass of ContentService. 
This is needed to configure your CacheManager (and in the case you are using the spring android module, you will configure your RestTemplate as well).
2. Second, define a few fields and methods inside your base activity class.
This will allow all your activities to use RoboSpice's ContentManager.

Those steps are examplified in the sample of RoboSpice.


IDE Configuration to contribute to RoboSpice
============================================
First, clone the RoboSpice git repo. Then, 
* In Eclipse :: 
    * import the maven projects from your local git repo.
    * change the sample-it project : remove maven nature and add the sample project in the buildpath of sample-it, as a project.
This is due to a bug in current Android Configurator.


