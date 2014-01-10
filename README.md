Dart
============

Extra "injection" library for Android which uses annotation processing to
generate code that does direct field assignment of your extras.

```java
class ExampleActivity extends Activity {
  @InjectExtra("key_1") String extra1;
  @InjectExtra("key_2") int extra2;
  @InjectExtra("key_3") User extra3; // User extends Parcelable

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.simple_activity);
    Dart.inject(this);
    // TODO Use "injected" extras...
  }
}
```


Download
--------

Download [the latest JAR][1] or grab via Maven:
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



 [1]: http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.f2prateek.dart&a=dart&v=LATEST