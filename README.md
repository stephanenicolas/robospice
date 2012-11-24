![RoboSpice logo](https://raw.github.com/octo-online/robospice/master/gfx/Robospice-logo-white-background.png)

Overview
--------

RoboSpice is a modular android library that makes writing asynchronous network requests *easy*.

To learn more about RoboSpice in 30 seconds, try this infographics : 
https://raw.github.com/octo-online/robospice/master/gfx/RoboSpice-InfoGraphics.png

RoboSpice is [maven-ready](http://search.maven.org/#search%7Cga%7C1%7Crobospice).
For non-Maven developers, follow [this guide](https://github.com/octo-online/robospice/wiki/Using-RoboSpice-without-Maven) to setup RoboSpice in your app.

Join our [discussion group on google](https://groups.google.com/forum/?fromgroups#!forum/robospice).
Browse [RoboSpice Wiki](https://github.com/octo-online/robospice/wiki).

Main features of RoboSpice
--------------------------

* executes asynchronously (in a background AndroidService) network requests
* supports REST out of the box (using [Spring Android](http://www.springsource.org/spring-android) or [Google Http Client](http://code.google.com/p/google-http-java-client)).
* is strongly typed ! You make your requests using POJOs and you get POJOs as request results.
* enforce no constraints neither on POJOs used for requests nor on Activity classes you use in your projects
* caches results in Json with both [Jackson](http://jackson.codehaus.org/) and [Gson](http://code.google.com/p/google-gson/), 
or [Xml](http://simple.sourceforge.net/), or flat text files, or binary files, even using [ORM Lite](http://ormlite.com/sqlite_java_android_orm.shtml) (still in beta)
* notifies your activities (or any other context) of the result of the network request if and only if they are still alive
* no memory leaks at all, like Android Loaders, unlike Android AsyncTasks
* notifies your activities on their UI Thread
* uses a simple but robust exception handling model
* supports aggregation of different web services
* supports multi-threading of request executions
* since version 1.2, RoboSpice is a full featured replacement for long running AsyncTasks even they are not related to networking.
* is open source ;) 
* and tested

Look at the presentation slides we created for DroidCon UK 2012, they are available in the [download section](https://github.com/octo-online/robospice/downloads).

Example code & demo
-------------------

You can find complete examples of RoboSpice usage in the application (included in Git Hub's repo) : RoboSpice Motivations.
You can also find the apk of the RoboSpice Motivations app in the [download section](https://github.com/octo-online/robospice/downloads) and on the [Google Play Store](http://goo.gl/pzqH4).

Read the [RoboSpice starter guide](https://github.com/octo-online/robospice/wiki) to learn more.


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