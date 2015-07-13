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

public class InjectExtraTest {

  @Test public void injectingExtra() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "public class Test extends Activity {", //
        "    @InjectExtra(\"key\") String extra;", //
        "}" //
    ));

    JavaFileObject injectorSource =
        JavaFileObjects.forSourceString("test/Test$$ExtraInjector", Joiner.on('\n').join( //
            "package test;", //
            "import com.f2prateek.dart.Dart.Finder;", //
            "public class Test$$ExtraInjector {", //
            "  public static void inject(Finder finder, final test.Test target, Object source) {",
            "    Object object;", "    object = finder.getExtra(source, \"key\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key' for field 'extra' was not found. If this extra is optional add '@Nullable' annotation.\");",
            "    }", //
            "    target.extra = (java.lang.String) object;", //
            "  }", //
            "}" //
        ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test/Test_Bundler", Joiner.on('\n').join( //
            "package test;", //
            "import android.os.Bundle;", //
            "import com.f2prateek.dart.Bundler;", //
            "public class Test_Bundler {", //
            "  private final Bundler bundler = Bundler.create();", //
            "  public Test_Bundler key(java.lang.String key) {", //
            "    bundler.put(\"key\", key);", //
            "    return this;", //
            "  }", //
            "  public Bundle get() {", //
            "    return bundler.get()", //
            "  }", //
            "}" //
        ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(dartProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(injectorSource, builderSource);
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

    JavaFileObject injectorSource =
        JavaFileObjects.forSourceString("test/Test$$ExtraInjector", Joiner.on('\n').join( //
            "package test;", //
            "import com.f2prateek.dart.Dart.Finder;", //
            "public class Test$$ExtraInjector {", //
            "  public static void inject(Finder finder, final test.Test target, Object source) {",
            "    Object object;", //
            "    object = finder.getExtra(source, \"key_bool\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key_bool' for field 'aBool' was not found. If this extra is optional add '@Nullable' annotation.\");",
            "    }", //
            "    target.aBool = (java.lang.Boolean) object;", //
            "    object = finder.getExtra(source, \"key_byte\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key_byte' for field 'aByte' was not found. If this extra is optional add '@Nullable' annotation.\");",
            "    }", //
            "    target.aByte = (java.lang.Byte) object;", //
            "    object = finder.getExtra(source, \"key_short\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key_short' for field 'aShort' was not found. If this extra is optional add '@Nullable' annotation.\");",
            "    }", //
            "    target.aShort = (java.lang.Short) object;", //
            "    object = finder.getExtra(source, \"key_int\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key_int' for field 'anInt' was not found. If this extra is optional add '@Nullable' annotation.\");",
            "    }", //
            "    target.anInt = (java.lang.Integer) object;", //
            "    object = finder.getExtra(source, \"key_long\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key_long' for field 'aLong' was not found. If this extra is optional add '@Nullable' annotation.\");",
            "    }", //
            "    target.aLong = (java.lang.Long) object;", //
            "    object = finder.getExtra(source, \"key_char\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key_char' for field 'aChar' was not found. If this extra is optional add '@Nullable' annotation.\");",
            "    }", //
            "    target.aChar = (java.lang.Character) object;", //
            "    object = finder.getExtra(source, \"key_float\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key_float' for field 'aFloat' was not found. If this extra is optional add '@Nullable' annotation.\");",
            "    }", //
            "    target.aFloat = (java.lang.Float) object;", //
            "    object = finder.getExtra(source, \"key_double\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key_double' for field 'aDouble' was not found. If this extra is optional add '@Nullable' annotation.\");",
            "    }", //
            "    target.aDouble = (java.lang.Double) object;", //
            "  }", //
            "}" //
        ));

      JavaFileObject builderSource =
          JavaFileObjects.forSourceString("test/Test_Bundler", Joiner.on('\n').join( //
              "package test;", //
              "import android.os.Bundle;", //
              "import com.f2prateek.dart.Bundler;", //
              "public class Test_Bundler {", //
              "  private final Bundler bundler = Bundler.create();", //
              "  public Test_Bundler key_bool(java.lang.Boolean key_bool) {", //
              "    bundler.put(\"key_bool\", key_bool);", //
              "    return this;", //
              "  }", //
              "  public Test_Bundler key_byte(java.lang.Byte key_byte) {", //
              "    bundler.put(\"key_byte\", key_byte);", //
              "    return this;", //
              "  }", //
              "  public Test_Bundler key_short(java.lang.Short key_short) {", //
              "    bundler.put(\"key_short\", key_short);", //
              "    return this;", //
              "  }", //
              "  public Test_Bundler key_int(java.lang.Integer key_int) {", //
              "    bundler.put(\"key_int\", key_int);", //
              "    return this;", //
              "  }", //
              "  public Test_Bundler key_long(java.lang.Long key_long) {", //
              "    bundler.put(\"key_long\", key_long);", //
              "    return this;", //
              "  }", //
              "  public Test_Bundler key_char(java.lang.Character key_char) {", //
              "    bundler.put(\"key_char\", key_char);", //
              "    return this;", //
              "  }", //
              "  public Test_Bundler key_float(java.lang.Float key_float) {", //
              "    bundler.put(\"key_float\", key_float);", //
              "    return this;", //
              "  }", //
              "  public Test_Bundler key_double(java.lang.Double key_double) {", //
              "    bundler.put(\"key_double\", key_double);", //
              "    return this;", //
              "  }", //
              "  public Bundle get() {", //
              "    return bundler.get()", //
              "  }", //
              "}" //
          ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(dartProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(injectorSource, builderSource);
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
        JavaFileObjects.forSourceString("test/Test$$ExtraInjector", Joiner.on('\n').join( //
            "package test;", //
            "import com.f2prateek.dart.Dart.Finder;", //
            "public class Test$$ExtraInjector {", //
            "  public static void inject(Finder finder, final test.Test target, Object source) {",
            "    Object object;", "    object = finder.getExtra(source, \"key\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key' for field 'extra1', field 'extra2', and field 'extra3' was not found. If this extra is optional add '@Nullable' annotation.\");",
            "    }", //
            "    target.extra1 = (java.lang.String) object;", //
            "    target.extra2 = (java.lang.String) object;", //
            "    target.extra3 = (java.lang.String) object;", //
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
        JavaFileObjects.forSourceString("test/Test$$ExtraInjector", Joiner.on('\n').join( //
            "package test;", //
            "import com.f2prateek.dart.Dart.Finder;", //
            "public class Test$$ExtraInjector {", //
            "  public static void inject(Finder finder, final test.Test target, Object source) {",
            "    Object object;", "    object = finder.getExtra(source, \"key\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key' for field 'key' was not found. If this extra is optional add '@Nullable' annotation.\");",
            "    }", //
            "    target.key = (java.lang.String) object;", //
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

  @Test public void fieldVisibility() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "public class Test extends Activity {", //
        "    @InjectExtra(\"key_1\") String extra1;", //
        "    @InjectExtra(\"key_2\") String extra2;", //
        "    @InjectExtra(\"key_3\") String extra3;", //
        "}" //
    ));

    ASSERT.about(javaSource()).that(source).processedWith(dartProcessors()).compilesWithoutError();
  }

  @Test public void optional() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "import com.f2prateek.dart.Optional;", //
        "public class Test extends Activity {", //
        "  @Optional @InjectExtra(\"key\") String extra;", //
        "}" //
    ));

    JavaFileObject expectedSource =
        JavaFileObjects.forSourceString("test/Test$$ExtraInjector", Joiner.on('\n').join( //
            "package test;", //
            "import com.f2prateek.dart.Dart.Finder;", //
            "public class Test$$ExtraInjector {", //
            "  public static void inject(Finder finder, final test.Test target, Object source) {",
            "    Object object;", //
            "    object = finder.getExtra(source, \"key\");", //
            "    if (object != null) {", //
            "    target.extra = (java.lang.String) object;", //
            "    }", //
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

  @Test public void nullable() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "import com.f2prateek.dart.Nullable;", //
        "public class Test extends Activity {", //
        "  @Nullable @InjectExtra(\"key\") String extra;", //
        "}" //
    ));

    JavaFileObject expectedSource =
        JavaFileObjects.forSourceString("test/Test$$ExtraInjector", Joiner.on('\n').join( //
            "package test;", //
            "import com.f2prateek.dart.Dart.Finder;", //
            "public class Test$$ExtraInjector {", //
            "  public static void inject(Finder finder, final test.Test target, Object source) {",
            "    Object object;", //
            "    object = finder.getExtra(source, \"key\");", //
            "    if (object != null) {", //
            "    target.extra = (java.lang.String) object;", //
            "    }", //
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

  @Test public void failsIfInPrivateClass() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "public class Test {", //
        "  private static class Inner {", //
        "    @InjectExtra(\"key\") String extra;", //
        "  }", //
        "}" //
    ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(dartProcessors())
        .failsToCompile()
        .withErrorContaining(
            String.format("@InjectExtra fields may not be contained in private classes. (%s)",
                "test.Test.Inner.extra"))
        .in(source)
        .onLine(5);
  }

  @Test public void failsIfPrivate() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "public class Test extends Activity {", //
        "    @InjectExtra(\"key\") private String extra;", //
        "}" //
    ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(dartProcessors())
        .failsToCompile()
        .withErrorContaining(
            String.format("@InjectExtra fields must not be private or static. (%s)",
                "test.Test.extra"))
        .in(source)
        .onLine(5);
  }

  @Test public void failsIfStatic() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "public class Test extends Activity {", //
        "    @InjectExtra(\"key\") static String extra;", //
        "}" //
    ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(dartProcessors())
        .failsToCompile()
        .withErrorContaining(
            String.format("@InjectExtra fields must not be private or static. (%s)",
                "test.Test.extra"))
        .in(source)
        .onLine(5);
  }

  @Test public void failsIfInInterface() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "public interface Test {", //
        "    @InjectExtra(\"key\") String extra = null;", //
        "}" //
    ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(dartProcessors())
        .failsToCompile()
        .withErrorContaining(
            String.format("@InjectExtra fields may only be contained in classes. (%s)",
                "test.Test.extra"))
        .in(source)
        .onLine(4);
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
        JavaFileObjects.forSourceString("test/Test$$ExtraInjector", Joiner.on('\n').join( //
            "package test;", //
            "import com.f2prateek.dart.Dart.Finder;", //
            "public class Test$$ExtraInjector {", //
            "  public static void inject(Finder finder, final test.Test target, Object source) {",
            "    Object object;", //
            "    object = finder.getExtra(source, \"key\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key' for field 'extra' was not found. If this extra is optional add '@Nullable' annotation.\");",
            "    }", //
            "    target.extra = (java.lang.String) object;", //
            "  }", //
            "}" //
        ));

    JavaFileObject expectedSource2 =
        JavaFileObjects.forSourceString("test/TestOne$$ExtraInjector", Joiner.on('\n').join( //
            "package test;", //
            "import com.f2prateek.dart.Dart.Finder;", //
            "public class TestOne$$ExtraInjector {", //
            "  public static void inject(Finder finder, final test.TestOne target, Object source) {",
            "    test.Test$$ExtraInjector.inject(finder, target, source);", //
            "    Object object;", //
            "    object = finder.getExtra(source, \"key\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key' for field 'extra1' was not found. If this extra is optional add '@Nullable' annotation.\");",
            "    }", //
            "    target.extra1 = (java.lang.String) object;", //
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
        JavaFileObjects.forSourceString("test/Test$$ExtraInjector", Joiner.on('\n').join( //
            "package test;", //
            "import com.f2prateek.dart.Dart.Finder;", //
            "public class Test$$ExtraInjector {", //
            "  public static void inject(Finder finder, final test.Test target, Object source) {",
            "    Object object;", //
            "    object = finder.getExtra(source, \"key\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key' for field 'extra' was not found. If this extra is optional add '@Nullable' annotation.\");",
            "    }", //
            "    target.extra = (java.lang.String) object;", //
            "  }", //
            "}" //
        ));

    JavaFileObject expectedSource2 = JavaFileObjects.forSourceString("test/TestOne$$ExtraInjector",
        Joiner.on('\n').join("package test;", //
            "import com.f2prateek.dart.Dart.Finder;", //
            "public class TestOne$$ExtraInjector {", //
            "  public static void inject(Finder finder, final test.TestOne target, Object source) {",
            "    test.Test$$ExtraInjector.inject(finder, target, source);", //
            "    Object object;", //
            "    object = finder.getExtra(source, \"key\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key' for field 'extra1' was not found. If this extra is optional add '@Nullable' annotation.\");",
            "    }", //
            "    target.extra1 = (java.lang.String) object;", //
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
}
