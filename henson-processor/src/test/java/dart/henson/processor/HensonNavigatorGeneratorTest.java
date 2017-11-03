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

package dart.henson.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

import com.google.common.base.Joiner;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

public class HensonNavigatorGeneratorTest {

  @Test
  public void
      hensonNavigatorGenerator_should_generateHensonClass_when_navigationModelIsDefined_and_containsExtras() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.InjectExtra;", //
                    "import dart.NavigationModel;", //
                    "@NavigationModel(\"test.Test\")", //
                    "public class TestNavigationModel {", //
                    "    @InjectExtra(\"key\") String extra;", //
                    "}" //
                    ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString(
            "test.navigation.Henson",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import android.content.Context;", //
                    "public class Henson {", //
                    "  private Henson() {", //
                    "  }", //
                    "  public static WithContextSetState with(Context context) {", //
                    "    return new test.navigation.Henson.WithContextSetState(context);", //
                    "  }", //
                    "  public static class WithContextSetState {", //
                    "    private Context context;", //
                    "    private WithContextSetState(Context context) {", //
                    "      this.context = context;", //
                    "    }", //
                    "    public Test$$IntentBuilder gotoTest() {", //
                    "      return new test.navigation.Test$$IntentBuilder(context);", //
                    "    }", //
                    "  }", //
                    "}" //
                    ));

    Compilation compilation =
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Henson")
        .hasSourceEquivalentTo(builderSource);
  }

  @Test
  public void
      hensonNavigatorGenerator_should_generateHensonClass_when_navigationModelIsDefined_and_DoesNotContainExtras() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.InjectExtra;", //
                    "import dart.NavigationModel;", //
                    "@NavigationModel(\"test.Test\")", //
                    "public class TestNavigationModel {", //
                    "}" //
                    ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString(
            "test.navigation.Henson",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import android.content.Context;", //
                    "public class Henson {", //
                    "  private Henson() {", //
                    "  }", //
                    "  public static WithContextSetState with(Context context) {", //
                    "    return new test.navigation.Henson.WithContextSetState(context);", //
                    "  }", //
                    "  public static class WithContextSetState {", //
                    "    private Context context;", //
                    "    private WithContextSetState(Context context) {", //
                    "      this.context = context;", //
                    "    }", //
                    "    public Test$$IntentBuilder gotoTest() {", //
                    "      return new test.navigation.Test$$IntentBuilder(context);", //
                    "    }", //
                    "  }", //
                    "}" //
                    ));

    Compilation compilation =
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Henson")
        .hasSourceEquivalentTo(builderSource);
  }

  @Test
  public void
      hensonNavigatorGenerator_should_generateHensonClass_when_navigationModelIsDefined_and_targetClassIsInner() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.InjectExtra;", //
                    "import dart.NavigationModel;", //
                    "@NavigationModel(\"test.Test$MyInnerTest\")", //
                    "public class TestNavigationModel {", //
                    "}" //
                    ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString(
            "test.navigation.Henson",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import android.content.Context;", //
                    "public class Henson {", //
                    "  private Henson() {", //
                    "  }", //
                    "  public static WithContextSetState with(Context context) {", //
                    "    return new test.navigation.Henson.WithContextSetState(context);", //
                    "  }", //
                    "  public static class WithContextSetState {", //
                    "    private Context context;", //
                    "    private WithContextSetState(Context context) {", //
                    "      this.context = context;", //
                    "    }", //
                    "    public Test$MyInnerTest$$IntentBuilder gotoTest$MyInnerTest() {", //
                    "      return new test.navigation.Test$MyInnerTest$$IntentBuilder(context);", //
                    "    }", //
                    "  }", //
                    "}" //
                    ));

    Compilation compilation =
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Henson")
        .hasSourceEquivalentTo(builderSource);
  }

  @Test(expected = AssertionError.class)
  public void
      hensonNavigatorGenerator_should_notGenerateHensonClass_when_navigationModelIsNotDefined_and_containExtras() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.InjectExtra;", //
                    "import dart.NavigationModel;", //
                    "public class TestNavigationModel {", //
                    "    @InjectExtra(\"key\") String extra;", //
                    "}" //
                    ));

    Compilation compilation =
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation).generatedSourceFile("test.navigation.Henson");
  }

  @Test
  public void
      hensonNavigatorGenerator_should_generateHensonClass_when_navigationModelIsDefinedForMultipleClasses() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel1",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.InjectExtra;", //
                    "import dart.NavigationModel;", //
                    "@NavigationModel(\"test.Test1\")", //
                    "public class TestNavigationModel1 {", //
                    "}", //
                    "@NavigationModel(\"test.Test2\")", //
                    "class TestNavigationModel2 extends TestNavigationModel3 {", //
                    "    @InjectExtra(\"key2\") String extra2;", //
                    "}", //
                    "class TestNavigationModel3 {", //
                    "    @InjectExtra(\"key3\") String extra3;", //
                    "}" //
                    ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString(
            "test.navigation.Henson",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import android.content.Context;", //
                    "public class Henson {", //
                    "  private Henson() {", //
                    "  }", //
                    "  public static WithContextSetState with(Context context) {", //
                    "    return new test.navigation.Henson.WithContextSetState(context);", //
                    "  }", //
                    "  public static class WithContextSetState {", //
                    "    private Context context;", //
                    "    private WithContextSetState(Context context) {", //
                    "      this.context = context;", //
                    "    }", //
                    "    public Test1$$IntentBuilder gotoTest1() {", //
                    "      return new test.navigation.Test1$$IntentBuilder(context);", //
                    "    }", //
                    "    public Test2$$IntentBuilder gotoTest2() {", //
                    "      return new test.navigation.Test2$$IntentBuilder(context);", //
                    "    }", //
                    "  }", //
                    "}" //
                    ));

    Compilation compilation =
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Henson")
        .hasSourceEquivalentTo(builderSource);
  }

  @Test
  public void
      hensonNavigatorGenerator_should_generateHensonClass_when_navigationModelIsDefined_and_usingGenerics() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel1",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.InjectExtra;", //
                    "import dart.NavigationModel;", //
                    "@NavigationModel(\"test.Test1\")", //
                    "public class TestNavigationModel1 extends TestNavigationModel3<String> {", //
                    "}", //
                    "@NavigationModel(\"test.Test2\")", //
                    "class TestNavigationModel2 extends TestNavigationModel3<Object> {", //
                    "    @InjectExtra(\"key2\") String extra2;", //
                    "}", //
                    "class TestNavigationModel3<T> {", //
                    "    @InjectExtra(\"key3\") String extra3;", //
                    "}" //
                    ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString(
            "test.navigation.Henson",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import android.content.Context;", //
                    "public class Henson {", //
                    "  private Henson() {", //
                    "  }", //
                    "  public static WithContextSetState with(Context context) {", //
                    "    return new test.navigation.Henson.WithContextSetState(context);", //
                    "  }", //
                    "  public static class WithContextSetState {", //
                    "    private Context context;", //
                    "    private WithContextSetState(Context context) {", //
                    "      this.context = context;", //
                    "    }", //
                    "    public Test1$$IntentBuilder gotoTest1() {", //
                    "      return new test.navigation.Test1$$IntentBuilder(context);", //
                    "    }", //
                    "    public Test2$$IntentBuilder gotoTest2() {", //
                    "      return new test.navigation.Test2$$IntentBuilder(context);", //
                    "    }", //
                    "  }", //
                    "}" //
                    ));

    Compilation compilation =
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Henson")
        .hasSourceEquivalentTo(builderSource);
  }

  @Test
  public void
      hensonNavigatorGenerator_should_generateHensonClass_when_navigationModelIsDefined_and_superClassIsAbstract() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel1",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.InjectExtra;", //
                    "import dart.NavigationModel;", //
                    "@NavigationModel(\"test.Test1\")", //
                    "public class TestNavigationModel1 extends TestNavigationModel2 {", //
                    "}", //
                    "abstract class TestNavigationModel2 {", //
                    "    @InjectExtra(\"key2\") String extra2;", //
                    "}" //
                    ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString(
            "test.navigation.Henson",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import android.content.Context;", //
                    "public class Henson {", //
                    "  private Henson() {", //
                    "  }", //
                    "  public static WithContextSetState with(Context context) {", //
                    "    return new test.navigation.Henson.WithContextSetState(context);", //
                    "  }", //
                    "  public static class WithContextSetState {", //
                    "    private Context context;", //
                    "    private WithContextSetState(Context context) {", //
                    "      this.context = context;", //
                    "    }", //
                    "    public Test1$$IntentBuilder gotoTest1() {", //
                    "      return new test.navigation.Test1$$IntentBuilder(context);", //
                    "    }", //
                    "  }", //
                    "}" //
                    ));

    Compilation compilation =
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Henson")
        .hasSourceEquivalentTo(builderSource);
  }

  @Test
  public void hensonNavigatorGenerator_should_fail_when_navigationModelAnnotatedClassIsAbstract() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel1",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.InjectExtra;", //
                    "import dart.NavigationModel;", //
                    "@NavigationModel(\"test.Test1\")", //
                    "public class TestNavigationModel1 {", //
                    "}", //
                    "@NavigationModel(\"test.Test2\")", //
                    "abstract class TestNavigationModel2 {", //
                    "}" //
                    ));

    Compilation compilation =
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .hadErrorContaining(
            "@NavigationModel class TestNavigationModel2 must not be private, static or abstract.");
  }

  @Test
  public void hensonNavigatorGenerator_should_fail_when_navigationModelAnnotatedClassIsInner() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel1",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.InjectExtra;", //
                    "import dart.NavigationModel;", //
                    "@NavigationModel(\"test.Test1\")", //
                    "public class TestNavigationModel1 {", //
                    "  @NavigationModel(\"test.Test2\")", //
                    "  class TestNavigationModel2 {", //
                    "  }", //
                    "}" //
                    ));

    Compilation compilation =
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .hadErrorContaining(
            "@NavigationModel class TestNavigationModel2 must be a top level class.");
  }

  @Test
  public void
      hensonNavigatorGenerator_should_fail_when_navigationModelAnnotatedClassIsInnerStatic() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel1",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.InjectExtra;", //
                    "import dart.NavigationModel;", //
                    "@NavigationModel(\"test.Test1\")", //
                    "public class TestNavigationModel1 {", //
                    "  @NavigationModel(\"test.Test2\")", //
                    "  static class TestNavigationModel2 {", //
                    "  }", //
                    "}" //
                    ));

    Compilation compilation =
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .hadErrorContaining(
            "@NavigationModel class TestNavigationModel2 must not be private, static or abstract.");
  }

  @Test
  public void hensonNavigatorGenerator_should_fail_when_navigationModelAnnotatedClassIsPrivate() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel1",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.InjectExtra;", //
                    "import dart.NavigationModel;", //
                    "@NavigationModel(\"test.Test1\")", //
                    "public class TestNavigationModel1 {", //
                    "  @NavigationModel(\"test.Test2\")", //
                    "  private class TestNavigationModel2 {", //
                    "  }", //
                    "}" //
                    ));

    Compilation compilation =
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .hadErrorContaining(
            "@NavigationModel class TestNavigationModel2 must not be private, static or abstract.");
  }
}
