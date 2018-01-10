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

public class HensonGeneratorTest {

  @Test
  public void
      hensonNavigatorGenerator_should_generateHensonClass_when_navigationModelIsDefined_and_containsExtras() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;",
                    "import dart.DartModel;",
                    "@DartModel",
                    "public class TestNavigationModel {",
                    "    String extra;",
                    "}"));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString(
            "test.navigation.Henson",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;",
                    "import android.content.Context;",
                    "public class Henson {",
                    "  private Henson() {",
                    "  }",
                    "  public static WithContextSetState with(Context context) {",
                    "    return new test.navigation.Henson.WithContextSetState(context);",
                    "  }",
                    "  public static class WithContextSetState {",
                    "    private Context context;",
                    "    private WithContextSetState(Context context) {",
                    "      this.context = context;",
                    "    }",
                    "    public Test__IntentBuilder.InitialState gotoTest() {",
                    "      return test.navigation.Test__IntentBuilder.getInitialState(context);",
                    "    }",
                    "  }",
                    "}"));

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
                    "package test.navigation;",
                    "import dart.DartModel;",
                    "@DartModel",
                    "public class TestNavigationModel {",
                    "}"));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString(
            "test.navigation.Henson",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;",
                    "import android.content.Context;",
                    "public class Henson {",
                    "  private Henson() {",
                    "  }",
                    "  public static WithContextSetState with(Context context) {",
                    "    return new test.navigation.Henson.WithContextSetState(context);",
                    "  }",
                    "  public static class WithContextSetState {",
                    "    private Context context;",
                    "    private WithContextSetState(Context context) {",
                    "      this.context = context;",
                    "    }",
                    "    public Test__IntentBuilder.InitialState gotoTest() {",
                    "      return test.navigation.Test__IntentBuilder.getInitialState(context);",
                    "    }",
                    "  }",
                    "}"));

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
                    "package test.navigation;",
                    "import dart.BindExtra;",
                    "import dart.DartModel;",
                    "public class TestNavigationModel {",
                    "    @BindExtra(\"key\") String extra;",
                    "}"));

    Compilation compilation =
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation).generatedSourceFile("test.navigation.Henson");
  }

  @Test
  public void
      hensonNavigatorGenerator_should_inheritedInitialState_when_superIntentBuilderHasOptionalExtras() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.Test1NavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;",
                    "import dart.DartModel;",
                    "@DartModel",
                    "public class Test1NavigationModel extends Test3NavigationModel {",
                    "}",
                    "@DartModel",
                    "class Test2NavigationModel extends Test3NavigationModel {",
                    "    String extra2;",
                    "}",
                    "@DartModel",
                    "class Test3NavigationModel {",
                    "}"));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString(
            "test.navigation.Henson",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;",
                    "import android.content.Context;",
                    "public class Henson {",
                    "  private Henson() {",
                    "  }",
                    "  public static WithContextSetState with(Context context) {",
                    "    return new test.navigation.Henson.WithContextSetState(context);",
                    "  }",
                    "  public static class WithContextSetState {",
                    "    private Context context;",
                    "    private WithContextSetState(Context context) {",
                    "      this.context = context;",
                    "    }",
                    "    public Test1__IntentBuilder.InitialState gotoTest1() {",
                    "      return test.navigation.Test1__IntentBuilder.getInitialState(context);",
                    "    }",
                    "    public Test2__IntentBuilder.InitialState gotoTest2() {",
                    "      return test.navigation.Test2__IntentBuilder.getInitialState(context);",
                    "    }",
                    "    public Test3__IntentBuilder.InitialState gotoTest3() {",
                    "      return test.navigation.Test3__IntentBuilder.getInitialState(context);",
                    "    }",
                    "  }",
                    "}"));

    Compilation compilation =
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Henson")
        .hasSourceEquivalentTo(builderSource);
  }

  @Test
  public void
      hensonNavigatorGenerator_should_inheritedInitialState_when_superIntentBuilderHasRequiredExtras() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.Test1NavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;",
                    "import dart.DartModel;",
                    "@DartModel",
                    "public class Test1NavigationModel extends Test3NavigationModel<String> {",
                    "}",
                    "@DartModel",
                    "class Test2NavigationModel extends Test3NavigationModel<Object> {",
                    "    String extra2;",
                    "}",
                    "@DartModel",
                    "class Test3NavigationModel<T> {",
                    "    String extra3;",
                    "}"));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString(
            "test.navigation.Henson",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;",
                    "import android.content.Context;",
                    "public class Henson {",
                    "  private Henson() {",
                    "  }",
                    "  public static WithContextSetState with(Context context) {",
                    "    return new test.navigation.Henson.WithContextSetState(context);",
                    "  }",
                    "  public static class WithContextSetState {",
                    "    private Context context;",
                    "    private WithContextSetState(Context context) {",
                    "      this.context = context;",
                    "    }",
                    "    public Test3__IntentBuilder.InitialState gotoTest1() {",
                    "      return test.navigation.Test1__IntentBuilder.getInitialState(context);",
                    "    }",
                    "    public Test2__IntentBuilder.InitialState gotoTest2() {",
                    "      return test.navigation.Test2__IntentBuilder.getInitialState(context);",
                    "    }",
                    "    public Test3__IntentBuilder.InitialState gotoTest3() {",
                    "      return test.navigation.Test3__IntentBuilder.getInitialState(context);",
                    "    }",
                    "  }",
                    "}"));

    Compilation compilation =
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Henson")
        .hasSourceEquivalentTo(builderSource);
  }
}
