Release notes for RoboSpice
===========================

Since release 1.4.0 of RoboSpice, each module has its own release cycle.
This changelog file won't be maintained anymore. Each module will have its own CHANGELOG file.

Version 1.4.0 (February 8 2013)
-------------

Enhancements : 

* Riccardo Ciovati joined the team.
* Added removeAllDataFromCache overload in spice manager : thx to Kaan Meralan
* Added retrofit module with tests and samples
* Enhanced cancel mechanism
* Enhanced core testing and extension testing.
* Added first UI module in RoboSpice to create ListViews using RoboSpice.
* CacheKey is now of type Object and not String anymore. Thx to Fernando Franco Gíraldez for his pull request.
* Added dedicated services to each module. They provide all configurations out of the box for processing web services and caching.
* Enhanced NetworkStateChecker interface in order to refine permission checking and allow real processing of request that
are not related to networking.
* Peer review of multi tasking aspects of RoboSpice by Henri Tremblay.
* Hosted on Sonar's Nemo.

Documentation : 
* Added documentation on Wiki pages for Advanced RoboSpice usages and FAQ, samples, maven and non maven setup, software design, contributors, etc.
* Added samples for most of the RoboSpice extensions.
* Added maven site
* Added repository branch on GitHub to assist non maven users to setup RoboSpice.
* Enhanced javadocs

Bug fix :

* Minor bugs of undesired aggregation of offline requests solved. Thanks to djusv.
* Minor bugs solved in persistence module.
* Minor bug in GsonObjectPersister solved. Thx to Alessio Bianchi. 

For contributors :
 
* Completely reviewed maven structure. Introduction of an easier extension mechanism and simpler samples. Thx to pommedeterresauté
* All project layouts inside RoboSpice are mavenized
* Added wiki pages for contributors, common tasks and so on.


Version 1.3.1 (december 2012)
-------------

* Bug fix release, listeners where not detached properly

Version 1.3.0 (december 2012)
-------------

* Added Google Http Java Client support via the robospice-google-http-client-module
* Added Google Http Java Client persistence module for Json (Xml support doesn't provide a generator and can't be added : see issue http://code.google.com/p/google-http-java-client/issues/detail?id=171&thanks=171&ts=1354436504)
* Dependency conflicts solved : excluded xpp3 dependency, updated jackson version
* Spring Android module classes have been renamed to make room for other REST clients implementations.
* Jerome Van Der Linden removed guava dependency, introduced Apache Commons dependency. 
* Added tests for getAllDataFromCache on persisters
* Added test for ormlite module
* Solved listener notification bug. Thx to Vincent Lemeunier 
* Refactored request cancelation mechanism. Thx to Philippe Prados for help with handlers.
* Added tests for request cancelation mechanism.
* RoboSpice reached the milestone of 103 tests. This is also a QA release.


Version 1.2.0 (november 12 2012)
-------------

* Added network stating indirection in RequestProcessor. Thx to Pierre Durand
* Added a check in request classes to verify that requests are not inner classes of activities
as this would create a memory leak. Thx to Cyril Mottier.
* fixed bug in InFileObjetPersister. Thx to florianmski
* fixed bug in json and xml persisters
* Added json gson and jackson and xml simple serializer tests.
* Added network state test.
* Added multiple object persister test.

Version 1.1.1 ( released ?)
-------------

Bug fix :
* enhanced SpiceManager cpu usage. There used to be a bug in case a service had no request to process that boosted cpu usage.
Thx to Riccardo Ciovati.
* enhanced ORM Lite support : tables are created fully dynamically for all POJOs saved via the ORMLiteObjectPersisterFactory.
No need to provide a specific factory for every class of IDs used by persisted POJOs.
* better RoboSpice Motivations icon
* Added RoboSpice Motivations to Google Play

Version 1.1.0 (released for DroidCon London, October 24th 2012)
-------------

* Added RoboSpice Motivations to repo as a RoboSpice demo app and in-depth argumentation for RoboSpice existence.
* Added RoboSpice Motivations to the Google Play store.
* Added Json Serialization support using Gson (Jackson is also supported since V1.0)
* Added Xml Serialization support using SimpleXMLSerializer
* Added Orm Serialization support using OrmLite
* Permission check : applications must declare both INTERNET and NETWORK_STATE permission.
* Added getFromCache method in SpiceManager to query cache content.
* Added progress monitoring for requests (both status and progress percent).
* Added common foreground service for all SpiceManagers using the same service class.
   * Requests are now completely decoupled from Activity life cycle.
   * Service stops when no more requests are active and no SpiceManager is bound.
* Added methods to get data in cache if present
* Added methods to add listeners to a pending request if present.
* Added support for creating notifications for a request through a new service helper.
* Allow to cancel pending requests from new Activity. 
* All logs in RoboSpice now use a downgraded version of RoboGuice's logging facility : 
http://code.google.com/p/roboguice/wiki/Logging
* Added the ability to provide a custom executor service to the SpiceService's RequestProcessor. Thx to Riccardo Ciovati.
* Added RoboSpice presentation in the download area of Git Hub
* Added all RoboSpice related gfx to GitHub repo.
* Added google discussion group
* Added Starter Guide on Git hub Wiki

Version 1.0.0
-------------

* Name changed from Content Manager to RoboSpice
* published on Github
* published on Maven Central 

Version 0.0.13
--------------

* minor enhancement of simple requests, exception stack traces were lost.

Version 0.0.12
--------------

* back to previous dependency injection model inside services.
* content manager is now more robust and more stable
* dropped the idea of integrating AsyncTasks into the framework.
* minor enhancements.


Version 0.0.11
--------------

* configuration is now entirely mavenized. No libs folder anymore, every dependency is controlled by maven.
Most dependencies are standard although some of the android dependencies are still missing in maven, in that case,
 developpers will have to create them through mvn import-file goal.
content-manager-it, the test app can't be used under eclipse yet (as the android configurator is not ready yet) : devs should disable maven nature and add the sample
as a project in the test app java build path. 
* various attempts have been made to enable canceling a spring android request but that failed. It's not possible and won't be possible in a near future to cancel a request
that has started its network connection. That's a real concern but spring android doesn't support this feature yet.
* mostly released for Samy to give content manager a circuit run.

Version 0.0.10
--------------

* revisited maven configuration for deployment

Version 0.0.9
-------------

* life cycle of the ContentManager has improved
* InputStream based requests had a logical problem : they were processing InputStream on the UI Thread, and that is prohibited as of HoneyComb (and a bad practice),
they now process the network stream in the background and offer either a memory stream or file stream to be processed by listeners (who can still delegate the processing
to an asynctask for buttering).
* Injection Dependency changed to some more standard option : developpers will have to create an Application Class that will provide ContentService instances all their
required dependencies.

Version 0.0.8 
-------------

* AndroidManifest check. ContentManager will get sure a ContentService has been declared in manifest

Version 0.0.6
-------------

* changed deploy url

Version 0.0.5
-------------

* async operations for ObjectPersisters are available (and optional)
* persistence layer cleaning, refactoring and renaming
* added BigInputStream and SmallInputStream requests
* added README
* testing 
* more cache cleaning methods
* removed roboguice dependency :(
* split of spring android and json modules

Version 0.0.4
-------------

* first maven and jenkins integrations
* renaming of maven artefacts
* nexus deployment
* local service binding
* removed all bundlization stuff

Version 0.0.1 (June 23rd 2012)
------------------------------

* extracted project from c*t*l*m app
* gitified project
