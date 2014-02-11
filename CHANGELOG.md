Release notes for RoboSpice
===========================

Version 1.5.0 (planned)
-------------

* TODO : finish migrating samples to gralde and release.
* TODO : inject requests in listeners
* TODO : inject service in requests

Version 1.4.12 (planned Feb 2014)
--------------

Enhancements

* Updated Retrofit extension to use Retrofit version 1.4.1

Bug fixes

* Bug fix : add proper synchronization to DefaultRequestProcessor stop/execute
* Fix a few bugs in spice list: when network was off, thumbnails were reloaded incorrectly.

Version 1.4.11 (January 16th 2014)
--------------

Enhancements

* Refactor of RoboSpice core dependencies. Thx to Isuski. https://github.com/octo-online/robospice/issues/245
* General maven plugin and dependencies upgrade.
* Correct api targetting problem for executors in ui spicelist.

Bug fixes 

* Request dropped when stopping the SpiceManager. Thx to Dorian Cussen. https://github.com/octo-online/robospice/issues/246 
* Bug in RequestProcessor stop mechanism. Thx to Isuski. https://github.com/octo-online/robospice/issues/244
* Minor Javadoc enhancements

Version 1.4.10 (January 9th 2014)
--------------

Enhancements 

* upgrade to maven android plugin version 3.8.1
* spicemanager: always use isStarted() when checking if started. Pull request #214. Thx to Giorgos Kylafas for fixing the bug. https://github.com/octo-online/robospice/pull/214 
* Updated android-maven-plugin to fix build. Pull request #235. Thx to Tanner Perrien for fixing he bug. https://github.com/octo-online/robospice/pull/235
* Add retrofit jackson 2 converter. Merged pull request Vlad omihaz : https://github.com/octo-online/robospice/pull/239
* Update all dependencies. Fix issue #238. Jackson dependency for spring android will remain 2.2.3. Jackson 2.3.1 needs Android SDK 9+.
* Optimized RoboSpice UI Spice List. Thanks to Cyril Mottier for pushing this. 

Bug fixes

* Bug fix : NPE in execute. Fix issue #221. Thx to Daniel Novak.
* Bug fix : Incorrect behaviour in addListenerIfPending. Fix issue #215. Thx to Isuski
* Bug fix : Fixed context leak in the event that the manager is not stopped. Pull request #233. Thx to Tanner Perrien.
* Bug fix : Many threads blocked in PausablePriorityBlockingQueue.take. Fix issue #220. Thx to Nicolas Poirier.
* Bug fix : ConcurrentModificationException in RequestProcessor. Fix issue 91. Pull request #242. Thx to softwaremaverick and Fernando Franco Gíraldez.

Version 1.4.9 (October 23rd 2013)
-------------

* Issue #200. App crash "Bad notification for startForeground". Thx to Mathieu-Castets, DanielNovak and Giorgos Kylafas for reporting, debugging and testing.

Version 1.4.8 (October 10th 2013)
-------------
* Issue #195. Remove all library support dependencies for easier setup. Thx to Bogdan Zurac for reporting the issue.
* Issue #192. A method has been made static in Google Http Java Client module. Thx to Vselod Ivanov for its patch.
* Issue #191. In some cases, requests where not aggregated properly. Thx to Vselod Ivanov its patch.

* Internal feature : RS tests are faster.

Version 1.4.7 (September 21th 2013)
-------------

** Enhancements
* New OkHttp module that allows to use OkHttpClient inside a RoboSpice request.
* Notifications now work correctly on Android 4.3 when service is going to background. Thx to Andrew Clark and others for this.
* New SpiceService monitoring system initiated by Andrew Clark. A new sample has been added : robospice-sample-core-monitor to demonstrate how to use this feature.

** Bug fixes
* Documentation has been updated in RS Starter Guide thanks to Mathieu Castets.
* removeDataFromCache now returns a future for possible sync. Thanks to sergej-koscejev for the fix in pull request https://github.com/octo-online/robospice/pull/185
* Better handling of aggregation in SpiceManager that caused a memory leak with many identical SpiceRequests. Thanks to seva-ask. Pull request https://github.com/octo-online/robospice/pull/182
* Issue #189 by Vsevolod Ivanov (seva ask): fix for fast switching. https://github.com/octo-online/robospice/pull/189
* A NPE could be sent when notifying listeners of a request not found when screen was turned on. 

Version 1.4.6 (July 18th 2013)
-------------

** Enhancements
* Google Http Client module now has pre-set services for Jackson,Jackson2 and Gson.
* Complete refactor of the core of RoboSpice : Request processor has now been split into distinct entities. Thx to Andrew Clark. pull request #160
* addListenerIfPending can now notify when no pending request has been found. Thx to Andrew Clark. pull request #160
* Fix RoboSpiceContentProvider to declare single item URI type as ITEM rather than DIRECTORY. Thx to Joel Steres. Pull request #151
* Remove constraint that ormlite extension cacheKey type matches POJO id type. Thx to Joel Steres. Pull request #145
* Update of the starter guide on GitHub. Issue https://github.com/octo-online/robospice/issues/162. Thks to Bobby-Jackson for this fix.
* Remove code duplication from RetrofitObjectPersister.loadDataFromCache and instead use readCacheDataFromFile. https://github.com/octo-online/robospice/pull/138. Thx to Jasco.
* Gradle support, demoed in RoboSpice samples. 
* Fix argument names in RoboSpiceDatabaseHelper.onUpdate. https://github.com/octo-online/robospice/pull/146. Thx to Joel Jasco for this fix.
* Fixed exception when writing to cache with an uncached service. https://github.com/octo-online/robospice/pull/135. Thx to Andrew Clark.
* DefaultRetryPolicy getDelayBeforeRetry returns fixed value of 0. https://github.com/octo-online/robospice/issues/129. Thx to Joel Jasco
* EasyMock 3.2 is used in RS 1.4.6, making the interface ICacheManager obsolete.
* General upgrades of dependencies

** Bug fixes
* Spelling correction in logs by Joel Steres
* Pull requests 134 : https://github.com/octo-online/robospice/pull/134 by Andrew Clark
* Pull requests 140 : https://github.com/octo-online/robospice/pull/140 by Andrew Clark
* SpiceManager could not be re-started properly. Bug fixed by issue https://github.com/octo-online/robospice/issues/128, thx to Bobby Jackson.
* Remove RequestCancellationListener after requests https://github.com/octo-online/robospice/pull/148. Thx to Michael Greifeneder
* FIX simple binary/text/bitmap requests indicated success completion when file not found orconnection failed (Issue 126). Thx to Andrew Clark

Version 1.4.5 (June 24th 2013)
-------------
** Enhancements
* Enhanced threading model for the SpiceManager and SpiceService. Decreased priority to increase UI Thread performance and 
enhance user experience.
* Add isDataInCache and getDateOfDataInCache to spiceManager. Thx to Onyx Mueller and Mathieu Castets for suggesting this feature.
* BitmapRequests, SimpleTextRequests have non final methods to allow developpers to extend them
* SpiceList APIs have been changed to allow multiple images per cell view in a list. Thx to Andrea Altobelli for submitting the feature.

** Bug fixes
* Cache folder creation is now thread safe.
* Correct bug of cache removal when there cache folder doesn't exist or is empty.
* SpiceArrayAdapter's getView method is not final any more to allow subclasses to customize adapters (adding sections for instance).
Thanks to Christopher Parola for this suggestion.

Version 1.4.4 (June 4th 2013)
-------------
* Bug fix release: 
Request priorization would fail on SDK 8 due to JDK 5 backward compatibility issues.
This bug is solved. RS is still compatible with Android SDK 8+.

Version 1.4.3 (June 3rd 2013)
-------------
** New module : AndroidORMLiteContentProvider
* Integration with AndroidORMLiteContentProvider
https://github.com/jakenjarvis/Android-OrmLiteContentProvider

** Features :
* RoboSpice drops support for SDK 6 and 7. SDK 8+ required.
* Upgrade to android maven plugin 3.6.0, adt 22, and SDK 17.
* Request priority management is now built into RoboSpice.
Requests with higher priority will get executed first. This is only taken into account for requests that exceed the number of threads of RoboSpice Service, otherwise this is not used.
https://github.com/octo-online/robospice/issues/99
Thanks to Volley for inspiring us on this feature. Richard Hyndman and Nick Butcher suggested us to add this a while ago.. Thx Google ! :)
* Add cachekey sanitation. Sanitation can be used to safely convert strings to cache keys persisted on a file system. Issue https://github.com/octo-online/robospice/issues/97  
* Retry Policy is now built into RoboSpice. Requests default to a non null retry policy that can be customized.
https://github.com/octo-online/robospice/issues/99
* RoboSpice file caching now uses a customizable cache folder to store data. https://github.com/octo-online/robospice/issues/103. Thx to David Sobreira Marques for this feature. 
* new SpiceManager method : putDataInCache, equivalent to putInCache but synchronous.
* Retrofit module's API improved. RetrofitSpiceRequest optimize creation of retrofit services. Thx to Jake Wharton for his feedback on this module.

** Bug fixes : 
* RoboSpice can now clean file cache at startup using removeAllDataFromCache. https://github.com/octo-online/robospice/issues/98.
Thx to David Sobreira Marques for this bug fix.


Version 1.4.2 (May 20th 2013)
-------------
** New module : Retrofit 
* New Retrofit module added to RoboSpice !! It is now very easy to use Retrofit
https://github.com/square/retrofit

** Features :
* New method "putInCache" added to spiceManager. 
Thanks to Christopher Jenkins fur suggesting this feature in https://github.com/octo-online/robospice/issues/75.

** Bugs :
* Spice Manager : getDataFromCache, addToCache and cancel will work even if network is down. This bug was mentionned by dkraus in https://github.com/octo-online/robospice/issues/67
* Content Provider module added to ease creation of ContentProviders backed by an ORMLite Database. (still in beta, waiting for AndroidORMLite to be released on central).

Version 1.4.1 (May 11 2013)
-------------

* RoboSpice core :
** Support for null RequestListener. Thx to chrisjenx. Issue #48
** Issue #62. Thanks to doridori.

* RoboSpice cache : 
** DurationInMillis constants names were confusing (even for contributors !!). We got that clear now.
** Added LRU ObjectPersister from David Stemmer, Mike Jancola
** Added BitmapRequest to ease getting Bitmap data 
** More testing.

* Spring Android module : 
** Add Jackson 2.1 support. Feature suggested by James Campbell.
** Fixed issue https://github.com/octo-online/robospice/issues/80. Thx to Aaron Pickard

* Google Http Java Client for Android module : 
** minor changes

* ORM-Lite module : 
** #Issue 93 solved thanks to Aaron Pickard
** https://github.com/octo-online/robospice/issues/93

* UI SpiceList module :
** UI SpiceList APIs got refactored. The new API is not backward compatible but is much more simple and elegant to implement.
** UI SpiceList now uses the BitmapRequest from the core module to download bitmap and cache them scaled and downsampled.
** Thx to Sergej Koščejev for adding parent parameter in view creation https://github.com/octo-online/robospice/pull/76

A beta version of Retrofit module is also available on the github repo.

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
