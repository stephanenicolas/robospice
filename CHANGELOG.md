Release notes for RoboSpice
===========================

Version 1.0.0
-------------

* Name changed from Content Manager to RoboSpice
* published on Github
* asked for Sonatype / Maven Central artefact hosting

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
