Dart [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.f2prateek.dart/dart/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.f2prateek.dart/dart) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Dart-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/1444)[![Build Status](https://travis-ci.org/f2prateek/dart.svg?branch=master)](https://travis-ci.org/f2prateek/dart)
============

Extra "injection" library for Android which uses annotation processing to
generate code that does direct field assignment of your extras.

Dart is inspired by [ButterKnife][1].

```java
class ExampleActivity extends Activity {
  @InjectExtra String extra1;
  @InjectExtra int extra2;
  @InjectExtra User extra3; // User implements Parcelable

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.simple_activity);
    Dart.inject(this);
    // TODO Use "injected" extras...
  }
}
```

Simply call one of the `inject()` methods, which will delegate to generated code.
You can inject from an Activity (which uses it's intent extras), Fragment (which use it's arguments)
or directly from a Bundle.

The key used for the extra will be by default the variable name. However, it can be set manually as a parameter in the annotation: `@InjectExtra("key")`

Optional Injection
------------------
By default all `@InjectExtra` fields are required. An exception will be thrown if the target extra cannot be found.

To suppress this behavior and create an optional injection, add the `@Nullable` annotation to the field or method.
Any annotation with the class name `Nullable` is respected, including ones from the support library annotations and ButterKnife.

```java
@Nullable @InjectExtra String title;
```

Default Values
--------------
You can assign any values to your fields to be used as default values, just as you would in regular "injection"-free code.
```java
@InjectExtra String title = "Default Title";
```
This value will be overriden after you call `inject()`. Remember to use the `@Nullable` annotation, if this injection is optional.

Bonus
-----

Also included is a `get()` method that simplifies code to retrieve extras from a Bundle.
It uses generics to infer return type and automatically perform the cast.

```java
Bundle bundle = getIntent().getExtras(); // getArguments() for a Fragment
User user = Dart.get(bundle, "key"); // User implements Parcelable
```

Henson
-----
In Dart 2.0, we added an anotation processor that helps you navigate between activities. 
The new module is called Henson (after [Matthew Henson](https://en.wikipedia.org/wiki/Matthew_Henson), the african american artic explorer that first reached the North Pole) :

For the sample activity mentioned above, Henson would offer a DSL to navigate to it easily : 
```java
Intent intent = Henson.with(this)
        .gotoExampleActivity()
        .key_1("defaultKeyExtra")
        .key_2(2)
        .key_3(new User())
        .build();
        
startActivty(intent);
```

Off course, you can add any additional extra to the intent before using it.

The Henson annotation processor will generate the Henson navigator class (used above) in a package that is : 
* either the package specified by the `dart.henson.package` annotation processor option
* or if no such option is used, in the common package of all annotated activities. See the Javadoc of HensonExtraProcessor for more details.


Proguard
--------

If Proguard is enabled be sure to add these rules on your configuration:

```
-dontwarn com.f2prateek.dart.internal.**
-keep class **$$ExtraInjector { *; }
-keepclasseswithmembernames class * {
    @com.f2prateek.dart.* <fields>;
}
#for dart 2.0 only
-keep class **Henson { *; }
-keep class **$$IntentBuilder { *; }
```

Bonus
-----

As you can see from the examples above, using both Dart & Henson not only provided a very structured generated navigation layer and conveninent DSLs, it also completely transparently allows to wrap/unwrap parcelables. 


Download
--------

For Dart 1.x :
Download [the latest JAR][2] or grab via Maven:
```xml
<dependency>
  <groupId>com.f2prateek.dart</groupId>
  <artifactId>dart</artifactId>
  <version>(insert latest version)</version>
</dependency>
```
or Gradle:
```groovy
compile 'com.f2prateek.dart:dart:(insert latest version)'
```

For Dart 2.x :
```xml
<dependency>
  <groupId>com.f2prateek.dart</groupId>
  <artifactId>dart</artifactId>
  <version>(insert latest version)</version>
</dependency>
<dependency>
  <groupId>com.f2prateek.dart</groupId>
  <artifactId>dart-processor</artifactId>
  <version>(insert latest version)</version>
  <scope>provided</scope>
</dependency>
```
or Gradle:
```groovy
compile 'com.f2prateek.dart:dart:(insert latest version)'
provided 'com.f2prateek.dart:dart-processor:(insert latest version)'
```

And for using Henson : 
```xml
<dependency>
  <groupId>com.f2prateek.dart</groupId>
  <artifactId>henson</artifactId>
  <version>(insert latest version)</version>
</dependency>
<dependency>
  <groupId>com.f2prateek.dart</groupId>
  <artifactId>henson-processor</artifactId>
  <version>(insert latest version)</version>
  <scope>provided</scope>
</dependency>
```
or Gradle:
```groovy
compile 'com.f2prateek.dart:henson:(insert latest version)'
provided 'com.f2prateek.dart:henson-processor:(insert latest version)'
```

When using Henson, as Android Studio doesn't call live annotation processors when editing a file, you might prefer using the [apt Android Studio plugin](https://bitbucket.org/hvisser/android-apt). It will allow to use Henson generated DSL right away when you edit your code. 

The Henson annotation processor dependency would then have to be declared within the apt scope instead of provided.

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


 [1]: http://jakewharton.github.io/butterknife/
 [2]: http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.f2prateek.dart&a=dart&v=LATEST
