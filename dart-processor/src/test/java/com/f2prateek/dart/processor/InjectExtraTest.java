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

package com.f2prateek.dart.processor;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static com.google.common.truth.Truth.assert_;

/**
 * Tests {@link com.f2prateek.dart.processor.InjectExtraProcessor}.
 * For tests not related to Parceler.
 */
public class InjectExtraTest {

  @Test public void testIsDebugDisabled() {
    boolean isDebugEnabled = new InjectExtraProcessor().isDebugEnabled();
    assertThat(isDebugEnabled).isFalse();
  }

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
            "import com.f2prateek.dart.Dart;", //
            "import java.lang.Object;", //
            "import java.lang.String;", //
            "public class Test$$ExtraInjector {", //
            "  public static void inject(Dart.Finder finder, Test target, Object source) {", //
            "    Object object;", //
            "    object = finder.getExtra(source, \"key\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key' for field 'extra' was not found. If this extra is optional add '@Nullable' annotation.\");",
            //
            "    }", //
            "    target.extra = (String) object;", //
            "  }", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.dartProcessorsWithoutParceler())
        .compilesWithoutError()
        .and()
        .generatesSources(injectorSource);
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
            "import com.f2prateek.dart.Dart;", //
            "import java.lang.Object;", //
            "public class Test$$ExtraInjector {", //
            "  public static void inject(Dart.Finder finder, Test target, Object source) {", //
            "    Object object;", //
            "    object = finder.getExtra(source, \"key_bool\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key_bool' for field 'aBool' was not found. If this extra is optional add '@Nullable' annotation.\");",
            //
            "    }", //
            "    target.aBool = (boolean) object;", //
            "    object = finder.getExtra(source, \"key_byte\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key_byte' for field 'aByte' was not found. If this extra is optional add '@Nullable' annotation.\");",
            //
            "    }", //
            "    target.aByte = (byte) object;", //
            "    object = finder.getExtra(source, \"key_short\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key_short' for field 'aShort' was not found. If this extra is optional add '@Nullable' annotation.\");",
            //
            "    }", //
            "    target.aShort = (short) object;", //
            "    object = finder.getExtra(source, \"key_int\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key_int' for field 'anInt' was not found. If this extra is optional add '@Nullable' annotation.\");",
            //
            "    }", //
            "    target.anInt = (int) object;", //
            "    object = finder.getExtra(source, \"key_long\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key_long' for field 'aLong' was not found. If this extra is optional add '@Nullable' annotation.\");",
            //
            "    }", //
            "    target.aLong = (long) object;", //
            "    object = finder.getExtra(source, \"key_char\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key_char' for field 'aChar' was not found. If this extra is optional add '@Nullable' annotation.\");",
            //
            "    }", //
            "    target.aChar = (char) object;", //
            "    object = finder.getExtra(source, \"key_float\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key_float' for field 'aFloat' was not found. If this extra is optional add '@Nullable' annotation.\");",
            //
            "    }", //
            "    target.aFloat = (float) object;", //
            "    object = finder.getExtra(source, \"key_double\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key_double' for field 'aDouble' was not found. If this extra is optional add '@Nullable' annotation.\");",
            //
            "    }", //
            "    target.aDouble = (double) object;", //
            "  }", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.dartProcessorsWithoutParceler())
        .compilesWithoutError()
        .and()
        .generatesSources(injectorSource);
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
            "import com.f2prateek.dart.Dart;", //
            "import java.lang.Object;", //
            "import java.lang.String;", //
            "public class Test$$ExtraInjector {", //
            "  public static void inject(Dart.Finder finder, Test target, Object source) {", //
            "    Object object;", //
            "    object = finder.getExtra(source, \"key\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key' for field 'extra1', field 'extra2', and field 'extra3' was not found. If this extra is optional add '@Nullable' annotation.\");",
            //
            "    }", //
            "    target.extra1 = (String) object;", //
            "    target.extra2 = (String) object;", //
            "    target.extra3 = (String) object;", //
            "  }", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.dartProcessorsWithoutParceler())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test public void defaultKey() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "import java.lang.Object;", //
        "import java.lang.String;", //
        "public class Test extends Activity {", //
        "    @InjectExtra String key;", //
        "}" //
    ));

    JavaFileObject expectedSource =
        JavaFileObjects.forSourceString("test/Test$$ExtraInjector", Joiner.on('\n').join( //
            "package test;", //
            "import com.f2prateek.dart.Dart;", //
            "import java.lang.Object;", //
            "import java.lang.String;", //
            "public class Test$$ExtraInjector {", //
            "  public static void inject(Dart.Finder finder, Test target, Object source) {", //
            "    Object object;", //
            "    object = finder.getExtra(source, \"key\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key' for field 'key' was not found. If this extra is optional add '@Nullable' annotation.\");",
            //
            "    }", //
            "    target.key = (String) object;", //
            "  }", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.dartProcessorsWithoutParceler())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test public void fieldVisibility() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "import java.lang.Object;", //
        "import java.lang.String;", //
        "public class Test extends Activity {", //
        "    @InjectExtra(\"key_1\") String extra1;", //
        "    @InjectExtra(\"key_2\") String extra2;", //
        "    @InjectExtra(\"key_3\") String extra3;", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.dartProcessorsWithoutParceler())
        .compilesWithoutError();
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
            "import com.f2prateek.dart.Dart;", //
            "import java.lang.Object;", //
            "import java.lang.String;", //
            "public class Test$$ExtraInjector {", //
            "  public static void inject(Dart.Finder finder, Test target, Object source) {", //
            "    Object object;", //
            "    object = finder.getExtra(source, \"key\");", //
            "    if (object != null) {", //
            "      target.extra = (String) object;", //
            "    }", //
            "  }", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.dartProcessorsWithoutParceler())
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
        "import java.lang.Object;", //
        "import java.lang.String;", //
        "public class Test extends Activity {", //
        "  @Nullable @InjectExtra(\"key\") String extra;", //
        "}" //
    ));

    JavaFileObject expectedSource =
        JavaFileObjects.forSourceString("test/Test$$ExtraInjector", Joiner.on('\n').join( //
            "package test;", //
            "import com.f2prateek.dart.Dart;", //
            "import java.lang.Object;", //
            "import java.lang.String;", //
            "public class Test$$ExtraInjector {", //
            "  public static void inject(Dart.Finder finder, Test target, Object source) {", //
            "    Object object;", //
            "    object = finder.getExtra(source, \"key\");", //
            "    if (object != null) {", //
            "      target.extra = (String) object;", //
            "    }", //
            "  }", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.dartProcessorsWithoutParceler())
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

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.dartProcessorsWithoutParceler())
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

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.dartProcessorsWithoutParceler())
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

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.dartProcessorsWithoutParceler())
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

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.dartProcessorsWithoutParceler())
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
            "import com.f2prateek.dart.Dart;", //
            "import java.lang.Object;", //
            "import java.lang.String;", //
            "public class Test$$ExtraInjector {", //
            "  public static void inject(Dart.Finder finder, Test target, Object source) {", //
            "    Object object;", //
            "    object = finder.getExtra(source, \"key\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key' for field 'extra' was not found. If this extra is optional add '@Nullable' annotation.\");",
            //
            "    }", //
            "    target.extra = (String) object;", //
            "  }", //
            "}" //
        ));

    JavaFileObject expectedSource2 =
        JavaFileObjects.forSourceString("test/TestOne$$ExtraInjector", Joiner.on('\n').join( //
            "package test;", //
            "import com.f2prateek.dart.Dart;", //
            "import java.lang.Object;", //
            "import java.lang.String;", //
            "public class TestOne$$ExtraInjector {", //
            "  public static void inject(Dart.Finder finder, TestOne target, Object source) {", //
            "    Test$$ExtraInjector.inject(finder, target, source);", //
            "    Object object;", //
            "    object = finder.getExtra(source, \"key\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key' for field 'extra1' was not found. If this extra is optional add '@Nullable' annotation.\");",
            //
            "    }", //
            "    target.extra1 = (String) object;", //
            "  }", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.dartProcessorsWithoutParceler())
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
            "import com.f2prateek.dart.Dart;", //
            "import java.lang.Object;", //
            "import java.lang.String;", //
            "public class Test$$ExtraInjector {", //
            "  public static void inject(Dart.Finder finder, Test target, Object source) {", //
            "    Object object;", //
            "    object = finder.getExtra(source, \"key\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key' for field 'extra' was not found. If this extra is optional add '@Nullable' annotation.\");",
            //
            "    }", //
            "    target.extra = (String) object;", //
            "  }", //
            "}" //
        ));

    JavaFileObject expectedSource2 =
        JavaFileObjects.forSourceString("test/TestOne$$ExtraInjector", Joiner.on('\n').join( //
            "package test;", //
            "import com.f2prateek.dart.Dart;", //
            "import java.lang.Object;", //
            "import java.lang.String;", //
            "public class TestOne$$ExtraInjector {", //
            "  public static void inject(Dart.Finder finder, TestOne target, Object source) {",
            "    Test$$ExtraInjector.inject(finder, target, source);", //
            "    Object object;", //
            "    object = finder.getExtra(source, \"key\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key' for field 'extra1' was not found. If this extra is optional add '@Nullable' annotation.\");",
            "    }", //
            "    target.extra1 = (String) object;", //
            "  }", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.dartProcessorsWithoutParceler())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource1, expectedSource2);
  }
}
