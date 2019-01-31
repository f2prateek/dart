Change Log
==========

Version 3.1.2 *(2019-01-31)*
----------------------------
* Use AGP 3.3 API for Task Configuration Avoidance

Version 3.1.1 *(2019-01-17)*
----------------------------
* support android x projects #230

Version 3.1.0 *(2019-01-11)*
----------------------------
* support incremental annotation processing #233

Version 3.0.3 *(2018-09-26)*
----------------------------
* release version 3.0.2 henson plugin installed a SNAPSHOT version of DH dependency
this is fixed in 3.0.3

Version 3.0.2 *(2018-09-25)*
----------------------------
* fixed issue https://github.com/f2prateek/dart/pull/228 make builds more quiet

Version 3.0.1 *(2018-06-22)*
----------------------------
* Fixed bug where parent activities using Dart outside the module couldn't be found all the time.
* Issue #217 resolved. The Henson Navigator works with Kotlin! Thanks to WenChao Kong (@cooperkong) for helping us to support kotlin!

Version 3.0.0 *(2018-05-22)*
----------------------------
* Version 3.X is not compatible with previous versions, please refer to the official documentation on Github (readme and wiki) to learn about the new API.

Version 2.0.2 *(2017-02-15)*
----------------------------
* Issue #132 resolved. Models can now use inheritance.

Version 2.0.1 *(2016-08-04)*
----------------------------
* Upgrade to JavaPoet.
* Upgrade testing lib for annotation processors. We can now see errors properly displayed in Intelli J.
* Add option to henson to use reflection when referencing target activity classes, see #128.

Version 2.0.0 *(2016-02-29)*
----------------------------
* Switch to JavaPoet.
* Use android.support.annotation.Nullable instead of com.f2prateek.dart.Optional.
* Do not throw UnableToInjectException if there's no @InjectExtras annotation found.
* Fix keys can be java identifiers.
* Add support for injecting a service .
* Add Henson helps to navigate between activities.

Version 1.2.0 
----------------------------

* Use variable name as key if one is not provided in annotation.
* Fix `char` injection.


Version 1.1.0 *(2014-01-17)*
----------------------------

* Support [Parceler](https://github.com/johncarl81/parceler) types.
* Guard against null intents and null bundles.
* Respect default values for optional extras.


Version 1.0.0 *(2014-01-10)*
----------------------------

Initial release.
