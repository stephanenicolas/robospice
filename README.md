RoboSpice
=========

Overview
--------

RoboSpice is a modular android library that eases the development of Data-Driven Android applications.

RoboSpice is [maven-ready](http://search.maven.org/#search%7Cga%7C1%7Crobospice).

Main features of RoboSpice
--------------------------

* executes asynchronously (in a background AndroidService) network requests that will return POJOs (ex: REST requests)
* caches results (in Json, or Xml, or flat text files, or binary files)
* notifies your activities (or any other context) of the result of the network request if they are still alive
* doesn't notify your activities of the result if they are not alive anymore
* notifies your activities on their UI Thread
* uses a simple but robust exception handling model
* supports multiple ContentServices to aggregate different web services results
* supports multi-threading of request executions
* is strongly typed ! 
* is open source ;) 
* and tested

Example code
------------

Here is a small example (a snippet from the sample app) : 

````java
    // ============================================================================================
    // ACTIVITY CLASS, can subclass any class, just a few line of boiler plate code to add to your common super class
    // could be a Service, an Application, any context.
    // ============================================================================================
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        //a pojo request
        weatherRequest = new WeatherRequest( "75000" );

        //execute a request, 
        //check if the result is in cache with a maximum expiry of 24 hours,
        //if it isn't, fetch remote data and cache it
        //each request has its own listener (see below)
        getContentManager().execute( weatherRequest, "75000.weather", DurationInMillis.ONE_DAY, new WeatherRequestListener() );        
    }

````


And the request class for the POJO simply looks like : 

```java
    // ============================================================================================
    // REQUEST CLASS, fully testable, will be executed in background, 
    // in a context that is independent of the activity
    // ============================================================================================

public final class WeatherRequest extends RestContentRequest< WeatherResult > {

    private String baseUrl;

    public WeatherRequest( String zipCode ) {
        super( WeatherResult.class );
        this.baseUrl = String.format( "http://www.myweather2.com/developer/forecast.ashx?uac=AQmS68n6Ku&query=%s&output=json", zipCode );
    }

    @Override
    public WeatherResult loadDataFromNetwork() throws RestClientException {
        //use a spring android RestTemplate to query web services and obtain the response as a POJO
        return getRestTemplate().getForObject( baseUrl, WeatherResult.class );
    }

}

```

And the request listener class are implemented as inner classes of the activity class : 

```java
    // ============================================================================================
    // INNER CLASS of the activity, notified on UI Thread, if and only if your activity is alive
    // ============================================================================================

    public final class WeatherRequestListener implements RequestListener< WeatherResult > {

        @Override
        public void onRequestFailure( ContentManagerException contentManagerException ) {
            //get notified of request's failure via a detailed exception
            Toast.makeText( SampleContentActivity.this, "failure", Toast.LENGTH_SHORT ).show();
        }

        @Override
        public void onRequestSuccess( final WeatherResult result ) {
            //get notified of request's success and access the result as POJO.
            Toast.makeText( SampleContentActivity.this, "success", Toast.LENGTH_SHORT ).show();
            String originalText = mCurrentWeatherTextView.getText().toString();
            mCurrentWeatherTextView.setText( originalText + result.toString() );
        }
    }

````


For more information, browse the source code of the sample project. Also, browse [RoboSpice Javadocs online](http://octo-online.github.com/robospice/apidocs/index.html).
For developpers
===============

IDE & Build Tools configuration to use RoboSpice in your App
------------------------------------------------------------

* In Eclipse :: simply add the following jars to your libs folder (availlable in [downloads](https://github.com/octo-online/robospice/downloads)) :
    * robospice-x.x.x.jar
    * robospice-json-x.x.x.jar
    * robospice-spring-android-x.x.x.jar
* Using Maven Android Plugin :: //TODO (RoboSpice is maven-ready, we need to publish on Maven Central Repo)
* In IntelliJ :: //TODO

Project Configuration to use RoboSpice
--------------------------------------

Once all the jars are part of your project, you need to follow 2 configuration steps : 

1. First, create a subclass of ContentService. 
This is needed to configure your CacheManager (and in the case you are using the spring android module, you will configure your RestTemplate as well).
2. Second, define a few fields and methods inside your base activity class.
This will allow all your activities to use RoboSpice's ContentManager.

Those steps are examplified in the sample of RoboSpice.

For contributors
================

IDE Configuration to contribute to RoboSpice
--------------------------------------------

First, clone the RoboSpice git repo. Then, 
* In Eclipse :: 
    * import the maven projects from your local git repo.
    * change the sample-it project : remove maven nature and add the sample project in the buildpath of sample-it, as a project.
This is due to a bug in current Android Configurator.


