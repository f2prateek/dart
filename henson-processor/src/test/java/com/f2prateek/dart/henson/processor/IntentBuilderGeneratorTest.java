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
import javax.annotation.processing.Processor;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static com.google.common.truth.Truth.assert_;

/**
 * Tests {@link com.f2prateek.dart.henson.processor.HensonExtraProcessor}.
 * For tests not related to Parceler.
 */
public class IntentBuilderGeneratorTest {

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
            "  public Test$$IntentBuilder.AllSet key(String extra) {", //
            "    bundler.put(\"key\",extra);", //
            "    return new Test$$IntentBuilder.AllSet();", //
            "  }", //
            "  public class AllSet {", //
            "    public Intent build() {", //
            "      intent.putExtras(bundler.get());", //
            "      return intent;", //
            "    }", //
            "  }", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessorsWithoutParceler())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }

  @Test public void hensonNavigable() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.HensonNavigable;", //
        "@HensonNavigable public class Test extends Activity {", //
        "}" //
    ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test/Test$$IntentBuilder", Joiner.on('\n').join( //
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
            "  public Intent build() {", //
            "    intent.putExtras(bundler.get());", //
            "    return intent;", //
            "  }", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessorsWithoutParceler())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }

  @Test public void hensonNavigable_withModel() {
    JavaFileObject source =
        JavaFileObjects.forSourceString("test.TestModel", Joiner.on('\n').join( //
            "package test;", //
            "import android.app.Activity;", //
            "import com.f2prateek.dart.InjectExtra;", //
            "import com.f2prateek.dart.HensonNavigable;", //
            "@HensonNavigable(model = Foo.class) public class TestModel extends Activity {", //
            "}", //
            "class Foo {", //
            "  @InjectExtra String extra;", //
            "}" //
        ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test/TestModel$$IntentBuilder", Joiner.on('\n').join( //
            "package test;", //
            "import android.content.Context;", //
            "import android.content.Intent;", //
            "import com.f2prateek.dart.henson.Bundler;", //
            "import java.lang.String;", //
            "public class TestModel$$IntentBuilder {", //
            "  private Intent intent;", //
            "  private Bundler bundler = Bundler.create();", //
            "  public TestModel$$IntentBuilder(Context context) {", //
            "    intent = new Intent(context, TestModel.class);", //
            "  }", //
            "  public TestModel$$IntentBuilder.AllSet extra(String extra) {", //
            "    bundler.put(\"extra\", extra);", //
            "    return new TestModel$$IntentBuilder.AllSet();", //
            "  }", //
            "  public class AllSet {", //
            "    public Intent build() {", //
            "      intent.putExtras(bundler.get());", //
            "      return intent;", //
            "    }", //
            "  }", //
            "}"
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessorsWithoutParceler())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }

  @Test public void hensonNavigable_with_extras() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "import com.f2prateek.dart.HensonNavigable;", //
        "@HensonNavigable public class Test extends Activity {", //
        "    @InjectExtra(\"key\") String extra;", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessorsWithoutParceler())
        .failsToCompile()
        .withErrorContaining(
            "@HensonNavigable class Test must not contain any @InjectExtra annotation");
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
            "public class Test$$IntentBuilder {", //
            "  private Intent intent;", //
            "  private Bundler bundler = Bundler.create();", //
            "  public Test$$IntentBuilder(Context context) {", //
            "    intent = new Intent(context, Test.class);", //
            "  }", //
            "  public Test$$IntentBuilder.AfterSettingKey_bool key_bool(boolean aBool) {", //
            "    bundler.put(\"key_bool\",aBool);", //
            "    return new Test$$IntentBuilder.AfterSettingKey_bool();", //
            "  }", //
            "  public class AfterSettingKey_bool {", //
            "    public Test$$IntentBuilder.AfterSettingKey_byte key_byte(byte aByte) {", //
            "      bundler.put(\"key_byte\",aByte);", //
            "      return new Test$$IntentBuilder.AfterSettingKey_byte();", //
            "    }", //
            "  }", //
            "  public class AfterSettingKey_byte {", //
            "    public Test$$IntentBuilder.AfterSettingKey_char key_char(char aChar) {", //
            "      bundler.put(\"key_char\",aChar);", //
            "      return new Test$$IntentBuilder.AfterSettingKey_char();", //
            "    }", //
            "  }", //
            "  public class AfterSettingKey_char {", //
            "    public Test$$IntentBuilder.AfterSettingKey_double key_double(double aDouble) {", //
            "      bundler.put(\"key_double\",aDouble);", //
            "      return new Test$$IntentBuilder.AfterSettingKey_double();", //
            "    }", //
            "  }", //
            "  public class AfterSettingKey_double {", //
            "    public Test$$IntentBuilder.AfterSettingKey_float key_float(float aFloat) {", //
            "      bundler.put(\"key_float\",aFloat);", //
            "      return new Test$$IntentBuilder.AfterSettingKey_float();", //
            "    }", //
            "  }", //
            "  public class AfterSettingKey_float {", //
            "    public Test$$IntentBuilder.AfterSettingKey_int key_int(int anInt) {", //
            "      bundler.put(\"key_int\",anInt);", //
            "      return new Test$$IntentBuilder.AfterSettingKey_int();", //
            "    }", //
            "  }", //
            "  public class AfterSettingKey_int {", //
            "    public Test$$IntentBuilder.AfterSettingKey_long key_long(long aLong) {", //
            "      bundler.put(\"key_long\",aLong);", //
            "      return new Test$$IntentBuilder.AfterSettingKey_long();", //
            "    }", //
            "  }", //
            "  public class AfterSettingKey_long {", //
            "    public Test$$IntentBuilder.AllSet key_short(short aShort) {", //
            "      bundler.put(\"key_short\",aShort);", //
            "      return new Test$$IntentBuilder.AllSet();", //
            "    }", //
            "  }", //
            "  public class AllSet {", //
            "    public Intent build() {", //
            "      intent.putExtras(bundler.get());", //
            "      return intent;", //
            "    }", //
            "  }", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessorsWithoutParceler())
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
            "  public Test$$IntentBuilder.AllSet key(String extra1) {", //
            "    bundler.put(\"key\",extra1);", //
            "    return new Test$$IntentBuilder.AllSet();", //
            "  }", //
            "  public class AllSet {", //
            "    public Intent build() {", //
            "      intent.putExtras(bundler.get());", //
            "      return intent;", //
            "    }", //
            "  }", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessorsWithoutParceler())
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
            "    public Intent build() {", //
            "      intent.putExtras(bundler.get());", //
            "      return intent;", //
            "    }", //
            "  }", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessorsWithoutParceler())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }

  @Test public void superclassWithSameKeys() {
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
            "  public Test$$IntentBuilder.AllSet key(String extra) {", //
            "    bundler.put(\"key\",extra);", //
            "    return new Test$$IntentBuilder.AllSet();", //
            "  }", //
            "  public class AllSet {", //
            "    public Intent build() {", //
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
            "  public TestOne$$IntentBuilder.AllSet key(String extra) {", //
            "    bundler.put(\"key\",extra);", //
            "    return new TestOne$$IntentBuilder.AllSet();", //
            "  }", //
            "  public class AllSet {", //
            "    public Intent build() {", //
            "      intent.putExtras(bundler.get());", //
            "      return intent;", //
            "    }", //
            "  }", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessorsWithoutParceler())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource1, expectedSource2);
  }

  @Test public void superclassDifferentKeys() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "public class Test extends Activity {", //
        "    @InjectExtra(\"key\") String extra;", //
        "}", //
        "class TestOne extends Test {", //
        "    @InjectExtra(\"key1\") String extra1;", //
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
            "  public Test$$IntentBuilder.AllSet key(String extra) {", //
            "    bundler.put(\"key\",extra);", //
            "    return new Test$$IntentBuilder.AllSet();", //
            "  }", //
            "  public class AllSet {", //
            "    public Intent build() {", //
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
            "  public TestOne$$IntentBuilder.AfterSettingKey key(String extra) {", //
            "    bundler.put(\"key\", extra);", //
            "    return new TestOne$$IntentBuilder.AfterSettingKey();", //
            "  }", //
            "  public class AfterSettingKey {", //
            "    public TestOne$$IntentBuilder.AllSet key1(String extra1) {", //
            "      bundler.put(\"key1\", extra1);", //
            "      return new TestOne$$IntentBuilder.AllSet();", //
            "    }", //
            "  }", //
            "  public class AllSet {", //
            "    public Intent build() {", //
            "      intent.putExtras(bundler.get());", //
            "      return intent;", //
            "    }", //
            "  }", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessorsWithoutParceler())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource1, expectedSource2);
  }

  //Test for https://github.com/f2prateek/dart/issues/64
  @Test public void superclassDifferentKeys_issue64() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "import com.f2prateek.dart.HensonNavigable;", //
        "public abstract class Test extends Activity {", //
        "    @InjectExtra(\"key\") String extra;", //
        "}", //
        "@HensonNavigable class TestAwo extends TestOne {", //
        "}", //
        "@HensonNavigable class TestOne extends Test {", //
        "}" //
    ));

    JavaFileObject expectedSource =
        JavaFileObjects.forSourceString("test/TestTwo_Bundler", Joiner.on('\n').join( //
            "package test;", //
            "import android.content.Context;", //
            "import android.content.Intent;", //
            "import com.f2prateek.dart.henson.Bundler;", //
            "import java.lang.String;", //
            "public class TestAwo$$IntentBuilder {", //
            "  private Intent intent;", //
            "  private Bundler bundler = Bundler.create();", //
            "  public TestAwo$$IntentBuilder(Context context) {", //
            "    intent = new Intent(context, TestAwo.class);", //
            "  }", //
            "  public TestAwo$$IntentBuilder.AllSet key(String extra) {", //
            "    bundler.put(\"key\", extra);", //
            "    return new TestAwo$$IntentBuilder.AllSet();", //
            "  }", //
            "  public class AllSet {", //
            "    public Intent build() {", //
            "      intent.putExtras(bundler.get());", //
            "      return intent;", //
            "    }", //
            "  }", //
            "}" //
        ));
    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessorsWithoutParceler())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
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
            "  public Test$$IntentBuilder.AllSet key(String extra) {", //
            "    bundler.put(\"key\",extra);", //
            "    return new Test$$IntentBuilder.AllSet();", //
            "  }", //
            "  public class AllSet {", //
            "    public Intent build() {", //
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
            "  public TestOne$$IntentBuilder.AllSet key(String extra) {", //
            "    bundler.put(\"key\",extra);", //
            "    return new TestOne$$IntentBuilder.AllSet();", //
            "  }", //
            "  public class AllSet {", //
            "    public Intent build() {", //
            "      intent.putExtras(bundler.get());", //
            "      return intent;", //
            "    }", //
            "  }", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessorsWithoutParceler())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource1, expectedSource2);
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
            "  public Test$$IntentBuilder.AllSet key(String extra) {", //
            "    bundler.put(\"key\",extra);", //
            "    return new Test$$IntentBuilder.AllSet();", //
            "  }", //
            "  public class AllSet {", //
            "    public Test$$IntentBuilder.AllSet key2(String extra2) {", //
            "      bundler.put(\"key2\",extra2);", //
            "      return this;", //
            "    }", //
            "    public Intent build() {", //
            "      intent.putExtras(bundler.get());", //
            "      return intent;", //
            "    }", //
            "  }", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessorsWithoutParceler())
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
            "  public Test$$IntentBuilder key(String extra) {", //
            "    bundler.put(\"key\",extra);", //
            "    return this;", //
            "  }", //
            "  public Intent build() {", //
            "    intent.putExtras(bundler.get());", //
            "    return intent;", //
            "  }", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessorsWithoutParceler())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }

  @Test public void injectingBothSerializableAndParcelableExtra() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import android.os.Parcelable;", //
        "import java.io.Serializable;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "class Extra implements Serializable, Parcelable {", //
        "  public void writeToParcel(android.os.Parcel out, int flags) {", //
        "  }", //
        "  public int describeContents() {", //
        "    return 0;", //
        "  }", //
        "}", //
        "public class Test extends Activity {", //
        "    @InjectExtra(\"key\") Extra extra;", //
        "}" //
    ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test/Test$$IntentBuilder", Joiner.on('\n').join( //
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
            "  public Test$$IntentBuilder.AllSet key(Extra extra) {", //
            "    bundler.put(\"key\",(android.os.Parcelable) extra);", //
            "    return new Test$$IntentBuilder.AllSet();", //
            "  }", //
            "  public class AllSet {", //
            "    public Intent build() {", //
            "      intent.putExtras(bundler.get());", //
            "      return intent;", //
            "    }", //
            "  }", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessorsWithoutParceler())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }

  @Test public void injectingExtra_keysAreSanitized() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "public class Test extends Activity {", //
        "    @InjectExtra(\"a.b\") String extra;", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessorsWithoutParceler())
        .failsToCompile()
        .withErrorContaining("Keys have to be valid java variable identifiers.")
        .in(source)
        .onLine(5);
  }
}
