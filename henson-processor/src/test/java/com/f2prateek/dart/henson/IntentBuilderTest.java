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

package com.f2prateek.dart.henson;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Ignore;
import org.junit.Test;

import static com.f2prateek.dart.henson.ProcessorTestUtilities.hensonProcessors;
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


      JavaFileObject builderSource =
          JavaFileObjects.forSourceString("test/Test$$IntentBuilder", Joiner.on('\n').join("package test;", //
                                                                                               "import android.os.Bundle;", //
                                                                                               "import com.f2prateek.dart.henson.Bundler;", //
                                                                                               "import android.content.Context;", //
                                                                                               "import android.content.Intent;", //
                                                                                               "public class Test$$IntentBuilder {", //
                                                                                               "  private final Intent intent;", //
                                                                                               "  private final Bundler bundler = Bundler.create();", //
                                                                                               "  public Test$$IntentBuilder(Context context) {", //
                                                                                               "    intent = new Intent(context, Test.class);", //
                                                                                               "  }", //
                                                                                               "  public Test$$IntentBuilder key(java.lang.String key) {", //
                                                                                               "    bundler.put(\"key\", key);", //
                                                                                               "    return this;", //
                                                                                               "  }", //
                                                                                               "  public Intent get() {", //
                                                                                               "    intent.putExtras(bundler.get());", //
                                                                                               "    return intent;", //
                                                                                               "  }", //
                                                                                               "}"));


    ASSERT.about(javaSource())
        .that(source)
        .processedWith(hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
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

      JavaFileObject builderSource =
          JavaFileObjects.forSourceString("test/Test$$IntentBuilder", Joiner.on('\n').join( //
                                                                                     "package test;", //
                                                                                         "import android.os.Bundle;", //
                                                                                         "import com.f2prateek.dart.henson.Bundler;", //
                                                                                         "import android.content.Context;", //
                                                                                         "import android.content.Intent;", //
                                                                                         "public class Test$$IntentBuilder {", //
                                                                                         "  private final Intent intent;", //
                                                                                         "  private final Bundler bundler = Bundler.create();", //
                                                                                         "  public Test$$IntentBuilder(Context context) {", //
                                                                                         "    intent = new Intent(context, Test.class);", //
                                                                                         "  }", //
                                                                                         "  public Test$$IntentBuilder key_bool(java.lang.Boolean key_bool) {", //
                                                                                         "    bundler.put(\"key_bool\", key_bool);", //
                                                                                         "    return this;", //
                                                                                         "  }", //
                                                                                         "  public Test$$IntentBuilder key_byte(java.lang.Byte key_byte) {", //
                                                                                         "    bundler.put(\"key_byte\", key_byte);", //
                                                                                         "    return this;", //
                                                                                         "  }", //
                                                                                         "  public Test$$IntentBuilder key_short(java.lang.Short key_short) {", //
                                                                                         "    bundler.put(\"key_short\", key_short);", //
                                                                                         "    return this;", //
                                                                                         "  }", //
                                                                                         "  public Test$$IntentBuilder key_int(java.lang.Integer key_int) {", //
                                                                                         "    bundler.put(\"key_int\", key_int);", //
                                                                                         "    return this;", //
                                                                                         "  }", //
                                                                                         "  public Test$$IntentBuilder key_long(java.lang.Long key_long) {", //
                                                                                         "    bundler.put(\"key_long\", key_long);", //
                                                                                         "    return this;", //
                                                                                         "  }", //
                                                                                         "  public Test$$IntentBuilder key_char(java.lang.Character key_char) {", //
                                                                                         "    bundler.put(\"key_char\", key_char);", //
                                                                                         "    return this;", //
                                                                                         "  }", //
                                                                                         "  public Test$$IntentBuilder key_float(java.lang.Float key_float) {", //
                                                                                         "    bundler.put(\"key_float\", key_float);", //
                                                                                         "    return this;", //
                                                                                         "  }", //
                                                                                         "  public Test$$IntentBuilder key_double(java.lang.Double key_double) {", //
                                                                                         "    bundler.put(\"key_double\", key_double);", //
                                                                                         "    return this;", //
                                                                                         "  }", //
                                                                                         "  public Intent get() {", //
                                                                                         "    intent.putExtras(bundler.get());", //
                                                                                         "    return intent;", //
                                                                                         "  }", //
                                                                                         "}\n"
          ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
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

      JavaFileObject builderSource =
          JavaFileObjects.forSourceString("test/Test$$IntentBuilder", Joiner.on('\n').join( //
                                                                                     "package test;", //
                                                                                         "", //
                                                                                         "import android.os.Bundle;", //
                                                                                         "import com.f2prateek.dart.henson.Bundler;", //
                                                                                         "import android.content.Context;", //
                                                                                         "import android.content.Intent;", //
                                                                                         "", //
                                                                                         "public class Test$$IntentBuilder {", //
                                                                                         "  private final Intent intent;", //
                                                                                         "  private final Bundler bundler = Bundler.create();", //
                                                                                         "", //
                                                                                         "  public Test$$IntentBuilder(Context context) {", //
                                                                                         "    intent = new Intent(context, Test.class);", //
                                                                                         "  }", //
                                                                                         "  public Test$$IntentBuilder key(java.lang.String key) {", //
                                                                                         "    bundler.put(\"key\", key);", //
                                                                                         "    return this;", //
                                                                                         "  }", //
                                                                                         "  public Intent get() {", //
                                                                                         "    intent.putExtras(bundler.get());", //
                                                                                         "    return intent;", //
                                                                                         "  }", //
                                                                                         "}"
          ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
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

      JavaFileObject builderSource =
          JavaFileObjects.forSourceString("test/Test$$IntentBuilder", Joiner.on('\n').join("package test;", //
                                                                                           "import android.os.Bundle;", //
                                                                                           "import com.f2prateek.dart.henson.Bundler;", //
                                                                                           "import android.content.Context;", //
                                                                                           "import android.content.Intent;", //
                                                                                           "public class Test$$IntentBuilder {", //
                                                                                           "  private final Intent intent;", //
                                                                                           "  private final Bundler bundler = Bundler.create();", //
                                                                                           "  public Test$$IntentBuilder(Context context) {", //
                                                                                           "    intent = new Intent(context, Test.class);", //
                                                                                           "  }", //
                                                                                           "  public Test$$IntentBuilder key(java.lang.String key) {", //
                                                                                           "    bundler.put(\"key\", key);", //
                                                                                           "    return this;", //
                                                                                           "  }", //
                                                                                           "  public Intent get() {", //
                                                                                           "    intent.putExtras(bundler.get());", //
                                                                                           "    return intent;", //
                                                                                           "  }", //
                                                                                           "}"));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }

  @Test @Ignore("Sorry, but I can't find the issue here") public void superclass() {
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
        JavaFileObjects.forSourceString("test/Test_Bundler", Joiner.on('\n').join( //
            "package test;", //
            "", //
            "import android.content.Context;", //
            "import android.content.Intent;", //
            "import java.lang.String;", //
            "", //
            "public class Test_Bundler {", //
            "  private final Context context;", //
            "", //
            "  private String extra;", //
            "", //
            "  private boolean extraIsSet;", //
            "", //
            "  public Test_Bundler(Context context) {", //
            "    this.context = context;", //
            "  }", //
            "", //
            "  public Test_Bundler withExtra(String extra) {", //
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
        JavaFileObjects.forSourceString("test/TestOne_Bundler", Joiner.on('\n').join( //
            "package test;", //
             "", //
             "import android.content.Context;", //
             "import android.content.Intent;", //
             "import java.lang.String;", //
             "", //
             "public class TestOne_Bundler {", //
             "  private final Context context;", //
             "", //
             "  private String extra1;", //
             "", //
             "  private boolean extra1IsSet;", //
             "", //
             "  public TestOne_Bundler(Context context) {", //
             "    this.context = context;", //
             "  }", //
             "", //
             "  public TestOne_Bundler withExtra1(String extra1) {", //
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
        .processedWith(hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource1, expectedSource2);
  }

    @Test @Ignore("Sorry, but I can't find the issue here") public void genericSuperclass() {
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
          JavaFileObjects.forSourceString("test/Test_Bundler", Joiner.on('\n').join( //
              "package test;", //
              "", //
              "import android.content.Context;", //
              "import android.content.Intent;", //
              "import java.lang.String;", //
              "", //
              "public class Test_Bundler {", //
              "  private final Context context;", //
              "", //
              "  private String extra;", //
              "", //
              "  private boolean extraIsSet;", //
              "", //
              "  public Test_Bundler(Context context) {", //
              "    this.context = context;", //
              "  }", //
              "", //
              "  public Test_Bundler withExtra(String extra) {", //
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
          JavaFileObjects.forSourceString("test/TestOne_Bundler", Joiner.on('\n').join( //
              "package test;", //
              "", //
              "import android.content.Context;", //
              "import android.content.Intent;", //
              "import java.lang.String;", //
              "", //
              "public class TestOne_Bundler {", //
              "  private final Context context;", //
              "", //
              "  private String extra1;", //
              "", //
              "  private boolean extra1IsSet;", //
               "", //
              "  public TestOne_Bundler(Context context) {", //
              "    this.context = context;", //
              "  }", //
              "", //
              "  public TestOne_Bundler withExtra1(String extra1) {", //
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
        .processedWith(hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource1, expectedSource2);
    }

    @Test @Ignore("Sorry, but I can't find the issue here") public void injectingParcelExtra() {
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

        JavaFileObject builderSource =
            JavaFileObjects.forSourceString("test/Test_Bundler", Joiner.on('\n').join("package test;\n", //
                                                                                          "\n", //
                                                                                          "import com.f2prateek.dart.henson.Bundler, //;\n", //
                                                                                          "public class Test_Bundler {\n", //
                                                                                          "  private final Bundler bundler = Bundler.create();\n", //
                                                                                          "  public Test_Bundler key(test.ExampleParcel key) {\n", //
                                                                                          "    bundler.put(\"key\", org.parceler.Parcels.wrap(key));\n", //
                                                                                          "    return this;\n", //
                                                                                          "   }\n", //
                                                                                          "   public Bundle get() {\n", //
                                                                                          "     return bundler.get();\n", //
                                                                                          "   }\n", //
                                                                                          "}\n" //
            ));

        ASSERT.about(javaSource())
            .that(source)
            .processedWith(hensonProcessors())
            .compilesWithoutError()
            .and()
            .generatesSources(builderSource);
    }
}
