![RoboSpice logo](https://raw.github.com/octo-online/robospice/master/gfx/Robospice-logo-white-background.png)

Overview
--------

RoboSpice is a modular android library that makes writing asynchronous network requests *easy*.

RoboSpice is [maven-ready](http://search.maven.org/#search%7Cga%7C1%7Crobospice).

Join our [discussion group on google](https://groups.google.com/forum/?fromgroups#!forum/robospice)

Main features of RoboSpice
--------------------------

* executes asynchronously (in a background AndroidService) network requests that will return POJOs (ex: REST requests using [Spring Android](http://www.springsource.org/spring-android))
* caches results (in Json with both [Jackson](http://jackson.codehaus.org/) and [Gson](http://code.google.com/p/google-gson/), 
or Xml, or flat text files, or binary files, even using [ORM Lite (still in beta)](http://ormlite.com/sqlite_java_android_orm.shtml))
* notifies your activities (or any other context) of the result of the network request if they are still alive
* doesn't notify your activities of the result if they are not alive anymore
* notifies your activities on their UI Thread
* uses a simple but robust exception handling model
* supports multiple ContentServices to aggregate different web services results
* supports multi-threading of request executions
* is strongly typed ! 
* enforce no constraints neither on POJOs used for requests nor on Activity classes you use in your projects
* is open source ;) 
* and tested

Look at the presentation slides we created for Droid Con, they are available in the [download section](https://github.com/octo-online/robospice/downloads)

Example code & demo
-------------------

You can find complete examples of RoboSpice usage in the application (included in Git Hub's repo) : RoboSpice Motivations.

You can also find the apk of the RoboSpice Motivations app in the [download section](https://github.com/octo-online/robospice/downloads) and on the Google Play Store soon.

Read the [RoboSpice starter guide](https://github.com/octo-online/robospice/wiki) to learn more.




