<img src="https://raw.github.com/octo-online/robospice/master/gfx/Robospice-logo-white-background.png" 
width="250px" />

Overview
--------

RoboSpice is a modular android library that makes writing asynchronous network requests *easy*.

To learn more about RoboSpice in 30 seconds, try [this infographics]
(https://raw.github.com/octo-online/robospice/master/gfx/RoboSpice-InfoGraphics.png).

If you want to start using RoboSpice right now, jump to the [Wiki](https://github.com/octo-online/robospice/wiki) or the [samples](https://github.com/octo-online/RoboSpice-samples).


Main features of RoboSpice
--------------------------

* executes network requests **asynchronously** (in a background AndroidService)
* supports **REST** out of the box (using [Spring Android](http://www.springsource.org/spring-android) or [Google Http Client](http://code.google.com/p/google-http-java-client)).
* is strongly typed ! You query webservices using **POJOs** and you get POJOs as request results.
* enforces no constraints neither on POJOs used for requests nor on Activity classes you use in your projects
* **caches results** in Json with both [Jackson](http://jackson.codehaus.org/) or [Gson](http://code.google.com/p/google-gson/),
or [Xml](http://simple.sourceforge.net/), or flat text files, or binary files, even using [ORM Lite](http://ormlite.com/sqlite_java_android_orm.shtml) (still in beta)
* notifies your activities (or any other context) of the result of the network request if and only if they are still alive
* **no memory leaks** at all, like Android Loaders, unlike Android AsyncTasks
* notifies your activities on their **UI Thread**
* uses a simple but **robust** exception handling model
* supports **multi-threading** of request executions
* supports aggregation of different web services
* is a full featured replacement for long running AsyncTasks even they are not related to networking.
* is **open source** ;) 
* and **tested**

* RoboSpice is [under Continuous Integration on a CloudBees server](https://robospice.ci.cloudbees.com/job/Build%20RoboSpice/). Thanks to CloudBees.
* RoboSpice is [under Quality control on Sonar's Nemo instance](http://nemo.sonarsource.org/dashboard/index/504442). Thanks to Sonar Source.

To learn more about RoboSpice
-----------------------------

To learn more, look at the presentation slides we created for DroidCon UK 2012, they are available in the [download section](https://github.com/octo-online/robospice/downloads).

A few links : 

* Browse RoboSpice's full documentation on [the Wiki](https://github.com/octo-online/robospice/wiki).
* [Javadocs](http://octo-online.github.com/robospice/site/latest/apidocs/index.html).
* [Maven site](http://octo-online.github.com/robospice/site/latest/index.html).
* [Discussion group on google](https://groups.google.com/forum/?fromgroups#!forum/robospice).

Example code & demo
-------------------

The RoboSpice team proposes a lot of sample applications in [their own GitHub repo](https://github.com/octo-online/RoboSpice-samples).

We also propose a few demo : 
* [RoboSpice Motivations](http://goo.gl/pzqH4) : a pedagogical app that explains the motivations behind RoboSpice.
* and [a demo that illustrates non-network related requests (offline)](](https://play.google.com/store/apps/details?id=com.octo.android.robospice.sample.offline#?t=W251bGwsMSwxLDIxMiwiY29tLm9jdG8uYW5kcm9pZC5yb2Jvc3BpY2Uuc2FtcGxlLm9mZmxpbmUiXQ..).

A projet initiated by Octo Technology 
-------------------------------------

![Octo Technology logo](https://raw.github.com/octo-online/robospice/master/gfx/octo-ascii-logo-blue.png)

RoboSpice has been incubated at [Octo Technology](http://www.octo.com/en), a french company based in Paris, focused on software design and quality. 
It offers its employees to work part time on Research & Development projects. RoboSpice was one of them.

RoboSpice is the news 
---------------------

RoboSpice has been featured in 
* Android dev weekly mailing list, [issue 31](http://androiddevweekly.com/2012/10/29/Issue-31.html).
* Android weekly mailing list, [issue 44](http://androidweekly.net/).
* [An article of Geeek.org ](http://www.geeek.org/developpement-android-robospice-simplifie-vos-appels-reseau-asynchrone-690.html)

RoboSpice has been presented at : 
* [DroidCon London](http://uk.droidcon.com/), October 2012
* [Nantes GDG Dev Fest, France](http://devfest.gdgnantes.com/?utm_source=3eme%2Bmsg&utm_medium=google-plus&utm_campaign=mailing-devfest), November 2012
* [Paris Android User Group](http://www.paug.fr/actualite-android/conference-presentation-des-librairies-robospice-et-polaris/), November 2012
* [DroidCon Amsterdam](http://www.droidcon.nl/), November 2012

[RoboSpice video at DroidConLondon](http://uk.droidcon.com/sessions/b-track-3/)

License
-------

  Copyright (C) 2012 Octo Technology (http://www.octo.com)
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	     http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
	
Alternatives to RoboSpice 
-------------------------

* [DataDroid](http://www.datadroidlib.com/2012/12/datadroid-v2-is-available)
* [REST Provider](https://github.com/novoda/RESTProvider)
* [REST Droid](https://github.com/PCreations/RESTDroid)
* [PostMan (rings twice) Lib](https://github.com/fedepaol/PostmanLib--Rings-Twice--Android)
