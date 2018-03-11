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
import static dart.processor.ProcessorTestUtilities.navigationModelBinderProcessors;

import com.google.common.base.Joiner;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

/** Tests {@link ExtraBinderProcessor}. For tests not related to Parceler. */
public class BindNavigationModelTest {

  @Test
  public void bindingNavigationModel() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestActivity",
            Joiner.on('\n')
                .join(
                    "package test;",
                    "import dart.DartModel;",
                    "import dart.Dart;",
                    "import java.lang.Object;",
                    "public class TestActivity {",
                    "  @DartModel TestActivityNavigationModel navigationModel;",
                    "}",
                    "class TestActivityNavigationModel {",
                    "}",
                    "class TestActivityNavigationModel__ExtraBinder {",
                    "  public static void bind(Dart.Finder finder, TestActivityNavigationModel navigationModel, Object source) {",
                    "  }",
                    "}"));

    JavaFileObject binderSource =
        JavaFileObjects.forSourceString(
            "test/TestActivity__NavigationModelBinder",
            Joiner.on('\n')
                .join(
                    "package test;",
                    "import dart.Dart;",
                    "public class TestActivity__NavigationModelBinder {",
                    "  public static void bind(Dart.Finder finder, TestActivity target) {",
                    "    TestActivityNavigationModel__ExtraBinder.bind(finder, target.navigationModel, target);",
                    "  }",
                    "  public static void assign(TestActivity target, TestActivityNavigationModel navigationModel) {",
                    "    target.navigationModel = navigationModel;",
                    "  }",
                    "}"));

    Compilation compilation =
        javac().withProcessors(navigationModelBinderProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test/TestActivity__NavigationModelBinder")
        .hasSourceEquivalentTo(binderSource);
  }

  @Test
  public void bindingMoreThanOneNavigationModel() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestActivity",
            Joiner.on('\n')
                .join(
                    "package test;",
                    "import dart.DartModel;",
                    "import dart.Dart;",
                    "import java.lang.Object;",
                    "public class TestActivity {",
                    "  @DartModel TestActivityNavigationModel navigationModel1;",
                    "  @DartModel TestActivityNavigationModel navigationModel2;",
                    "}",
                    "class TestActivityNavigationModel {",
                    "}",
                    "class TestActivityNavigationModel__ExtraBinder {",
                    "  public static void bind(Dart.Finder finder, TestActivityNavigationModel navigationModel, Object source) {",
                    "  }",
                    "}"));

    Compilation compilation =
        javac().withProcessors(navigationModelBinderProcessors()).compile(source);
    assertThat(compilation)
        .hadErrorContaining("Component TestActivity cannot bind more than one NavigationModel.")
        .inFile(source);
  }

  @Test
  public void bindingNavigationModelInPrivateField() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestActivity",
            Joiner.on('\n')
                .join(
                    "package test;",
                    "import dart.DartModel;",
                    "import dart.Dart;",
                    "import java.lang.Object;",
                    "public class TestActivity {",
                    "  @DartModel private TestActivityNavigationModel navigationModel;",
                    "}",
                    "class TestActivityNavigationModel {",
                    "}",
                    "class TestActivityNavigationModel__ExtraBinder {",
                    "  public static void bind(Dart.Finder finder, TestActivityNavigationModel navigationModel, Object source) {",
                    "  }",
                    "}"));

    Compilation compilation =
        javac().withProcessors(navigationModelBinderProcessors()).compile(source);
    assertThat(compilation)
        .hadErrorContaining(
            "@DartModel field must not be private or static. (test.TestActivity.navigationModel)")
        .inFile(source);
  }

  @Test
  public void bindingNavigationModelInStaticField() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestActivity",
            Joiner.on('\n')
                .join(
                    "package test;",
                    "import dart.DartModel;",
                    "import dart.Dart;",
                    "import java.lang.Object;",
                    "public class TestActivity {",
                    "  @DartModel static TestActivityNavigationModel navigationModel;",
                    "}",
                    "class TestActivityNavigationModel {",
                    "}",
                    "class TestActivityNavigationModel__ExtraBinder {",
                    "  public static void bind(Dart.Finder finder, TestActivityNavigationModel navigationModel, Object source) {",
                    "  }",
                    "}"));

    Compilation compilation =
        javac().withProcessors(navigationModelBinderProcessors()).compile(source);
    assertThat(compilation)
        .hadErrorContaining(
            "@DartModel field must not be private or static. (test.TestActivity.navigationModel)")
        .inFile(source);
  }

  @Test
  public void bindingNavigationModelForChildAndParentClass() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestActivity",
            Joiner.on('\n')
                .join(
                    "package test;",
                    "import dart.DartModel;",
                    "import dart.Dart;",
                    "import java.lang.Object;",
                    "public class TestActivity extends TestSuperActivity {",
                    "  @DartModel TestActivityNavigationModel navigationModel;",
                    "}",
                    "class TestSuperActivity {",
                    "  @DartModel TestSuperActivityNavigationModel superNavigationModel;",
                    "}",
                    "class TestActivityNavigationModel extends TestSuperActivityNavigationModel {",
                    "}",
                    "class TestSuperActivityNavigationModel {",
                    "}",
                    "class TestActivityNavigationModel__ExtraBinder {",
                    "  public static void bind(Dart.Finder finder, TestActivityNavigationModel navigationModel, Object source) {",
                    "  }",
                    "}",
                    "class TestSuperActivityNavigationModel__ExtraBinder {",
                    "  public static void bind(Dart.Finder finder, TestSuperActivityNavigationModel navigationModel, Object source) {",
                    "  }",
                    "}"));

    JavaFileObject binderSource1 =
        JavaFileObjects.forSourceString(
            "test/TestActivity__NavigationModelBinder",
            Joiner.on('\n')
                .join(
                    "package test;",
                    "import dart.Dart;",
                    "public class TestActivity__NavigationModelBinder {",
                    "  public static void bind(Dart.Finder finder, TestActivity target) {",
                    "    TestActivityNavigationModel__ExtraBinder.bind(finder, target.navigationModel, target);",
                    "    TestSuperActivity__NavigationModelBinder.assign(target, target.navigationModel);",
                    "  }",
                    "  public static void assign(TestActivity target, TestActivityNavigationModel navigationModel) {",
                    "    target.navigationModel = navigationModel;",
                    "    TestSuperActivity__NavigationModelBinder.assign(target, navigationModel);",
                    "  }",
                    "}"));

    JavaFileObject binderSource2 =
        JavaFileObjects.forSourceString(
            "test/TestActivity__NavigationModelBinder",
            Joiner.on('\n')
                .join(
                    "package test;",
                    "import dart.Dart;",
                    "public class TestSuperActivity__NavigationModelBinder {",
                    "  public static void bind(Dart.Finder finder, TestSuperActivity target) {",
                    "    TestSuperActivityNavigationModel__ExtraBinder.bind(finder, target.superNavigationModel, target);",
                    "  }",
                    "  public static void assign(TestSuperActivity target, TestSuperActivityNavigationModel navigationModel) {",
                    "    target.superNavigationModel = navigationModel;",
                    "  }",
                    "}"));

    Compilation compilation =
        javac().withProcessors(navigationModelBinderProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test/TestActivity__NavigationModelBinder")
        .hasSourceEquivalentTo(binderSource1);
    assertThat(compilation)
        .generatedSourceFile("test/TestSuperActivity__NavigationModelBinder")
        .hasSourceEquivalentTo(binderSource2);
  }

  @Test
  public void bindingNavigationModelForChildAndGrandParentClass() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestActivity",
            Joiner.on('\n')
                .join(
                    "package test;",
                    "import dart.DartModel;",
                    "import dart.Dart;",
                    "import java.lang.Object;",
                    "public class TestActivity extends TestIntermediateActivity {",
                    "  @DartModel TestActivityNavigationModel navigationModel;",
                    "}",
                    "class TestIntermediateActivity extends TestSuperActivity {",
                    "}",
                    "class TestSuperActivity {",
                    "  @DartModel TestSuperActivityNavigationModel superNavigationModel;",
                    "}",
                    "class TestActivityNavigationModel extends TestSuperActivityNavigationModel {",
                    "}",
                    "class TestSuperActivityNavigationModel {",
                    "}",
                    "class TestActivityNavigationModel__ExtraBinder {",
                    "  public static void bind(Dart.Finder finder, TestActivityNavigationModel navigationModel, Object source) {",
                    "  }",
                    "}",
                    "class TestSuperActivityNavigationModel__ExtraBinder {",
                    "  public static void bind(Dart.Finder finder, TestSuperActivityNavigationModel navigationModel, Object source) {",
                    "  }",
                    "}"));

    JavaFileObject binderSource1 =
        JavaFileObjects.forSourceString(
            "test/TestActivity__NavigationModelBinder",
            Joiner.on('\n')
                .join(
                    "package test;",
                    "import dart.Dart;",
                    "public class TestActivity__NavigationModelBinder {",
                    "  public static void bind(Dart.Finder finder, TestActivity target) {",
                    "    TestActivityNavigationModel__ExtraBinder.bind(finder, target.navigationModel, target);",
                    "    TestSuperActivity__NavigationModelBinder.assign(target, target.navigationModel);",
                    "  }",
                    "  public static void assign(TestActivity target, TestActivityNavigationModel navigationModel) {",
                    "    target.navigationModel = navigationModel;",
                    "    TestSuperActivity__NavigationModelBinder.assign(target, navigationModel);",
                    "  }",
                    "}"));

    JavaFileObject binderSource2 =
        JavaFileObjects.forSourceString(
            "test/TestActivity__NavigationModelBinder",
            Joiner.on('\n')
                .join(
                    "package test;",
                    "import dart.Dart;",
                    "public class TestSuperActivity__NavigationModelBinder {",
                    "  public static void bind(Dart.Finder finder, TestSuperActivity target) {",
                    "    TestSuperActivityNavigationModel__ExtraBinder.bind(finder, target.superNavigationModel, target);",
                    "  }",
                    "  public static void assign(TestSuperActivity target, TestSuperActivityNavigationModel navigationModel) {",
                    "    target.superNavigationModel = navigationModel;",
                    "  }",
                    "}"));

    Compilation compilation =
        javac().withProcessors(navigationModelBinderProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test/TestActivity__NavigationModelBinder")
        .hasSourceEquivalentTo(binderSource1);
    assertThat(compilation)
        .generatedSourceFile("test/TestSuperActivity__NavigationModelBinder")
        .hasSourceEquivalentTo(binderSource2);
  }

  @Test
  public void bindingNavigationModelForChildWithParentOutside() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.TestActivity",
            Joiner.on('\n')
                .join(
                    "package test;",
                    "import dart.DartModel;",
                    "import dart.Dart;",
                    "import java.lang.Object;",
                    "import dart.processor.data.ActivityWithNavigationModelField;",
                    "import dart.processor.data.ActivityWithNavigationModelFieldNavigationModel;",
                    "public class TestActivity extends ActivityWithNavigationModelField {",
                    "  @DartModel TestActivityNavigationModel navigationModel;",
                    "}",
                    "class TestActivityNavigationModel extends ActivityWithNavigationModelFieldNavigationModel {",
                    "}",
                    "class TestActivityNavigationModel__ExtraBinder {",
                    "  public static void bind(Dart.Finder finder, TestActivityNavigationModel navigationModel, Object source) {",
                    "  }",
                    "}"));

    JavaFileObject binderSource1 =
        JavaFileObjects.forSourceString(
            "test/TestActivity__NavigationModelBinder",
            Joiner.on('\n')
                .join(
                    "package test;",
                    "import dart.Dart;",
                    "import dart.processor.data.ActivityWithNavigationModelField__NavigationModelBinder;",
                    "public class TestActivity__NavigationModelBinder {",
                    "  public static void bind(Dart.Finder finder, TestActivity target) {",
                    "    TestActivityNavigationModel__ExtraBinder.bind(finder, target.navigationModel, target);",
                    "    ActivityWithNavigationModelField__NavigationModelBinder.assign(target, target.navigationModel);",
                    "  }",
                    "  public static void assign(TestActivity target, TestActivityNavigationModel navigationModel) {",
                    "    target.navigationModel = navigationModel;",
                    "    ActivityWithNavigationModelField__NavigationModelBinder.assign(target, navigationModel);",
                    "  }",
                    "}"));

    Compilation compilation =
        javac().withProcessors(navigationModelBinderProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test/TestActivity__NavigationModelBinder")
        .hasSourceEquivalentTo(binderSource1);
  }
}
