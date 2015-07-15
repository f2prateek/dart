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

package com.f2prateek.dart.henson.processor;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

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
        JavaFileObjects.forSourceString("test/Test$$IntentBuilder", Joiner.on('\n').join( //
            "package test;", //
                "import android.content.Context;", //
                "import android.content.Intent;", //
                "import com.f2prateek.dart.henson.Bundler;", //
                "import java.lang.String;", //
                "public class Test$$IntentBuilder {", //
                "  private Intent intent;", //
                "  private Bundler bundler = Bundler.create();", //
                "  public Test$$IntentBuilder(Context context) {", //
                "    intent = new Intent(context, Test.class);", //
                "  }", //
                "  public Test$$IntentBuilder.AllSet key(String key) {", //
                "    bundler.put(\"key\",key);", //
                "    return new Test$$IntentBuilder.AllSet();", //
                "  }", //
                "  public class AllSet {", //
                "    public Intent get() {", //
                "      intent.putExtras(bundler.get());", //
                "      return intent;", //
                "    }", //
                "  }", //
                "}" //
        ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
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
            "import android.content.Context;", //
            "import android.content.Intent;", //
            "import com.f2prateek.dart.henson.Bundler;", //
            "import java.lang.Boolean;", //
            "import java.lang.Byte;", //
            "import java.lang.Character;", //
            "import java.lang.Double;", //
            "import java.lang.Float;", //
            "import java.lang.Integer;", //
            "import java.lang.Long;", //
            "import java.lang.Short;", //
            "public class Test$$IntentBuilder {", //
            "  private Intent intent;", //
            "  private Bundler bundler = Bundler.create();", //
            "  public Test$$IntentBuilder(Context context) {", //
            "    intent = new Intent(context, Test.class);", //
            "  }", //
            "  public Test$$IntentBuilder.AfterSettingKey_bool key_bool(Boolean key_bool) {", //
            "    bundler.put(\"key_bool\",key_bool);", //
            "    return new Test$$IntentBuilder.AfterSettingKey_bool();", //
            "  }", //
            "  public class AfterSettingKey_bool {", //
            "    public Test$$IntentBuilder.AfterSettingKey_byte key_byte(Byte key_byte) {", //
            "      bundler.put(\"key_byte\",key_byte);", //
            "      return new Test$$IntentBuilder.AfterSettingKey_byte();", //
            "    }", //
            "  }", //
            "  public class AfterSettingKey_byte {", //
            "    public Test$$IntentBuilder.AfterSettingKey_char key_char(Character key_char) {", //
            "      bundler.put(\"key_char\",key_char);", //
            "      return new Test$$IntentBuilder.AfterSettingKey_char();", //
            "    }", //
            "  }", //
            "  public class AfterSettingKey_char {", //
            "    public Test$$IntentBuilder.AfterSettingKey_double key_double(Double key_double) {", //
            "      bundler.put(\"key_double\",key_double);", //
            "      return new Test$$IntentBuilder.AfterSettingKey_double();", //
            "    }", //
            "  }", //
            "  public class AfterSettingKey_double {", //
            "    public Test$$IntentBuilder.AfterSettingKey_float key_float(Float key_float) {", //
            "      bundler.put(\"key_float\",key_float);", //
            "      return new Test$$IntentBuilder.AfterSettingKey_float();", //
            "    }", //
            "  }", //
            "  public class AfterSettingKey_float {", //
            "    public Test$$IntentBuilder.AfterSettingKey_int key_int(Integer key_int) {", //
            "      bundler.put(\"key_int\",key_int);", //
            "      return new Test$$IntentBuilder.AfterSettingKey_int();", //
            "    }", //
            "  }", //
            "  public class AfterSettingKey_int {", //
            "    public Test$$IntentBuilder.AfterSettingKey_long key_long(Long key_long) {", //
            "      bundler.put(\"key_long\",key_long);", //
            "      return new Test$$IntentBuilder.AfterSettingKey_long();", //
            "    }", //
            "  }", //
            "  public class AfterSettingKey_long {", //
            "    public Test$$IntentBuilder.AllSet key_short(Short key_short) {", //
            "      bundler.put(\"key_short\",key_short);", //
            "      return new Test$$IntentBuilder.AllSet();", //
            "    }", //
            "  }", //
            "  public class AllSet {", //
            "    public Intent get() {", //
            "      intent.putExtras(bundler.get());", //
            "      return intent;", //
            "    }", //
            "  }", //
            "}" //
        ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
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
                "import android.content.Context;", //
                "import android.content.Intent;", //
                "import com.f2prateek.dart.henson.Bundler;", //
                "import java.lang.String;", //
                "public class Test$$IntentBuilder {", //
                "  private Intent intent;", //
                "  private Bundler bundler = Bundler.create();", //
                "  public Test$$IntentBuilder(Context context) {", //
                "    intent = new Intent(context, Test.class);", //
                "  }", //
                "  public Test$$IntentBuilder.AllSet key(String key) {", //
                "    bundler.put(\"key\",key);", //
                "    return new Test$$IntentBuilder.AllSet();", //
                "  }", //
                "  public class AllSet {", //
                "    public Intent get() {", //
                "      intent.putExtras(bundler.get());", //
                "      return intent;", //
                "    }", //
                "  }", //
                "}" //
        ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
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
        JavaFileObjects.forSourceString("test/Test$$IntentBuilder", Joiner.on('\n').join( //
            "package test;", //
                "import android.content.Context;", //
                "import android.content.Intent;", //
                "import com.f2prateek.dart.henson.Bundler;", //
                "import java.lang.String;", //
                "public class Test$$IntentBuilder {", //
                "  private Intent intent;", //
                "  private Bundler bundler = Bundler.create();", //
                "  public Test$$IntentBuilder(Context context) {", //
                "    intent = new Intent(context, Test.class);", //
                "  }", //
                "  public Test$$IntentBuilder.AllSet key(String key) {", //
                "    bundler.put(\"key\",key);", //
                "    return new Test$$IntentBuilder.AllSet();", //
                "  }", //
                "  public class AllSet {", //
                "    public Intent get() {", //
                "      intent.putExtras(bundler.get());", //
                "      return intent;", //
                "    }", //
                "  }", //
                "}" //
        ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
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
        JavaFileObjects.forSourceString("test/Test_Bundler", Joiner.on('\n').join( //
            "package test;", //
                "import android.content.Context;", //
                "import android.content.Intent;", //
                "import com.f2prateek.dart.henson.Bundler;", //
                "import java.lang.String;", //
                "public class Test$$IntentBuilder {", //
                "  private Intent intent;", //
                "  private Bundler bundler = Bundler.create();", //
                "  public Test$$IntentBuilder(Context context) {", //
                "    intent = new Intent(context, Test.class);", //
                "  }", //
                "  public Test$$IntentBuilder.AllSet key(String key) {", //
                "    bundler.put(\"key\",key);", //
                "    return new Test$$IntentBuilder.AllSet();", //
                "  }", //
                "  public class AllSet {", //
                "    public Intent get() {", //
                "      intent.putExtras(bundler.get());", //
                "      return intent;", //
                "    }", //
                "  }", //
                "}" //
                ));

    JavaFileObject expectedSource2 =
        JavaFileObjects.forSourceString("test/TestOne_Bundler", Joiner.on('\n').join( //
            "package test;", //
                "import android.content.Context;", //
                "import android.content.Intent;", //
                "import com.f2prateek.dart.henson.Bundler;", //
                "import java.lang.String;", //
                "public class TestOne$$IntentBuilder {", //
                "  private Intent intent;", //
                "  private Bundler bundler = Bundler.create();", //
                "  public TestOne$$IntentBuilder(Context context) {", //
                "    intent = new Intent(context, TestOne.class);", //
                "  }", //
                "  public TestOne$$IntentBuilder.AllSet key(String key) {", //
                "    bundler.put(\"key\",key);", //
                "    return new TestOne$$IntentBuilder.AllSet();", //
                "  }", //
                "  public class AllSet {", //
                "    public Intent get() {", //
                "      intent.putExtras(bundler.get());", //
                "      return intent;", //
                "    }", //
                "  }", //
                "}" //
        ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
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
        JavaFileObjects.forSourceString("test/Test$$IntentBuilder", Joiner.on('\n').join( //
            "package test;", //
            "import android.content.Context;", //
            "import android.content.Intent;", //
            "import com.f2prateek.dart.henson.Bundler;", //
            "import java.lang.String;", //
            "public class Test$$IntentBuilder {", //
            "  private Intent intent;", //
            "  private Bundler bundler = Bundler.create();", //
            "  public Test$$IntentBuilder(Context context) {", //
            "    intent = new Intent(context, Test.class);", //
            "  }", //
            "  public Test$$IntentBuilder.AllSet key(String key) {", //
            "    bundler.put(\"key\",key);", //
            "    return new Test$$IntentBuilder.AllSet();", //
            "  }", //
            "  public class AllSet {", //
            "    public Intent get() {", //
            "      intent.putExtras(bundler.get());", //
            "      return intent;", //
            "    }", //
            "  }", //
            "}" //
        ));

    JavaFileObject expectedSource2 =
        JavaFileObjects.forSourceString("test/TestOne_Bundler", Joiner.on('\n').join( //
            "package test;", //

            "import android.content.Context;", //
            "import android.content.Intent;", //
            "import com.f2prateek.dart.henson.Bundler;", //
            "import java.lang.String;", //
            "public class TestOne$$IntentBuilder {", //
            "  private Intent intent;", //
            "  private Bundler bundler = Bundler.create();", //
            "  public TestOne$$IntentBuilder(Context context) {", //
            "    intent = new Intent(context, TestOne.class);", //
            "  }", //
            "  public TestOne$$IntentBuilder.AllSet key(String key) {", //
            "    bundler.put(\"key\",key);", //
            "    return new TestOne$$IntentBuilder.AllSet();", //
            "  }", //
            "  public class AllSet {", //
            "    public Intent get() {", //
            "      intent.putExtras(bundler.get());", //
            "      return intent;", //
            "    }", //
            "  }", //
            "}" //
        ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
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

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test/Test_Bundler", Joiner.on('\n').join( //
            "package test;", //
            "import android.content.Context;", //
            "import android.content.Intent;", //
            "import com.f2prateek.dart.henson.Bundler;", //
            "public class Test$$IntentBuilder {", //
            "  private Intent intent;", //
            "  private Bundler bundler = Bundler.create();", //
            "  public Test$$IntentBuilder(Context context) {", //
            "    intent = new Intent(context, Test.class);", //
            "  }", //
            "  public Test$$IntentBuilder.AllSet key(ExampleParcel key) {", //
            "    bundler.put(\"key\",org.parceler.Parcels.wrap(key));", //
            "    return new Test$$IntentBuilder.AllSet();", //
            "  }", //
            "  public class AllSet {", //
            "    public Intent get() {", //
            "      intent.putExtras(bundler.get());", //
            "      return intent;", //
            "    }", //
            "  }", //
            "}" //
        ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }

  @Test public void injectingOptionalExtra() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "import com.f2prateek.dart.Nullable;", //
        "public class Test extends Activity {", //
        "    @InjectExtra(\"key\") String extra;", //
        "    @InjectExtra(\"key2\") @Nullable String extra2;", //
        "}" //
    ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test/Test$$IntentBuilder", Joiner.on('\n').join( //
            "package test;", //
            "import android.content.Context;", //
            "import android.content.Intent;", //
            "import com.f2prateek.dart.henson.Bundler;", //
            "import java.lang.String;", //
            "public class Test$$IntentBuilder {", //
            "  private Intent intent;", //
            "  private Bundler bundler = Bundler.create();", //
            "  public Test$$IntentBuilder(Context context) {", //
            "    intent = new Intent(context, Test.class);", //
            "  }", //
            "  public Test$$IntentBuilder.AllSet key(String key) {", //
            "    bundler.put(\"key\",key);", //
            "    return new Test$$IntentBuilder.AllSet();", //
            "  }", //
            "  public class AllSet {", //
            "    public Test$$IntentBuilder.AllSet key2(String key2) {", //
            "      bundler.put(\"key2\",key2);", //
            "      return this;", //
            "    }", //
            "    public Intent get() {", //
            "      intent.putExtras(bundler.get());", //
            "      return intent;", //
            "    }", //
            "  }", //
            "}" //
        ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }

  @Test public void injectingOptionalOnlyExtra() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "import com.f2prateek.dart.Nullable;", //
        "public class Test extends Activity {", //
        "    @InjectExtra(\"key\") @Nullable String extra;", //
        "}" //
    ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test/Test$$IntentBuilder", Joiner.on('\n').join( //
            "package test;", //
            "import android.content.Context;", //
            "import android.content.Intent;", //
            "import com.f2prateek.dart.henson.Bundler;", //
            "import java.lang.String;", //
            "public class Test$$IntentBuilder {", //
            "  private Intent intent;", //
            "  private Bundler bundler = Bundler.create();", //
            "  public Test$$IntentBuilder(Context context) {", //
            "    intent = new Intent(context, Test.class);", //
            "  }", //
            "  public Test$$IntentBuilder key(String key) {", //
            "    bundler.put(\"key\",key);", //
            "    return this;", //
            "  }", //
            "  public Intent get() {", //
            "    intent.putExtras(bundler.get());", //
            "    return intent;", //
            "  }", //
            "}" //
        ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }
}
