/*
 * Copyright 2013 Jake Wharton
 * Copyright 2014 Prateek Srivastava (@f2prateek)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.f2prateek.dart.internal;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.f2prateek.dart.internal.ProcessorTestUtilities.dartProcessors;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.truth0.Truth.ASSERT;

public class IntentBuilderTest {

  @Test public void injectingExtra() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "public class Test extends Activity {", //
        "    @InjectExtra(\"key\") String extra;", //
        "}" //
    ));

    JavaFileObject expectedSource =
        JavaFileObjects.forSourceString("test/TestIntentBuilder", Joiner.on('\n').join( //
            "package test;", //
             "", //
             "import android.content.Context;" , //
             "import android.content.Intent;", //
             "import java.lang.String;", //
             "", //
             "public class TestIntentBuilder {", //
             "  private final Context context;", //
             "", //
             "  private String extra;", //
             "", //
             "  private boolean extraIsSet;", //
             "", //
             "  public TestIntentBuilder(Context context) {", //
             "    this.context = context;", //
             "  }", //
             "", //
             "  public TestIntentBuilder withExtra(String extra) {", //
             "    this.extra = extra;", //
             "    extraIsSet = true;", //
             "    return this;", //
             "  }", //
             "", //
             "  public Intent build() {", //
             "    Intent intent = new Intent(context, test.Test.class);", //
             "    if (extraIsSet) {", //
             "      intent.putExtra(\"key\", extra);", //
             "    } else {", //
             "      throw new IllegalStateException(\"Parameter extra is mandatory\");", //
             "    }", //
             "    return intent;", //
             "  }", //
             "}" //
      ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(dartProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test public void injectingAllPrimitives() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "public class Test extends Activity {", //
        "    @InjectExtra(\"key_bool\") boolean aBool;", //
        "    @InjectExtra(\"key_byte\") byte aByte;", //
        "    @InjectExtra(\"key_short\") short aShort;", //
        "    @InjectExtra(\"key_int\") int anInt;", //
        "    @InjectExtra(\"key_long\") long aLong;", //
        "    @InjectExtra(\"key_char\") char aChar;", //
        "    @InjectExtra(\"key_float\") float aFloat;", //
        "    @InjectExtra(\"key_double\") double aDouble;", //
        "}" //
    ));

    JavaFileObject expectedSource =
        JavaFileObjects.forSourceString("test/TestIntentBuilder", Joiner.on('\n').join( //
            "package test;", //
            "", //
            "import android.content.Context;", //
            "import android.content.Intent;", //
            "", //
            "public class TestIntentBuilder {", //
            "  private final Context context;", //
            "", //
            "  private boolean aBool;", //
            "", //
            "  private boolean aBoolIsSet;", //
            "", //
            "  private byte aByte;", //
            "", //
            "  private boolean aByteIsSet;", //
            "", //
            "  private short aShort;", //
            "", //
            "  private boolean aShortIsSet;", //
            "", //
            "  private int anInt;", //
            "", //
            "  private boolean anIntIsSet;", //
            "", //
            "  private long aLong;", //
            "", //
            "  private boolean aLongIsSet;", //
            "", //
            "  private char aChar;", //
            "", //
            "  private boolean aCharIsSet;", //
            "", //
            "  private float aFloat;", //
            "", //
            "  private boolean aFloatIsSet;", //
            "", //
            "  private double aDouble;", //
            "", //
            "  private boolean aDoubleIsSet;", //
            "", //
            "  public TestIntentBuilder(Context context) {", //
            "    this.context = context;", //
            "  }", //
            "", //
            "  public TestIntentBuilder withABool(boolean aBool) {", //
            "    this.aBool = aBool;", //
            "    aBoolIsSet = true;", //
            "    return this;", //
            "  }", //
            "", //
            "  public TestIntentBuilder withAByte(byte aByte) {", //
            "    this.aByte = aByte;", //
            "    aByteIsSet = true;", //
            "    return this;", //
            "  }", //
            "", //
            "  public TestIntentBuilder withAShort(short aShort) {", //
            "    this.aShort = aShort;", //
            "    aShortIsSet = true;", //
            "    return this;", //
            "  }", //
            "", //
            "  public TestIntentBuilder withAnInt(int anInt) {", //
            "    this.anInt = anInt;", //
            "    anIntIsSet = true;", //
            "    return this;", //
            "  }", //
            "", //
            "  public TestIntentBuilder withALong(long aLong) {", //
            "    this.aLong = aLong;", //
            "    aLongIsSet = true;", //
            "    return this;", //
            "  }", //
            "", //
            "  public TestIntentBuilder withAChar(char aChar) {", //
              "    this.aChar = aChar;", //
              "    aCharIsSet = true;", //
              "    return this;", //
              "  }", //
              "", //
              "  public TestIntentBuilder withAFloat(float aFloat) {", //
              "    this.aFloat = aFloat;", //
              "    aFloatIsSet = true;", //
              "    return this;", //
              "  }", //
              "", //
              "  public TestIntentBuilder withADouble(double aDouble) {", //
              "    this.aDouble = aDouble;", //
              "    aDoubleIsSet = true;", //
              "    return this;", //
              "  }", //
              "", //
              "  public Intent build() {", //
              "    Intent intent = new Intent(context, test.Test.class);", //
              "    if (aBoolIsSet) {", //
              "      intent.putExtra(\"key_bool\", aBool);", //
              "    } else {", //
              "      throw new IllegalStateException(\"Parameter aBool is mandatory\");", //
              "    }", //
              "    if (aByteIsSet) {", //
              "      intent.putExtra(\"key_byte\", aByte);", //
              "    } else {", //
              "      throw new IllegalStateException(\"Parameter aByte is mandatory\");", //
              "    }", //
              "    if (aShortIsSet) {", //
              "      intent.putExtra(\"key_short\", aShort);", //
              "    } else {", //
              "      throw new IllegalStateException(\"Parameter aShort is mandatory\");", //
              "    }", //
              "    if (anIntIsSet) {", //
              "      intent.putExtra(\"key_int\", anInt);", //
              "    } else {", //
              "      throw new IllegalStateException(\"Parameter anInt is mandatory\");", //
              "    }", //
              "    if (aLongIsSet) {", //
              "      intent.putExtra(\"key_long\", aLong);", //
              "    } else {", //
              "      throw new IllegalStateException(\"Parameter aLong is mandatory\");", //
              "    }", //
              "    if (aCharIsSet) {", //
              "      intent.putExtra(\"key_char\", aChar);", //
              "    } else {", //
              "      throw new IllegalStateException(\"Parameter aChar is mandatory\");", //
              "    }", //
              "    if (aFloatIsSet) {", //
              "      intent.putExtra(\"key_float\", aFloat);", //
              "    } else {", //
              "      throw new IllegalStateException(\"Parameter aFloat is mandatory\");", //
              "    }", //
              "    if (aDoubleIsSet) {", //
              "      intent.putExtra(\"key_double\", aDouble);", //
              "    } else {", //
              "      throw new IllegalStateException(\"Parameter aDouble is mandatory\");", //
              "    }", //
              "    return intent;", //
              "  }", //
              "}" //
        ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(dartProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test public void oneFindPerKey() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "public class Test extends Activity {", //
        "    @InjectExtra(\"key\") String extra1;", //
        "    @InjectExtra(\"key\") String extra2;", //
        "    @InjectExtra(\"key\") String extra3;", //
        "}" //
    ));

    JavaFileObject expectedSource =
        JavaFileObjects.forSourceString("test/TestIntentBuilder", Joiner.on('\n').join( //
            "package test;", //
            "", //
            "import android.content.Context;", //
            "import android.content.Intent;", //
            "import java.lang.String;", //
            "", //
            "public class TestIntentBuilder {", //
            "  private final Context context;", //
            "", //
            "  private String extra1;", //
            "", //
            "  private boolean extra1IsSet;", //
            "", //
            "  public TestIntentBuilder(Context context) {", //
            "    this.context = context;", //
            "  }", //
            "", //
            "  public TestIntentBuilder withExtra1(String extra1) {", //
            "    this.extra1 = extra1;", //
            "    extra1IsSet = true;", //
            "    return this;", //
            "  }", //
            "", //
            "  public Intent build() {", //
            "    Intent intent = new Intent(context, test.Test.class);", //
            "    if (extra1IsSet) {", //
            "      intent.putExtra(\"key\", extra1);", //
            "    } else {", //
            "      throw new IllegalStateException(\"Parameter extra1 is mandatory\");", //
            "    }", //
            "    return intent;", //
            "  }", //
            "}"  //
        ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(dartProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test public void defaultKey() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "public class Test extends Activity {", //
        "    @InjectExtra String key;", //
        "}" //
    ));

    JavaFileObject expectedSource =
        JavaFileObjects.forSourceString("test/TestIntentBuilder", Joiner.on('\n').join( //
            "package test;", //
            "", //
            "import android.content.Context;", //
             "import android.content.Intent;", //
             "import java.lang.String;", //
             "", //
             "public class TestIntentBuilder {", //
             "  private final Context context;", //
             "", //
             "  private String key;", //
             "", //
             "  private boolean keyIsSet;", //
             "", //
             "  public TestIntentBuilder(Context context) {", //
             "    this.context = context;", //
             "  }", //
             "", //
             "  public TestIntentBuilder withKey(String key) {", //
             "    this.key = key;", //
             "    keyIsSet = true;", //
             "    return this;", //
             "  }", //
             "", //
             "  public Intent build() {", //
             "    Intent intent = new Intent(context, test.Test.class);", //
             "    if (keyIsSet) {", //
             "      intent.putExtra(\"key\", key);", //
             "    } else {", //
             "      throw new IllegalStateException(\"Parameter key is mandatory\");", //
             "    }", //
             "    return intent;", //
             "  }", //
             "}" //
        ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(dartProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test public void superclass() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "public class Test extends Activity {", //
        "    @InjectExtra(\"key\") String extra;", //
        "}", //
        "class TestOne extends Test {", //
        "    @InjectExtra(\"key\") String extra1;", //
        "}", //
        "class TestTwo extends Test {", //
        "}" //
    ));

    JavaFileObject expectedSource1 =
        JavaFileObjects.forSourceString("test/TestIntentBuilder", Joiner.on('\n').join( //
            "package test;", //
            "", //
            "import android.content.Context;", //
            "import android.content.Intent;", //
            "import java.lang.String;", //
            "", //
            "public class TestIntentBuilder {", //
            "  private final Context context;", //
            "", //
            "  private String extra;", //
            "", //
            "  private boolean extraIsSet;", //
            "", //
            "  public TestIntentBuilder(Context context) {", //
            "    this.context = context;", //
            "  }", //
            "", //
            "  public TestIntentBuilder withExtra(String extra) {", //
            "    this.extra = extra;", //
            "    extraIsSet = true;", //
            "    return this;", //
            "  }", //
            "", //
            "  public Intent build() {", //
            "    Intent intent = new Intent(context, test.Test.class);", //
            "    if (extraIsSet) {", //
            "      intent.putExtra(\"key\", extra);", //
            "    } else {", //
            "      throw new IllegalStateException(\"Parameter extra is mandatory\");", //
            "    }", //
            "    return intent;", //
            "  }", //
            "}" //
        ));

    JavaFileObject expectedSource2 =
        JavaFileObjects.forSourceString("test/TestOneIntentBuilder", Joiner.on('\n').join( //
            "package test;", //
             "", //
             "import android.content.Context;", //
             "import android.content.Intent;", //
             "import java.lang.String;", //
             "", //
             "public class TestOneIntentBuilder {", //
             "  private final Context context;", //
             "", //
             "  private String extra1;", //
             "", //
             "  private boolean extra1IsSet;", //
             "", //
             "  public TestOneIntentBuilder(Context context) {", //
             "    this.context = context;", //
             "  }", //
             "", //
             "  public TestOneIntentBuilder withExtra1(String extra1) {", //
             "    this.extra1 = extra1;", //
             "    extra1IsSet = true;", //
             "    return this;", //
             "  }", //
             "", //
             "  public Intent build() {", //
             "    Intent intent = new Intent(context, test.TestOne.class);", //
             "    if (extra1IsSet) {", //
             "      intent.putExtra(\"key\", extra1);", //
             "    } else {", //
             "      throw new IllegalStateException(\"Parameter extra1 is mandatory\");", //
             "    }", //
             "    return intent;", //
             "  }", //
             "}" //
        ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(dartProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource1, expectedSource2);

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(dartProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource1, expectedSource2);
  }

  @Test public void genericSuperclass() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "public class Test<T> extends Activity {", //
        "    @InjectExtra(\"key\") String extra;", //
        "}", //
        "class TestOne extends Test<String> {", //
        "    @InjectExtra(\"key\") String extra1;", //
        "}", //
        "class TestTwo extends Test<Object> {", //
        "}" //
    ));

      JavaFileObject expectedSource1 =
          JavaFileObjects.forSourceString("test/TestIntentBuilder", Joiner.on('\n').join( //
              "package test;", //
              "", //
              "import android.content.Context;", //
              "import android.content.Intent;", //
              "import java.lang.String;", //
              "", //
              "public class TestIntentBuilder {", //
              "  private final Context context;", //
              "", //
              "  private String extra;", //
              "", //
              "  private boolean extraIsSet;", //
              "", //
              "  public TestIntentBuilder(Context context) {", //
              "    this.context = context;", //
              "  }", //
              "", //
              "  public TestIntentBuilder withExtra(String extra) {", //
              "    this.extra = extra;", //
              "    extraIsSet = true;", //
              "    return this;", //
              "  }", //
              "", //
              "  public Intent build() {", //
              "    Intent intent = new Intent(context, test.Test.class);", //
              "    if (extraIsSet) {", //
              "      intent.putExtra(\"key\", extra);", //
              "    } else {", //
              "      throw new IllegalStateException(\"Parameter extra is mandatory\");", //
              "    }", //
              "    return intent;", //
              "  }", //
              "}" //
          ));

      JavaFileObject expectedSource2 =
          JavaFileObjects.forSourceString("test/TestOneIntentBuilder", Joiner.on('\n').join( //
              "package test;", //
              "", //
              "import android.content.Context;", //
              "import android.content.Intent;", //
              "import java.lang.String;", //
              "", //
              "public class TestOneIntentBuilder {", //
              "  private final Context context;", //
              "", //
              "  private String extra1;", //
              "", //
              "  private boolean extra1IsSet;", //
               "", //
              "  public TestOneIntentBuilder(Context context) {", //
              "    this.context = context;", //
              "  }", //
              "", //
              "  public TestOneIntentBuilder withExtra1(String extra1) {", //
              "    this.extra1 = extra1;", //
              "    extra1IsSet = true;", //
              "    return this;", //
              "  }", //
              "", //
              "  public Intent build() {", //
              "    Intent intent = new Intent(context, test.TestOne.class);", //
              "    if (extra1IsSet) {", //
              "      intent.putExtra(\"key\", extra1);", //
              "    } else {", //
              "      throw new IllegalStateException(\"Parameter extra1 is mandatory\");", //
              "    }", //
              "    return intent;", //
              "  }", //
              "}" //
          ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(dartProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource1, expectedSource2);
  }

    @Test public void injectingParcelExtra() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
             "package test;", //
             "import android.app.Activity;", //
             "import com.f2prateek.dart.InjectExtra;", //
             "import org.parceler.Parcel;", //
             "import org.parceler.ParcelConstructor;", //
             "@Parcel", //
             "class ExampleParcel {", //
             "", //
             "  String name;", //
             "", //
             "  @ParcelConstructor", //
             "  public ExampleParcel(String name) {", //
             "    this.name = name;", //
             "  }", //
             "", //
             "  public String getName() {", //
             "    return name;", //
             "  }", //
             "}", //
             "public class Test extends Activity {", //
             "    @InjectExtra(\"key\") ExampleParcel extra;", //
             "}" //
        ));

        JavaFileObject expectedSource =
            JavaFileObjects.forSourceString("test/TestIntentBuilder", Joiner.on('\n').join( //
            "package test;", //
             "", //
             "import android.content.Context;", //
             "import android.content.Intent;", //
             "import android.os.Parcelable;", //
             "", //
             "public class TestIntentBuilder {", //
             "  private final Context context;", //
             "", //
             "  private Parcelable extra;", //
             "", //
             "  private boolean extraIsSet;", //
             "", //
             "  public TestIntentBuilder(Context context) {", //
             "    this.context = context;", //
             "  }", //
             "", //
             "  public TestIntentBuilder withExtra(ExampleParcel extra) {", //
             "    this.extra = org.parceler.Parcels.wrap(extra);", //
             "    extraIsSet = true;", //
             "    return this;", //
             "  }", //
             "", //
             "  public Intent build() {", //
             "    Intent intent = new Intent(context, test.Test.class);", //
             "    if (extraIsSet) {", //
             "      intent.putExtra(\"key\", extra);", //
             "    } else {", //
             "      throw new IllegalStateException(\"Parameter extra is mandatory\");", //
             "    }", //
             "    return intent;", //
             "  }", //
             "}" //
            ));


        ASSERT.about(javaSource())
            .that(source)
            .processedWith(dartProcessors())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedSource);
    }
}
