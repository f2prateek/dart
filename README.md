Dart
============

Extra "injection" library for Android which uses annotation processing to
generate code that does direct field assignment of your extras.

Dart is inspired by [ButterKnife][1].

```java
class ExampleActivity extends Activity {
  @InjectExtra("key_1") String extra1;
  @InjectExtra("key_2") int extra2;
  @InjectExtra("key_3") User extra3; // User implements Parcelable

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

Optional Injection
------------------
By default all @InjectExtra fields are required. An exception will be thrown if the target extra cannot be found.

To suppress this behavior and create an optional injection, add the `@Nullable` annotation to the field or method.
Any annotation with the class name `Nullable` is respected, including ones from the support library annotations and ButterKnife.

```java
@Nullable @InjectExtra("key") String title;
```

Bonus
-----

Also included is a `get()` method that simplifies code to retrieve extras from a Bundle.
It uses generics to infer return type and automatically perform the cast.

```java
Bundle bundle = getIntent().getExtras(); // getArguments() for a Fragment
User user = Dart.get(bundle, "key"); // User implements Parcelable
```

Proguard
--------

If Proguard is enabled be sure to add these rules on your configuration:

```
-dontwarn com.f2prateek.dart.internal.**
-keep class **$$ExtraInjector { *; }
-keepnames class * { @com.f2prateek.dart.InjectExtra *;}
```

Download
--------

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
