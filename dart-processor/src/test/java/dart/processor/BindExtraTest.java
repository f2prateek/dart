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

import com.google.common.base.Joiner;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static dart.processor.ProcessorTestUtilities.dartProcessorsWithoutParceler;

/** Tests {@link dart.processor.InjectExtraProcessor}. For tests not related to Parceler. */
public class BindExtraTest {

  @Test
  public void bindingExtra() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestNavigationModel",
            Joiner.on('\n')
                .join("package test;",
                    "import dart.BindExtra;",
                    "import dart.DartModel;",
                    "@DartModel",
                    "public class TestNavigationModel {",
                    "    @BindExtra(\"key\") String extra;",
                    "}"
                ));

    JavaFileObject binderSource =
        JavaFileObjects.forSourceString(
            "test/TestNavigationModel__ExtraBinder",
            Joiner.on('\n')
                .join("package test;",
                    "import dart.Dart;",
                    "import java.lang.Object;",
                    "import java.lang.String;",
                    "public class TestNavigationModel__ExtraBinder {",
                    "  public static void bind(Dart.Finder finder, TestNavigationModel target, Object source) {",
                    "    Object object;",
                    "    object = finder.getExtra(source, \"key\");",
                    "    if (object == null) {",
                    "      throw new IllegalStateException(\"Required extra with key 'key' for field 'extra' was not found. If this extra is optional add '@Nullable' annotation.\");",
                    "    }",
                    "    target.extra = (String) object;",
                    "  }",
                    "}"
                ));

    Compilation compilation =
        javac().withProcessors(dartProcessorsWithoutParceler()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test/TestNavigationModel__ExtraBinder")
        .hasSourceEquivalentTo(binderSource);
  }

  @Test
  public void bindingAllPrimitives() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestNavigationModel",
            Joiner.on('\n')
                .join("package test;",
                    "import dart.BindExtra;",
                    "import dart.DartModel;",
                    "@DartModel",
                    "public class TestNavigationModel {",
                    "    @BindExtra(\"key_bool\") boolean aBool;",
                    "    @BindExtra(\"key_byte\") byte aByte;",
                    "    @BindExtra(\"key_short\") short aShort;",
                    "    @BindExtra(\"key_int\") int anInt;",
                    "    @BindExtra(\"key_long\") long aLong;",
                    "    @BindExtra(\"key_char\") char aChar;",
                    "    @BindExtra(\"key_float\") float aFloat;",
                    "    @BindExtra(\"key_double\") double aDouble;",
                    "}"
                ));

    JavaFileObject binderSource =
        JavaFileObjects.forSourceString(
            "test/TestNavigationModel__ExtraBinder",
            Joiner.on('\n')
                .join("package test;",
                    "import dart.Dart;",
                    "import java.lang.Object;",
                    "public class TestNavigationModel__ExtraBinder {",
                    "  public static void bind(Dart.Finder finder, TestNavigationModel target, Object source) {",
                    "    Object object;",
                    "    object = finder.getExtra(source, \"key_bool\");",
                    "    if (object == null) {",
                    "      throw new IllegalStateException(\"Required extra with key 'key_bool' for field 'aBool' was not found. If this extra is optional add '@Nullable' annotation.\");",
                    "    }",
                    "    target.aBool = (boolean) object;",
                    "    object = finder.getExtra(source, \"key_byte\");",
                    "    if (object == null) {",
                    "      throw new IllegalStateException(\"Required extra with key 'key_byte' for field 'aByte' was not found. If this extra is optional add '@Nullable' annotation.\");",
                    "    }",
                    "    target.aByte = (byte) object;",
                    "    object = finder.getExtra(source, \"key_short\");",
                    "    if (object == null) {",
                    "      throw new IllegalStateException(\"Required extra with key 'key_short' for field 'aShort' was not found. If this extra is optional add '@Nullable' annotation.\");",
                    "    }",
                    "    target.aShort = (short) object;",
                    "    object = finder.getExtra(source, \"key_int\");",
                    "    if (object == null) {",
                    "      throw new IllegalStateException(\"Required extra with key 'key_int' for field 'anInt' was not found. If this extra is optional add '@Nullable' annotation.\");",
                    "    }",
                    "    target.anInt = (int) object;",
                    "    object = finder.getExtra(source, \"key_long\");",
                    "    if (object == null) {",
                    "      throw new IllegalStateException(\"Required extra with key 'key_long' for field 'aLong' was not found. If this extra is optional add '@Nullable' annotation.\");",
                    "    }",
                    "    target.aLong = (long) object;",
                    "    object = finder.getExtra(source, \"key_char\");",
                    "    if (object == null) {",
                    "      throw new IllegalStateException(\"Required extra with key 'key_char' for field 'aChar' was not found. If this extra is optional add '@Nullable' annotation.\");",
                    "    }",
                    "    target.aChar = (char) object;",
                    "    object = finder.getExtra(source, \"key_float\");",
                    "    if (object == null) {",
                    "      throw new IllegalStateException(\"Required extra with key 'key_float' for field 'aFloat' was not found. If this extra is optional add '@Nullable' annotation.\");",
                    "    }",
                    "    target.aFloat = (float) object;",
                    "    object = finder.getExtra(source, \"key_double\");",
                    "    if (object == null) {",
                    "      throw new IllegalStateException(\"Required extra with key 'key_double' for field 'aDouble' was not found. If this extra is optional add '@Nullable' annotation.\");",
                    "    }",
                    "    target.aDouble = (double) object;",
                    "  }",
                    "}"
                ));

    Compilation compilation =
        javac().withProcessors(dartProcessorsWithoutParceler()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test/TestNavigationModel__ExtraBinder")
        .hasSourceEquivalentTo(binderSource);
  }

  @Test
  public void oneFindPerKey() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestNavigationModel",
            Joiner.on('\n')
                .join("package test;",
                    "import dart.BindExtra;",
                    "import dart.DartModel;",
                    "@DartModel",
                    "public class TestNavigationModel {",
                    "    @BindExtra(\"key\") String extra1;",
                    "    @BindExtra(\"key\") String extra2;",
                    "    @BindExtra(\"key\") String extra3;",
                    "}"
                ));

    JavaFileObject expectedSource =
        JavaFileObjects.forSourceString(
            "test/TestNavigationModel__ExtraBinder",
            Joiner.on('\n')
                .join("package test;",
                    "import dart.Dart;",
                    "import java.lang.Object;",
                    "import java.lang.String;",
                    "public class TestNavigationModel__ExtraBinder {",
                    "  public static void bind(Dart.Finder finder, TestNavigationModel target, Object source) {",
                    "    Object object;",
                    "    object = finder.getExtra(source, \"key\");",
                    "    if (object == null) {",
                    "      throw new IllegalStateException(\"Required extra with key 'key' for field 'extra1', field 'extra2', and field 'extra3' was not found. If this extra is optional add '@Nullable' annotation.\");",
                    "    }",
                    "    target.extra1 = (String) object;",
                    "    target.extra2 = (String) object;",
                    "    target.extra3 = (String) object;",
                    "  }",
                    "}"
                ));

    Compilation compilation =
        javac().withProcessors(dartProcessorsWithoutParceler()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test/TestNavigationModel__ExtraBinder")
        .hasSourceEquivalentTo(expectedSource);
  }

  @Test
  public void defaultKey() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestNavigationModel",
            Joiner.on('\n')
                .join("package test;",
                    "import dart.BindExtra;",
                    "import dart.DartModel;",
                    "import java.lang.Object;",
                    "import java.lang.String;",
                    "@DartModel",
                    "public class TestNavigationModel {",
                    "    @BindExtra String key;",
                    "}"
                ));

    JavaFileObject expectedSource =
        JavaFileObjects.forSourceString(
            "test/TestNavigationModel__ExtraBinder",
            Joiner.on('\n')
                .join("package test;",
                    "import dart.Dart;",
                    "import java.lang.Object;",
                    "import java.lang.String;",
                    "public class TestNavigationModel__ExtraBinder {",
                    "  public static void bind(Dart.Finder finder, TestNavigationModel target, Object source) {",
                    "    Object object;",
                    "    object = finder.getExtra(source, \"key\");",
                    "    if (object == null) {",
                    "      throw new IllegalStateException(\"Required extra with key 'key' for field 'key' was not found. If this extra is optional add '@Nullable' annotation.\");",
                    "    }",
                    "    target.key = (String) object;",
                    "  }",
                    "}"
                ));

    Compilation compilation =
        javac().withProcessors(dartProcessorsWithoutParceler()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test/TestNavigationModel__ExtraBinder")
        .hasSourceEquivalentTo(expectedSource);
  }

  @Test
  public void fieldVisibility() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestNavigationModel",
            Joiner.on('\n')
                .join("package test;",
                    "import dart.BindExtra;",
                    "import dart.DartModel;",
                    "import java.lang.Object;",
                    "import java.lang.String;",
                    "@DartModel",
                    "public class TestNavigationModel {",
                    "    @BindExtra(\"key_1\") String extra1;",
                    "    @BindExtra(\"key_2\") String extra2;",
                    "    @BindExtra(\"key_3\") String extra3;",
                    "}"
                ));

    Compilation compilation =
        javac().withProcessors(dartProcessorsWithoutParceler()).compile(source);
    assertThat(compilation).succeededWithoutWarnings();
  }

  @Test
  public void nullable() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestNavigationModel",
            Joiner.on('\n')
                .join("package test;",
                    "import dart.BindExtra;",
                    "import dart.DartModel;",
                    "import java.lang.Object;",
                    "import java.lang.String;",
                    "import java.lang.annotation.Retention;",
                    "import java.lang.annotation.Target;",
                    "import static java.lang.annotation.ElementType.FIELD;",
                    "import static java.lang.annotation.RetentionPolicy.CLASS;",
                    "@DartModel",
                    "public class TestNavigationModel {",
                    "  @Nullable @BindExtra(\"key\") String extra;",
                    "}",
                    "@Retention(CLASS) @Target(FIELD) @interface Nullable {}"));

    JavaFileObject expectedSource =
        JavaFileObjects.forSourceString(
            "test/TestNavigationModel__ExtraBinder",
            Joiner.on('\n')
                .join("package test;",
                    "import dart.Dart;",
                    "import java.lang.Object;",
                    "import java.lang.String;",
                    "public class TestNavigationModel__ExtraBinder {",
                    "  public static void bind(Dart.Finder finder, TestNavigationModel target, Object source) {",
                    "    Object object;",
                    "    object = finder.getExtra(source, \"key\");",
                    "    if (object != null) {",
                    "      target.extra = (String) object;",
                    "    }",
                    "  }",
                    "}"
                ));

    Compilation compilation =
        javac()
            .withProcessors(ProcessorTestUtilities.dartProcessorsWithoutParceler())
            .compile(source);
    assertThat(compilation)
        .generatedSourceFile("test/TestNavigationModel__ExtraBinder")
        .hasSourceEquivalentTo(expectedSource);
  }

  @Test
  public void failsIfInPrivateClass() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestNavigationModel",
            Joiner.on('\n')
                .join("package test;",
                    "import dart.BindExtra;",
                    "import dart.DartModel;",
                    "public class TestNavigationModel {",
                    "@DartModel",
                    "  private static class Inner {",
                    "    @BindExtra(\"key\") String extra;",
                    "  }",
                    "}"
                ));

    Compilation compilation =
        javac()
            .withProcessors(ProcessorTestUtilities.dartProcessorsWithoutParceler())
            .compile(source);
    assertThat(compilation)
        .hadErrorContaining(
            "DartModel class Inner must not be private, static or abstract.")
        .inFile(source)
        .onLine(6);
  }

  @Test
  public void failsIfPrivate() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestNavigationModel",
            Joiner.on('\n')
                .join("package test;",
                    "import dart.BindExtra;",
                    "import dart.DartModel;",
                    "@DartModel",
                    "public class TestNavigationModel {",
                    "    @BindExtra(\"key\") private String extra;",
                    "}"
                ));

    Compilation compilation =
        javac()
            .withProcessors(ProcessorTestUtilities.dartProcessorsWithoutParceler())
            .compile(source);
    assertThat(compilation)
        .hadErrorContaining(
            "@DartModel field must not be private or static. (test.TestNavigationModel.extra)")
        .inFile(source)
        .onLine(6);
  }

  @Test
  public void failsIfStatic() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestNavigationModel",
            Joiner.on('\n')
                .join("package test;",
                    "import dart.BindExtra;",
                    "import dart.DartModel;",
                    "@DartModel",
                    "public class TestNavigationModel {",
                    "    @BindExtra(\"key\") static String extra;",
                    "}"
                ));

    Compilation compilation =
        javac()
            .withProcessors(ProcessorTestUtilities.dartProcessorsWithoutParceler())
            .compile(source);
    assertThat(compilation)
        .hadErrorContaining(
            "@DartModel field must not be private or static. (test.TestNavigationModel.extra)")
        .inFile(source)
        .onLine(6);
  }

  @Test
  public void failsIfInInterface() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestNavigationModel",
            Joiner.on('\n')
                .join("package test;",
                    "import dart.BindExtra;",
                    "import dart.DartModel;",
                    "@DartModel",
                    "public interface TestNavigationModel {",
                    "    @BindExtra(\"key\") String extra = null;",
                    "}"
                ));

    Compilation compilation =
        javac()
            .withProcessors(ProcessorTestUtilities.dartProcessorsWithoutParceler())
            .compile(source);
    assertThat(compilation)
        .hadErrorContaining(
            "@DartModel class TestNavigationModel must not be private, static or abstract.")
        .inFile(source)
        .onLine(5);
  }

  @Test
  public void superclass() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestNavigationModel",
            Joiner.on('\n')
                .join("package test;",
                    "import dart.BindExtra;",
                    "import dart.DartModel;",
                    "@DartModel",
                    "public class TestNavigationModel {",
                    "    @BindExtra(\"key\") String extra;",
                    "}",
                    "@DartModel",
                    "class TestOneNavigationModel extends TestNavigationModel {",
                    "    @BindExtra(\"key\") String extra1;",
                    "}",
                    "@DartModel",
                    "class TestTwoNavigationModel extends TestNavigationModel {",
                    "}"
                ));

    JavaFileObject expectedSource1 =
        JavaFileObjects.forSourceString(
            "test/TestNavigationModel__ExtraBinder",
            Joiner.on('\n')
                .join("package test;",
                    "import dart.Dart;",
                    "import java.lang.Object;",
                    "import java.lang.String;",
                    "public class TestNavigationModel__ExtraBinder {",
                    "  public static void bind(Dart.Finder finder, TestNavigationModel target, Object source) {",
                    "    Object object;",
                    "    object = finder.getExtra(source, \"key\");",
                    "    if (object == null) {",
                    "      throw new IllegalStateException(\"Required extra with key 'key' for field 'extra' was not found. If this extra is optional add '@Nullable' annotation.\");",
                    "    }",
                    "    target.extra = (String) object;",
                    "  }",
                    "}"
                ));

    JavaFileObject expectedSource2 =
        JavaFileObjects.forSourceString(
            "test/TestOneNavigationModel__ExtraBinder",
            Joiner.on('\n')
                .join("package test;",
                    "import dart.Dart;",
                    "import java.lang.Object;",
                    "import java.lang.String;",
                    "public class TestOneNavigationModel__ExtraBinder {",
                    "  public static void bind(Dart.Finder finder, TestOneNavigationModel target, Object source) {",
                    "    TestNavigationModel__ExtraBinder.bind(finder, target, source);",
                    "    Object object;",
                    "    object = finder.getExtra(source, \"key\");",
                    "    if (object == null) {",
                    "      throw new IllegalStateException(\"Required extra with key 'key' for field 'extra1' was not found. If this extra is optional add '@Nullable' annotation.\");",
                    "    }",
                    "    target.extra1 = (String) object;",
                    "  }",
                    "}"
                ));

    Compilation compilation =
        javac()
            .withProcessors(ProcessorTestUtilities.dartProcessorsWithoutParceler())
            .compile(source);
    assertThat(compilation)
        .generatedSourceFile("test/TestNavigationModel__ExtraBinder")
        .hasSourceEquivalentTo(expectedSource1);
    assertThat(compilation)
        .generatedSourceFile("test/TestOneNavigationModel__ExtraBinder")
        .hasSourceEquivalentTo(expectedSource2);
  }

  @Test
  public void genericSuperclass() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestNavigationModel",
            Joiner.on('\n')
                .join("package test;",
                    "import dart.BindExtra;",
                    "import dart.DartModel;",
                    "@DartModel",
                    "public class TestNavigationModel<T> {",
                    "    @BindExtra(\"key\") String extra;",
                    "}",
                    "@DartModel",
                    "class TestOneNavigationModel extends TestNavigationModel<String> {",
                    "    @BindExtra(\"key\") String extra1;",
                    "}",
                    "@DartModel",
                    "class TestTwoNavigationModel extends TestNavigationModel<Object> {",
                    "}"
                ));

    JavaFileObject expectedSource1 =
        JavaFileObjects.forSourceString(
            "test/TestNavigationModel__ExtraBinder",
            Joiner.on('\n')
                .join("package test;",
                    "import dart.Dart;",
                    "import java.lang.Object;",
                    "import java.lang.String;",
                    "public class TestNavigationModel__ExtraBinder {",
                    "  public static void bind(Dart.Finder finder, TestNavigationModel target, Object source) {",
                    "    Object object;",
                    "    object = finder.getExtra(source, \"key\");",
                    "    if (object == null) {",
                    "      throw new IllegalStateException(\"Required extra with key 'key' for field 'extra' was not found. If this extra is optional add '@Nullable' annotation.\");",
                    "    }",
                    "    target.extra = (String) object;",
                    "  }",
                    "}"
                ));

    JavaFileObject expectedSource2 =
        JavaFileObjects.forSourceString(
            "test/TestOneNavigationModel__ExtraBinder",
            Joiner.on('\n')
                .join("package test;",
                    "import dart.Dart;",
                    "import java.lang.Object;",
                    "import java.lang.String;",
                    "public class TestOneNavigationModel__ExtraBinder {",
                    "  public static void bind(Dart.Finder finder, TestOneNavigationModel target, Object source) {",
                    "    TestNavigationModel__ExtraBinder.bind(finder, target, source);",
                    "    Object object;",
                    "    object = finder.getExtra(source, \"key\");",
                    "    if (object == null) {",
                    "      throw new IllegalStateException(\"Required extra with key 'key' for field 'extra1' was not found. If this extra is optional add '@Nullable' annotation.\");",
                    "    }",
                    "    target.extra1 = (String) object;",
                    "  }",
                    "}"
                ));

    Compilation compilation =
        javac()
            .withProcessors(ProcessorTestUtilities.dartProcessorsWithoutParceler())
            .compile(source);
    assertThat(compilation)
        .generatedSourceFile("test/TestNavigationModel__ExtraBinder")
        .hasSourceEquivalentTo(expectedSource1);
    assertThat(compilation)
        .generatedSourceFile("test/TestOneNavigationModel__ExtraBinder")
        .hasSourceEquivalentTo(expectedSource2);
  }
}
