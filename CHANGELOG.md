Change Log
==========

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
