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
import static dart.henson.processor.ProcessorTestUtilities.getMostEnclosingElement;
import static dart.henson.processor.ProcessorTestUtilities.hensonProcessorWithoutParceler;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Joiner;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import org.junit.Test;

/** For tests related to Parceler, but Parceler is not available. */
public class IntentBuilderGeneratorWithoutParcelerTest {

  @Test
  public void intentBuilderGenerator_should_generateCode_when_extraIsSerializableCollection() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;",
                    "import java.util.ArrayList;",
                    "import dart.BindExtra;",
                    "public class TestNavigationModel {",
                    "    @BindExtra ArrayList<String> extra;",
                    "}"));

    String intentBuilderQualifiedName = "test.navigation.Test__IntentBuilder";
    JavaFileObject builderSource =
        JavaFileObjects.forSourceString(
            intentBuilderQualifiedName,
            Joiner.on('\n')
                .join( //
                    "package test.navigation;",
                    "import static dart.henson.ActivityClassFinder.getClassDynamically;",
                    "import android.content.Context;",
                    "import android.content.Intent;",
                    "import dart.henson.AllRequiredSetState;",
                    "import dart.henson.Bundler;",
                    "import dart.henson.RequiredStateSequence;",
                    "import java.lang.String;",
                    "import java.util.ArrayList;",
                    "public class Test__IntentBuilder {",
                    "  public static InitialState getInitialState(Context context) {",
                    "    final Intent intent = new Intent(context, getClassDynamically(\"test.navigation.Test\"));",
                    "    final Bundler bundler = Bundler.create();",
                    "    return new InitialState(bundler, intent);",
                    "  }",
                    "  public static <ALL_SET extends AllSet> RequiredSequence<ALL_SET> getNextState(Bundler bundler,",
                    "          ALL_SET allSetState) {",
                    "    return new RequiredSequence<>(bundler, allSetState);",
                    "  }",
                    "  public static class RequiredSequence<ALL_SET extends AllSet> extends RequiredStateSequence<ALL_SET> {",
                    "    public RequiredSequence(Bundler bundler, ALL_SET allRequiredSetState) {",
                    "      super(bundler, allRequiredSetState);",
                    "    }",
                    "    public ALL_SET extra(ArrayList<String> extra) {",
                    "      bundler.put(\"extra\", extra);",
                    "      return allRequiredSetState;",
                    "    }",
                    "  }",
                    "  public static class AllSet<SELF extends AllSet<SELF>> extends AllRequiredSetState {",
                    "    public AllSet(Bundler bundler, Intent intent) {",
                    "      super(bundler, intent);",
                    "    }",
                    "  }",
                    "  public static class ResolvedAllSet extends AllSet<ResolvedAllSet> {",
                    "    public ResolvedAllSet(Bundler bundler, Intent intent) {",
                    "      super(bundler, intent);",
                    "    }",
                    "  }",
                    "  public static class InitialState extends RequiredSequence<ResolvedAllSet> {",
                    "    public InitialState(Bundler bundler, Intent intent) {",
                    "      super(bundler, new ResolvedAllSet(bundler, intent));",
                    "    }",
                    "  }",
                    "}"));

    IntentBuilderProcessor processor = hensonProcessorWithoutParceler();
    Compilation compilation = javac().withProcessors(processor).compile(source);
    assertThat(compilation)
        .generatedSourceFile(intentBuilderQualifiedName)
        .hasSourceEquivalentTo(builderSource);

    TypeElement originatingElement = processor.getOriginatingElement(intentBuilderQualifiedName);
    TypeElement mostEnclosingElement = getMostEnclosingElement(originatingElement);
    assertTrue(
        mostEnclosingElement
            .getQualifiedName()
            .contentEquals("test.navigation.TestNavigationModel"));
  }

  @Test
  public void intentBuilderGenerator_should_fail_when_extraIsNonSerializableCollection() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;",
                    "import java.util.List;",
                    "import dart.BindExtra;",
                    "public class TestNavigationModel {",
                    "    @BindExtra List<String> extra;",
                    "}"));

    Compilation compilation =
        javac().withProcessors(hensonProcessorWithoutParceler()).compile(source);
    assertThat(compilation)
        .hadErrorContaining(
            "The fields of class annotated with @DartModel must be primitive, Serializable or Parcelable (test.navigation.TestNavigationModel.extra).");
  }

  @Test
  public void
      intentBuilderGenerator_should_fail_when_extraIsAnnotatedWithParceler_and_parcelerIsOff() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;",
                    "import dart.BindExtra;",
                    "import org.parceler.Parcel;",
                    "public class TestNavigationModel {",
                    "    @BindExtra ClassWithRequiredAndOptionalExtrasNavigationModel extra;",
                    "    @Parcel static class ClassWithRequiredAndOptionalExtrasNavigationModel {}",
                    "}"));

    Compilation compilation =
        javac().withProcessors(hensonProcessorWithoutParceler()).compile(source);
    assertThat(compilation)
        .hadErrorContaining(
            "The fields of class annotated with @DartModel must be primitive, Serializable or Parcelable (test.navigation.TestNavigationModel.extra).");
  }

  @Test
  public void
      intentBuilderGenerator_should_fail_when_extraIsCollectionOfElementAnnotatedWithParceler_and_parcelerIsOff() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;",
                    "import java.util.List;",
                    "import dart.BindExtra;",
                    "public class TestNavigationModel {",
                    "    @BindExtra List<ClassWithRequiredAndOptionalExtrasNavigationModel> extra;",
                    "    @Parcel static class ClassWithRequiredAndOptionalExtrasNavigationModel {}",
                    "}"));

    Compilation compilation =
        javac().withProcessors(hensonProcessorWithoutParceler()).compile(source);
    assertThat(compilation)
        .hadErrorContaining(
            "The fields of class annotated with @DartModel must be primitive, Serializable or Parcelable (test.navigation.TestNavigationModel.extra).");
  }

  @Test
  public void intentBuilderGenerator_should_generateCode_when_extraIsParcelable() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;",
                    "import java.util.List;",
                    "import android.os.Parcelable;",
                    "import dart.BindExtra;",
                    "public class TestNavigationModel {",
                    "    @BindExtra ClassWithRequiredAndOptionalExtrasNavigationModel extra;",
                    "    class ClassWithRequiredAndOptionalExtrasNavigationModel implements Parcelable {",
                    "        public void writeToParcel(android.os.Parcel out, int flags) {",
                    "        }",
                    "        public int describeContents() {",
                    "            return 0;",
                    "        }",
                    "    }",
                    "}"));

    String intentBuilderQualifiedName = "test.navigation.Test__IntentBuilder";
    JavaFileObject builderSource =
        JavaFileObjects.forSourceString(
            intentBuilderQualifiedName,
            Joiner.on('\n')
                .join( //
                    "package test.navigation;",
                    "import static dart.henson.ActivityClassFinder.getClassDynamically;",
                    "import android.content.Context;",
                    "import android.content.Intent;",
                    "import dart.henson.AllRequiredSetState;",
                    "import dart.henson.Bundler;",
                    "import dart.henson.RequiredStateSequence;",
                    "public class Test__IntentBuilder {",
                    "  public static InitialState getInitialState(Context context) {",
                    "    final Intent intent = new Intent(context, getClassDynamically(\"test.navigation.Test\"));",
                    "    final Bundler bundler = Bundler.create();",
                    "    return new InitialState(bundler, intent);",
                    "  }",
                    "  public static <ALL_SET extends AllSet> RequiredSequence<ALL_SET> getNextState(Bundler bundler,",
                    "          ALL_SET allSetState) {",
                    "    return new RequiredSequence<>(bundler, allSetState);",
                    "  }",
                    "  public static class RequiredSequence<ALL_SET extends AllSet> extends RequiredStateSequence<ALL_SET> {",
                    "    public RequiredSequence(Bundler bundler, ALL_SET allRequiredSetState) {",
                    "      super(bundler, allRequiredSetState);",
                    "    }",
                    "    public ALL_SET extra(TestNavigationModel.ClassWithRequiredAndOptionalExtrasNavigationModel extra) {",
                    "      bundler.put(\"extra\",(android.os.Parcelable) extra);",
                    "      return allRequiredSetState;",
                    "    }",
                    "  }",
                    "  public static class AllSet<SELF extends AllSet<SELF>> extends AllRequiredSetState {",
                    "    public AllSet(Bundler bundler, Intent intent) {",
                    "      super(bundler, intent);",
                    "    }",
                    "  }",
                    "  public static class ResolvedAllSet extends AllSet<ResolvedAllSet> {",
                    "    public ResolvedAllSet(Bundler bundler, Intent intent) {",
                    "      super(bundler, intent);",
                    "    }",
                    "  }",
                    "  public static class InitialState extends RequiredSequence<ResolvedAllSet> {",
                    "    public InitialState(Bundler bundler, Intent intent) {",
                    "      super(bundler, new ResolvedAllSet(bundler, intent));",
                    "    }",
                    "  }",
                    "}"));

    IntentBuilderProcessor processor = hensonProcessorWithoutParceler();
    Compilation compilation = javac().withProcessors(processor).compile(source);
    assertThat(compilation)
        .generatedSourceFile(intentBuilderQualifiedName)
        .hasSourceEquivalentTo(builderSource);

    TypeElement originatingElement = processor.getOriginatingElement(intentBuilderQualifiedName);
    TypeElement mostEnclosingElement = getMostEnclosingElement(originatingElement);
    assertTrue(
        mostEnclosingElement
            .getQualifiedName()
            .contentEquals("test.navigation.TestNavigationModel"));
  }

  @Test
  public void
      intentBuilderGenerator_should_generateCode_when_extraIsParcelableThatExtendsParcelable() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;",
                    "import java.util.List;",
                    "import android.os.Parcelable;",
                    "import dart.BindExtra;",
                    "public class TestNavigationModel {",
                    "    @BindExtra ClassWithRequiredAndOptionalExtrasNavigationModel extra;",
                    "    class FooParent implements Parcelable {",
                    "        public void writeToParcel(android.os.Parcel out, int flags) {",
                    "        }",
                    "        public int describeContents() {",
                    "            return 0;",
                    "        }",
                    "    }",
                    "    class ClassWithRequiredAndOptionalExtrasNavigationModel extends FooParent implements Parcelable {",
                    "        public void writeToParcel(android.os.Parcel out, int flags) {",
                    "        }",
                    "        public int describeContents() {",
                    "            return 0;",
                    "        }",
                    "    }",
                    "}"));

    String intentBuilderQualifiedName = "test.navigation.Test__IntentBuilder";
    JavaFileObject builderSource =
        JavaFileObjects.forSourceString(
            intentBuilderQualifiedName,
            Joiner.on('\n')
                .join( //
                    "package test.navigation;",
                    "import static dart.henson.ActivityClassFinder.getClassDynamically;",
                    "import android.content.Context;",
                    "import android.content.Intent;",
                    "import dart.henson.AllRequiredSetState;",
                    "import dart.henson.Bundler;",
                    "import dart.henson.RequiredStateSequence;",
                    "public class Test__IntentBuilder {",
                    "  public static InitialState getInitialState(Context context) {",
                    "    final Intent intent = new Intent(context, getClassDynamically(\"test.navigation.Test\"));",
                    "    final Bundler bundler = Bundler.create();",
                    "    return new InitialState(bundler, intent);",
                    "  }",
                    "",
                    "  public static <ALL_SET extends AllSet> RequiredSequence<ALL_SET> getNextState(Bundler bundler,",
                    "          ALL_SET allSetState) {",
                    "    return new RequiredSequence<>(bundler, allSetState);",
                    "  }",
                    "  public static class RequiredSequence<ALL_SET extends AllSet> extends RequiredStateSequence<ALL_SET> {",
                    "    public RequiredSequence(Bundler bundler, ALL_SET allRequiredSetState) {",
                    "      super(bundler, allRequiredSetState);",
                    "    }",
                    "    public ALL_SET extra(TestNavigationModel.ClassWithRequiredAndOptionalExtrasNavigationModel extra) {",
                    "      bundler.put(\"extra\",(android.os.Parcelable) extra);",
                    "      return allRequiredSetState;",
                    "    }",
                    "  }",
                    "  public static class AllSet<SELF extends AllSet<SELF>> extends AllRequiredSetState {",
                    "    public AllSet(Bundler bundler, Intent intent) {",
                    "      super(bundler, intent);",
                    "    }",
                    "  }",
                    "  public static class ResolvedAllSet extends AllSet<ResolvedAllSet> {",
                    "    public ResolvedAllSet(Bundler bundler, Intent intent) {",
                    "      super(bundler, intent);",
                    "    }",
                    "  }",
                    "  public static class InitialState extends RequiredSequence<ResolvedAllSet> {",
                    "    public InitialState(Bundler bundler, Intent intent) {",
                    "      super(bundler, new ResolvedAllSet(bundler, intent));",
                    "    }",
                    "  }",
                    "}"));

    IntentBuilderProcessor processor = hensonProcessorWithoutParceler();
    Compilation compilation = javac().withProcessors(processor).compile(source);
    assertThat(compilation)
        .generatedSourceFile(intentBuilderQualifiedName)
        .hasSourceEquivalentTo(builderSource);

    TypeElement originatingElement = processor.getOriginatingElement(intentBuilderQualifiedName);
    TypeElement mostEnclosingElement = getMostEnclosingElement(originatingElement);
    assertTrue(
        mostEnclosingElement
            .getQualifiedName()
            .contentEquals("test.navigation.TestNavigationModel"));
  }
}
