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

package dart.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static dart.processor.ProcessorTestUtilities.dartProcessorsWithoutParceler;

import com.google.common.base.Joiner;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

/** Tests {@link dart.processor.InjectExtraProcessor}. For tests not related to Parceler. */
public class BindExtraTest {

  @Test
  public void testIsDebugDisabled() {
    //boolean isDebugEnabled = new InjectExtraProcessor().isDebugEnabled();
    //assertThat(isDebugEnabled).isFalse();
  }

  @Test
  public void bindingExtra() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.Test",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import android.app.Activity;", //
                    "import dart.BindExtra;", //
                    "public class Test extends Activity {", //
                    "    @BindExtra(\"key\") String extra;", //
                    "}" //
                    ));

    JavaFileObject binderSource =
        JavaFileObjects.forSourceString(
            "test/Test__ExtraBinder",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import dart.Dart;", //
                    "import java.lang.Object;", //
                    "import java.lang.String;", //
                    "public class Test__ExtraBinder {", //
                    "  public static void bind(Dart.Finder finder, Test target, Object source) {", //
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

    Compilation compilation =
        javac().withProcessors(dartProcessorsWithoutParceler()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test/Test__ExtraBinder")
        .hasSourceEquivalentTo(binderSource);
  }

  @Test
  public void bindingAllPrimitives() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.Test",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import android.app.Activity;", //
                    "import dart.BindExtra;", //
                    "public class Test extends Activity {", //
                    "    @BindExtra(\"key_bool\") boolean aBool;", //
                    "    @BindExtra(\"key_byte\") byte aByte;", //
                    "    @BindExtra(\"key_short\") short aShort;", //
                    "    @BindExtra(\"key_int\") int anInt;", //
                    "    @BindExtra(\"key_long\") long aLong;", //
                    "    @BindExtra(\"key_char\") char aChar;", //
                    "    @BindExtra(\"key_float\") float aFloat;", //
                    "    @BindExtra(\"key_double\") double aDouble;", //
                    "}" //
                    ));

    JavaFileObject binderSource =
        JavaFileObjects.forSourceString(
            "test/Test__ExtraBinder",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import dart.Dart;", //
                    "import java.lang.Object;", //
                    "public class Test__ExtraBinder {", //
                    "  public static void bind(Dart.Finder finder, Test target, Object source) {", //
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

    Compilation compilation =
        javac().withProcessors(dartProcessorsWithoutParceler()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test/Test__ExtraBinder")
        .hasSourceEquivalentTo(binderSource);
  }

  @Test
  public void oneFindPerKey() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.Test",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import android.app.Activity;", //
                    "import dart.BindExtra;", //
                    "public class Test extends Activity {", //
                    "    @BindExtra(\"key\") String extra1;", //
                    "    @BindExtra(\"key\") String extra2;", //
                    "    @BindExtra(\"key\") String extra3;", //
                    "}" //
                    ));

    JavaFileObject expectedSource =
        JavaFileObjects.forSourceString(
            "test/Test__ExtraBinder",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import dart.Dart;", //
                    "import java.lang.Object;", //
                    "import java.lang.String;", //
                    "public class Test__ExtraBinder {", //
                    "  public static void bind(Dart.Finder finder, Test target, Object source) {", //
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

    Compilation compilation =
        javac().withProcessors(dartProcessorsWithoutParceler()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test/Test__ExtraBinder")
        .hasSourceEquivalentTo(expectedSource);
  }

  @Test
  public void defaultKey() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.Test",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import android.app.Activity;", //
                    "import dart.BindExtra;", //
                    "import java.lang.Object;", //
                    "import java.lang.String;", //
                    "public class Test extends Activity {", //
                    "    @BindExtra String key;", //
                    "}" //
                    ));

    JavaFileObject expectedSource =
        JavaFileObjects.forSourceString(
            "test/Test__ExtraBinder",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import dart.Dart;", //
                    "import java.lang.Object;", //
                    "import java.lang.String;", //
                    "public class Test__ExtraBinder {", //
                    "  public static void bind(Dart.Finder finder, Test target, Object source) {", //
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

    Compilation compilation =
        javac().withProcessors(dartProcessorsWithoutParceler()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test/Test__ExtraBinder")
        .hasSourceEquivalentTo(expectedSource);
  }

  @Test
  public void fieldVisibility() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.Test",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import android.app.Activity;", //
                    "import dart.BindExtra;", //
                    "import java.lang.Object;", //
                    "import java.lang.String;", //
                    "public class Test extends Activity {", //
                    "    @BindExtra(\"key_1\") String extra1;", //
                    "    @BindExtra(\"key_2\") String extra2;", //
                    "    @BindExtra(\"key_3\") String extra3;", //
                    "}" //
                    ));

    Compilation compilation =
        javac().withProcessors(dartProcessorsWithoutParceler()).compile(source);
    assertThat(compilation).succeededWithoutWarnings();
  }

  @Test
  public void nullable() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.Test",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import android.app.Activity;", //
                    "import dart.BindExtra;", //
                    "import java.lang.Object;", //
                    "import java.lang.String;", //
                    "import java.lang.annotation.Retention;", //
                    "import java.lang.annotation.Target;", //
                    "import static java.lang.annotation.ElementType.FIELD;", //
                    "import static java.lang.annotation.RetentionPolicy.CLASS;", //
                    "public class Test extends Activity {", //
                    "  @Nullable @BindExtra(\"key\") String extra;", //
                    "}", //
                    "@Retention(CLASS) @Target(FIELD) @interface Nullable {}"));

    JavaFileObject expectedSource =
        JavaFileObjects.forSourceString(
            "test/Test__ExtraBinder",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import dart.Dart;", //
                    "import java.lang.Object;", //
                    "import java.lang.String;", //
                    "public class Test__ExtraBinder {", //
                    "  public static void bind(Dart.Finder finder, Test target, Object source) {", //
                    "    Object object;", //
                    "    object = finder.getExtra(source, \"key\");", //
                    "    if (object != null) {", //
                    "      target.extra = (String) object;", //
                    "    }", //
                    "  }", //
                    "}" //
                    ));

    Compilation compilation =
        javac()
            .withProcessors(ProcessorTestUtilities.dartProcessorsWithoutParceler())
            .compile(source);
    assertThat(compilation)
        .generatedSourceFile("test/Test__ExtraBinder")
        .hasSourceEquivalentTo(expectedSource);
  }

  @Test
  public void failsIfInPrivateClass() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.Test",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import android.app.Activity;", //
                    "import dart.BindExtra;", //
                    "public class Test {", //
                    "  private static class Inner {", //
                    "    @BindExtra(\"key\") String extra;", //
                    "  }", //
                    "}" //
                    ));

    Compilation compilation =
        javac()
            .withProcessors(ProcessorTestUtilities.dartProcessorsWithoutParceler())
            .compile(source);
    assertThat(compilation)
        .hadErrorContaining(
            "@BindExtra fields may not be contained in private classes. (test.Test.Inner.extra)")
        .inFile(source)
        .onLine(5);
  }

  @Test
  public void failsIfPrivate() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.Test",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import android.app.Activity;", //
                    "import dart.BindExtra;", //
                    "public class Test extends Activity {", //
                    "    @BindExtra(\"key\") private String extra;", //
                    "}" //
                    ));

    Compilation compilation =
        javac()
            .withProcessors(ProcessorTestUtilities.dartProcessorsWithoutParceler())
            .compile(source);
    assertThat(compilation)
        .hadErrorContaining("@BindExtra fields must not be private or static. (test.Test.extra)")
        .inFile(source)
        .onLine(5);
  }

  @Test
  public void failsIfStatic() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.Test",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import android.app.Activity;", //
                    "import dart.BindExtra;", //
                    "public class Test extends Activity {", //
                    "    @BindExtra(\"key\") static String extra;", //
                    "}" //
                    ));

    Compilation compilation =
        javac()
            .withProcessors(ProcessorTestUtilities.dartProcessorsWithoutParceler())
            .compile(source);
    assertThat(compilation)
        .hadErrorContaining("@BindExtra fields must not be private or static. (test.Test.extra)")
        .inFile(source)
        .onLine(5);
  }

  @Test
  public void failsIfInInterface() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.Test",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import android.app.Activity;", //
                    "import dart.BindExtra;", //
                    "public interface Test {", //
                    "    @BindExtra(\"key\") String extra = null;", //
                    "}" //
                    ));

    Compilation compilation =
        javac()
            .withProcessors(ProcessorTestUtilities.dartProcessorsWithoutParceler())
            .compile(source);
    assertThat(compilation)
        .hadErrorContaining("@BindExtra fields may only be contained in classes. (test.Test.extra)")
        .inFile(source)
        .onLine(4);
  }

  @Test
  public void superclass() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.Test",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import android.app.Activity;", //
                    "import dart.BindExtra;", //
                    "public class Test extends Activity {", //
                    "    @BindExtra(\"key\") String extra;", //
                    "}", //
                    "class TestOne extends Test {", //
                    "    @BindExtra(\"key\") String extra1;", //
                    "}", //
                    "class TestTwo extends Test {", //
                    "}" //
                    ));

    JavaFileObject expectedSource1 =
        JavaFileObjects.forSourceString(
            "test/Test__ExtraBinder",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import dart.Dart;", //
                    "import java.lang.Object;", //
                    "import java.lang.String;", //
                    "public class Test__ExtraBinder {", //
                    "  public static void bind(Dart.Finder finder, Test target, Object source) {", //
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
        JavaFileObjects.forSourceString(
            "test/TestOne__ExtraBinder",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import dart.Dart;", //
                    "import java.lang.Object;", //
                    "import java.lang.String;", //
                    "public class TestOne__ExtraBinder {", //
                    "  public static void bind(Dart.Finder finder, TestOne target, Object source) {", //
                    "    Test__ExtraBinder.bind(finder, target, source);", //
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

    Compilation compilation =
        javac()
            .withProcessors(ProcessorTestUtilities.dartProcessorsWithoutParceler())
            .compile(source);
    assertThat(compilation)
        .generatedSourceFile("test/Test__ExtraBinder")
        .hasSourceEquivalentTo(expectedSource1);
    assertThat(compilation)
        .generatedSourceFile("test/TestOne__ExtraBinder")
        .hasSourceEquivalentTo(expectedSource2);
  }

  @Test
  public void genericSuperclass() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.Test",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import android.app.Activity;", //
                    "import dart.BindExtra;", //
                    "public class Test<T> extends Activity {", //
                    "    @BindExtra(\"key\") String extra;", //
                    "}", //
                    "class TestOne extends Test<String> {", //
                    "    @BindExtra(\"key\") String extra1;", //
                    "}", //
                    "class TestTwo extends Test<Object> {", //
                    "}" //
                    ));

    JavaFileObject expectedSource1 =
        JavaFileObjects.forSourceString(
            "test/Test__ExtraBinder",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import dart.Dart;", //
                    "import java.lang.Object;", //
                    "import java.lang.String;", //
                    "public class Test__ExtraBinder {", //
                    "  public static void bind(Dart.Finder finder, Test target, Object source) {", //
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
        JavaFileObjects.forSourceString(
            "test/TestOne__ExtraBinder",
            Joiner.on('\n')
                .join( //
                    "package test;", //
                    "import dart.Dart;", //
                    "import java.lang.Object;", //
                    "import java.lang.String;", //
                    "public class TestOne__ExtraBinder {", //
                    "  public static void bind(Dart.Finder finder, TestOne target, Object source) {",
                    "    Test__ExtraBinder.bind(finder, target, source);", //
                    "    Object object;", //
                    "    object = finder.getExtra(source, \"key\");", //
                    "    if (object == null) {", //
                    "      throw new IllegalStateException(\"Required extra with key 'key' for field 'extra1' was not found. If this extra is optional add '@Nullable' annotation.\");",
                    "    }", //
                    "    target.extra1 = (String) object;", //
                    "  }", //
                    "}" //
                    ));

    Compilation compilation =
        javac()
            .withProcessors(ProcessorTestUtilities.dartProcessorsWithoutParceler())
            .compile(source);
    assertThat(compilation)
        .generatedSourceFile("test/Test__ExtraBinder")
        .hasSourceEquivalentTo(expectedSource1);
    assertThat(compilation)
        .generatedSourceFile("test/TestOne__ExtraBinder")
        .hasSourceEquivalentTo(expectedSource2);
  }
}
