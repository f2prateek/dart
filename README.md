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
You can inject from an Activity (which uses its intent extras), Fragment (which uses its arguments)
or directly from a Bundle.

The key used for the extra will be the field name by default. However, it can be set manually as a parameter in the annotation: `@InjectExtra("key")`

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
This value will be overridden after you call `inject()`. Remember to use the `@Nullable` annotation, if this injection is optional.

Bonus
-----

Also included is a `get()` method that simplifies code to retrieve extras from a Bundle.
It uses generics to infer return type and automatically perform the cast.

```java
Bundle bundle = getIntent().getExtras(); // getArguments() for a Fragment
User user = Dart.get(bundle, "key"); // User implements Parcelable
```

Henson
------
In Dart 2.0, we added an annotation processor that helps you to navigate between activities.
The new module is called Henson (after [Matthew Henson](https://en.wikipedia.org/wiki/Matthew_Henson), the African-American Arctic explorer that first reached the North Pole) :

For the sample activity mentioned above, Henson will offer a DSL to navigate to it easily :
```java
Intent intent = Henson.with(this)
        .gotoExampleActivity()
        .extra1("defaultKeyExtra")
        .extra2(2)
        .extra3(new User())
        .build();

startActivity(intent);
```

Of course, you can add any additional extra to the intent before using it.

The DSL will be generated for all classes which contain `@InjectExtra` fields. If you want to extend it to other classes, use the `@HensonNavigable` annotation.

```java
@HensonNavigable
class AnotherActivity extends Activity {
  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ...
  }
}
```

The Henson annotation processor will generate the `Henson` navigator class (used above) in a package that is :
* either the package specified by the `dart.henson.package` annotation processor option
* or if no such option is used, in the common package of all annotated activities. See the Javadoc of `HensonExtraProcessor` for more details.

If your activites and fragment are in *different packages*, you will need to specify a package via the `dart.henson.package` annotation processor option.
If you're using gradle, simply add this to your `build.gradle`
```groovy
apt {
    arguments {
        "dart.henson.package" "your.package.name"
    }
}
```

If you're using the newest version of Android gradle plugin, it's now possible to use a built-in annotationProcessor scope instead of apt. In this case, you can pass arguments to the annotation processors using :

```groovy
defaultConfig {
    javaCompileOptions {
        annotationProcessorOptions {
            arguments = [ 'dart.henson.package' : 'your.package.name' ]
        }
    }
}
```

Bonus
-----
As you can see from the examples above, using both Dart & Henson not only provides a very structured generated navigation layer and convenient DSLs; it also allows to wrap/unwrap parcelables automatically.

Parceler
-------------------------
Dart 2.0 offers a built-in support for [Parceler](https://github.com/johncarl81/parceler). Using Parceler with Dart 2 is optional.

If you use Parceler, Dart will automatically detect @Parcel annotated beans (pojos), or collections of them, and wrap them using the Henson DSL and unwrap them when they are injected via Dart.

```java
@Parcel
public class ParcelExample {
    ...
}
```

```java
class OneMoreActivityActivity extends Activity {
  @InjectExtra ParcelExample extra;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.simple_activity);
    Dart.inject(this);
    // TODO Use "injected" extras...
  }
}
```

```java
Intent intent = Henson.with(this)
        .gotoOneMoreActivityActivity()
        .extra(new ParcelExample())
        .build();

startActivity(intent);
```
Parceler usage is optional and will take place only when Parceler is present in the classpath.

When available, Parceler will be used to parcelize collections instead of serializing them, in order to gain speed.

ProGuard
--------

If ProGuard is enabled be sure to add these rules to your configuration:

```
-dontwarn com.f2prateek.dart.internal.**
-keep class **$$ExtraInjector { *; }
-keepclasseswithmembernames class * {
    @com.f2prateek.dart.* <fields>;
}
#for dart 2.0 only
-keep class **Henson { *; }
-keep class **$$IntentBuilder { *; }


#if you use it
#see Parceler's github page
#for specific proguard instructions
```

Download
--------

For Dart 2.x :
Gradle:
```groovy
compile 'com.f2prateek.dart:dart:(insert latest version)'
provided 'com.f2prateek.dart:dart-processor:(insert latest version)'
```
or maven
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

And for using Henson :
Gradle:
```groovy
compile 'com.f2prateek.dart:henson:(insert latest version)'
provided 'com.f2prateek.dart:henson-processor:(insert latest version)'
```
When using Henson, as Android Studio doesn't call live annotation processors when editing a file, you might prefer using the [apt Android Studio plugin](https://bitbucket.org/hvisser/android-apt). It will allow you to use the Henson generated DSL right away when you edit your code.

The Henson annotation processor dependency would then have to be declared within the apt scope instead of provided.

or maven
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
For Dart 1.x :
Gradle:
```groovy
compile 'com.f2prateek.dart:dart:(insert latest version)'
```
Maven:
```xml
<dependency>
  <groupId>com.f2prateek.dart</groupId>
  <artifactId>dart</artifactId>
  <version>(insert latest version)</version>
</dependency>
```

Kotlin
-----
For all Kotlin enthusiasts, you may wonder how to use this library to configure your intents. This is perfectly compatible, with a bit of understanding of how Kotlin works, especially when it comes to annotation processing.

Assuming that your project is already configured with Kotlin, update your `build.gradle` file :

```groovy
apply plugin: 'kotlin-kapt'

dependencies {
  compile 'com.f2prateek.dart:henson:(insert latest version)'
  kapt 'com.f2prateek.dart:henson-processor:(insert latest version)'
}
```

Now you can use `@InjectExtra` annotation to generate either non-null or nullables properties :

```kotlin
class ExampleActivity : Activity() {
  @InjectExtra
  lateinit var title: String

  @InjectExtra
  var titleDefaultValue: String = "Default Title"

  @Nullable
  @JvmField
  @InjectExtra
  var titleNullable: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(R.layout.simple_activity)
      Dart.inject(this)
      // TODO Use "injected" extras...
  }
}
```

Note that you still need to use the Java `@Nullable` annotation otherwise Henson won't interpret your property as nullable and will generate a builder with a mandatory field (even though you declared your property as nullable with the "?" Kotlin marker). Finally, you have to add the `@JvmField` annotation or your compiler will complain about not having a backing field.

You may need to add an argument to your `build.gradle` file if your activities and fragments are located in different packages as mentioned above. The Kotlin syntax with **kapt** is :

```groovy
kapt {
    arguments {
        arg("dart.henson.package", "your.package.name")
    }
}
```

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
