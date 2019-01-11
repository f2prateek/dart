<p align="center">
 <img src="https://raw.githubusercontent.com/f2prateek/dart/master/assets/logo/banner.png" width=500 align="center">
</p>

Dart [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.f2prateek.dart/dart/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.f2prateek.dart/dart) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Dart-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/1444)[![Build Status](https://travis-ci.org/f2prateek/dart.svg?branch=master)](https://travis-ci.org/f2prateek/dart)
============

**** This is the README of the version 3 of Dart & Henson has been released. If you are looking for the README page of Dart & Henson 2, please visit [this wiki page](https://github.com/f2prateek/dart/wiki/Dart-&-Henson-v2-). ****

## Summary 
Extra "binding" & intent builders library for Android. Dart & Henson (DH) uses annotation processing to bind intent's extras to pojo fields, and to generate intent builders via a fluent API.

## Description of DH 3
Dart and Henson is a an Android Library that structures the navigation layer of your apps. It helps to create intents and consume them in a structured way. We believe it's the best way to organize your navigation layer, and make it less error-prone and easier to maintain.

It is made of 2 components: Dart and Henson. Both of them use annotated classes (navigation models) that describe the parameters (extras of the intent) of a target activity. DH3 provides a gradle plugin to generate the henson navigator class, and we strongly encourage to use the plugin. See the [samples](https://github.com/f2prateek/dart/tree/master/dart-sample) for more details.

## Navigation models

A navigation model class is a simple pojo with annotated non private fields. The fields describe an extra passed to the target of an intent:
```java
@DartModel //use this annotation on all your models
public class MyActivityNavigationModel {
  //a simple requested field, it's name is used as the extra key
  @BindExtra String extra1;
  //a named field using an annotation
  @BindExtra(MY_CONSTANT_NAME) String extra2;
  //an optional field
  @BindExtra @Nullable MyParcelableOrSerializable extra3;
}
```

To setup a navigation model module:
```groovy
dependencies {
  implementation 'com.f2prateek.dart:dart-annotations:X.Y.Z'
  implementation 'com.f2prateek.dart:dart:X.Y.Z'
  implementation 'com.f2prateek.dart:henson:X.Y.Z'
  annotationProcessor 'com.f2prateek.dart:dart-processor:X.Y.Z'
  annotationProcessor 'com.f2prateek.dart:henson-processor:X.Y.Z'
}
```

Note that in DH3, navigation models:
* are mandatory, it's not possible to annotate activities directly.
* must follow a naming convention: they should have the same fully qualified name as the activity or service they describe the navigation of, plus the suffix: `NavigationModel`. (e.g.: `com.foo.wooper.app.MyActivityNavigationModel`).
* must be placed in their own module. If `MyActivity` lives in the module `module-foo`, then you should place your navigation models inside a module named `module-foo-navigation`. There is no constraint enforcement on the name of this module, but we strongly encourage you to stick to this convention for naming the navigation model.
* the `@DartModel` annotation is actually optional if there is at least one field annotated with `@BindExtra`, but, as a good practice, we recommend to always add it.

## Dart

The historical first component of the library is used to map intents to Pojos (navigation models). Typically, a target activity will define a navigation model class, a pojo with annotated fields and will map the intents it receives to an instance of its model: 

```java
public class MyActivity extends Activity {

  //the navigation model field must be annotated
  //it's up to developers to initialize it
  @DartModel MyNavigationModel navigationModel;
  
  public void onCreate(Bundle savedInstanceState) {
    Dart.bind(this);
  }
}
```

Note that in DH3:
* an activity (or a service) can map the extras of the intent it receives, or a bundle like `savedInstanceState`. For fragments the bundle of `getArguments()` will be used;
* you can also use `Dart.bind(this, this)` or `Dart.bind(this, bundle)`;
* the initialization of the navigation model is performed by Dart.
* you can use the code above in a super class and forget not call `bind` in subclasses. But subclasses will need to annotate their own navigation model field.
* in the case of inheritance, the navigation model of the subclasses must extend the navigation model of the super class.
* in the case of inheritance, `bind()` will replace the instance of the navigation model of the super classes by an instance of the navigation model of the subclasses. That's a side effect of Dart, it allows for better performances (as it doesn't rebind the model in all classes).

## Henson

The second component of the library is used to create intents. Based on the navigation model, henson will create an intent builder for the described class (remember the name of the activity / service can be dedudced from the FQN of the model). It creates also some useful wrapper around them, see below.

Generally speaking, Intent Builders generated by Henson are not used directly, but via the `HensonNavigator` class that is generated for each module. When you want a module `M0` to use other module navigation APIs, `M0` must use the gradle henson-plugin.

#### The HensonNavigator Class

Setup of a module using other modules navigation API via `HensonNavigator`:
```groovy
apply plugin: 'dart.henson-plugin'

buildscript {
  repostories {
     jcenter()
  }
  dependencies {
    classpath "com.f2prateek.dart:henson-plugin:X.Y.Z"
  }
}

dependencies {
  implementation project(':module1-navigation')
}

henson {
  navigatorPackageName = "com.foo.module0"
}
```

The plugin scans your dependencies and wraps all the intent builders that are found in the classpath.
The `HensonNavigator` is then generated:
```java
Intent intent = HensonNavigator.gotoMyActivity(context)
 .extra1("foo")
 .extra2(42)
 .extra3(myObj) //optional
 .build();
```

The intent builders used by a module are detected automatically during the build, based on the dependencies a module uses, and the `HensonNavigator` is generated accordingly.

## What's new in DH3 ?

Briefly:
* DH2.1 is available to help you migrate to DH3. We will detail this in our migration guide.
* DH3 classes have been repackaged to allow a smoother migration.
* DH3 fully supports modularization. It was the main motivation for the version 3, and it requested quite a few changes. 
* DH3 supports navigation cycles between modules. As modules expose their navigation APIs in a different module, we avoid compile time cycles.
* DH3 offers a gradle plugin. DH3 uses a lot of annotation processing internally, configurations, artifacts, custom tasks. Do not set it up manually unless you know gradle well. Use the plugin.

ProGuard
--------

If ProGuard is enabled be sure to add these rules to your configuration:

```
-dontwarn dart.internal.**
-keep class **__ExtraBinder { *; }
-keep class **__NavigationModelBinder { *; }
-keepclasseswithmembernames class * {
    @dart.* <fields>;
}
-keep class **Henson { *; }
-keep class **__IntentBuilder { *; }
-keep class **HensonNavigator { *; }

#if you use it
#see Parceler's github page
#for specific proguard instructions
```

Download
--------

Dart:
```groovy
implementation 'com.f2prateek.dart:dart:(insert latest version)'
annotationProcessor 'com.f2prateek.dart:dart-processor:(insert latest version)'
```

Henson :
```groovy
implementation 'com.f2prateek.dart:henson:(insert latest version)'
annotationProcessor 'com.f2prateek.dart:henson-processor:(insert latest version)'
```

Henson-plugin :
```groovy
classpath 'com.f2prateek.dart:henson-plugin:(insert latest version)'
apply plugin: 'dart.henson-plugin'
```

Kotlin
-----
For all Kotlin enthusiasts, you may wonder how to use this library to configure your intents. This is perfectly compatible, with a bit of understanding of how Kotlin works, especially when it comes to annotation processing.

Assuming that your project is already configured with Kotlin, update your `build.gradle` file :

```groovy
apply plugin: 'kotlin-kapt'

dependencies {
  implementation 'com.f2prateek.dart:henson:(insert latest version)'
  kapt 'com.f2prateek.dart:henson-processor:(insert latest version)'
}
```

Please note that DH3 annotation processors will support incremental annotation processing via the new gradle API. They are also deterministic and kapt tasks can be safely cached.

Now you can use `@BindExtra` annotation to generate either non-null or nullables properties :

```kotlin
class MyExampleActivityNavigationModel {
  @BindExtra
  lateinit var title: String

  @BindExtra
  var titleDefaultValue: String = "Default Title"

  @Nullable
  @JvmField
  @BindExtra
  var titleNullable: String? = null
}
```

Note that you still need to use the Java `@Nullable` annotation otherwise Henson won't interpret your property as nullable and will generate a builder with a mandatory field (even though you declared your property as nullable with the "?" Kotlin marker). Finally, you have to add the `@JvmField` annotation or your compiler will complain about not having a backing field.

Finally, if you are using Parceler that comes built-in with this library, the syntax does not change from Java, except when dealing with **data** classes. Because Parceler requires a default constructor with no argument, here is how you need to declare your **data** class :

```kotlin
@Parcel(Parcel.Serialization.BEAN)
data class ParcelExample @ParcelConstructor constructor(
        val id: Int,
        val name: String,
        ...
}
```

Talks & Slides
-------

* AndevCon SF (2016): [slides](https://speakerdeck.com/stephanenicolas/dart-and-henson)
* DroidCon Kaigi, Japan (2017): (https://news.realm.io/news/better-android-intents-with-dart-and-henson/)
* DroidCon Italy, Turin (2017):[video](https://www.youtube.com/watch?v=EVQewsJu0NA)
* "Reducing Intent data passing ‘putExtra’, ‘putInt’, ‘putParcelable’ Boiler plate code" from Take Off Android:[Article](https://blog.f22labs.com/reducing-intent-data-passing-putextra-putint-putparcelable-boiler-plate-code-fd62b449dac3)

Credits
-------

The original author of the library is [Prateek Srivastava](https://github.com/f2prateek) who got inspired by the work of [Jake Wharton](https://github.com/jakewharton) on [butter knife](http://jakewharton.github.io/butterknife/).

Later, [Daniel Molinero Reguera](https://github.com/dlemures) and [Stephane NICOLAS](https://github.com/stephanenicolas) contributed Henson in Dart 2, and rewrote the library for Dart 3.

Logo was designed by [Jibbie R. Eguna(jbeguna04)](https://github.com/jbeguna04)

All the effort on versions 2 and versions 3 have been actively supported by [Groupon](https://groupon.github.io/). Thanks for this awesome OSS commitment !

License
-------

    Copyright 2013 Jake Wharton
    Copyright 2014 Prateek Srivastava (@f2prateek)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


 [1]: http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.f2prateek.dart&a=dart&v=LATEST
