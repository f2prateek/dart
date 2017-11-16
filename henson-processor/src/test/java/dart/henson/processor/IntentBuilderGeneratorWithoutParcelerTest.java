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

/** For tests related to Parceler, but Parceler is not available. */
public class IntentBuilderGeneratorWithoutParcelerTest {

  @Test
  public void intentBuilderGenerator_should_generateCode_when_extraIsSerializableCollection() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import java.util.ArrayList;", //
                    "import dart.BindExtra;", //
                    "import dart.DartModel;", //
                    "@DartModel(\"test.Test\")", //
                    "public class TestNavigationModel {", //
                    "    @BindExtra(\"key\") ArrayList<String> extra;", //
                    "}" //
                    ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString(
            "test.navigation.Test__IntentBuilder",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import android.content.Context;", //
                    "import android.content.Intent;", //
                    "import dart.henson.Bundler;", //
                    "import java.lang.Class;", //
                    "import java.lang.Exception;", //
                    "import java.lang.String;", //
                    "import java.util.ArrayList;", //
                    "public class Test__IntentBuilder {", //
                    "  private Intent intent;", //
                    "  private Bundler bundler = Bundler.create();", //
                    "  public Test__IntentBuilder(Context context) {", //
                    "    intent = new Intent(context, getClassDynamically(\"test.Test\"));", //
                    "  }", //
                    "  public Class getClassDynamically(String className) {", //
                    "    try {", //
                    "      return Class.forName(className);", //
                    "    } catch(Exception ex) {", //
                    "      throw new RuntimeException(ex);", //
                    "    }", //
                    "  }", //
                    "  public Test__IntentBuilder.AllSet key(ArrayList<String> extra) {", //
                    "    bundler.put(\"key\", extra);", //
                    "    return new Test__IntentBuilder.AllSet();", //
                    "  }", //
                    "  public class AllSet {", //
                    "    public Intent build() {", //
                    "      intent.putExtras(bundler.get());", //
                    "      return intent;", //
                    "    }", //
                    "  }", //
                    "}" //
                    ));

    Compilation compilation =
        javac()
            .withProcessors(ProcessorTestUtilities.hensonProcessorWithoutParceler())
            .compile(source);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Test__IntentBuilder")
        .hasSourceEquivalentTo(builderSource);
  }

  @Test
  public void intentBuilderGenerator_should_fail_when_extraIsNonSerializableCollection() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import java.util.List;", //
                    "import dart.BindExtra;", //
                    "import dart.DartModel;", //
                    "@DartModel(\"test.Test\")", //
                    "public class TestNavigationModel {", //
                    "    @BindExtra(\"key\") List<String> extra;", //
                    "}" //
                    ));

    Compilation compilation =
        javac()
            .withProcessors(ProcessorTestUtilities.hensonProcessorWithoutParceler())
            .compile(source);
    assertThat(compilation)
        .hadErrorContaining("@BindExtra field must be a primitive or Serializable or Parcelable");
  }

  @Test
  public void
      intentBuilderGenerator_should_fail_when_extraIsAnnotatedWithParceler_and_parcelerIsOff() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.BindExtra;", //
                    "import dart.DartModel;", //
                    "import org.parceler.Parcel;", //
                    "@DartModel(\"test.Test\")", //
                    "public class TestNavigationModel {", //
                    "    @BindExtra(\"key\") Foo extra;", //
                    "    @Parcel static class Foo {}", //
                    "}" //
                    ));

    Compilation compilation =
        javac()
            .withProcessors(ProcessorTestUtilities.hensonProcessorWithoutParceler())
            .compile(source);
    assertThat(compilation)
        .hadErrorContaining("@BindExtra field must be a primitive or Serializable or Parcelable");
  }

  @Test
  public void
      intentBuilderGenerator_should_fail_when_extraIsCollectionOfElementAnnotatedWithParceler_and_parcelerIsOff() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import java.util.List;", //
                    "import dart.BindExtra;", //
                    "import dart.DartModel;", //
                    "@DartModel(\"test.Test\")", //
                    "public class TestNavigationModel {", //
                    "    @BindExtra(\"key\") List<Foo> extra;", //
                    "    @Parcel static class Foo {}", //
                    "}" //
                    ));

    Compilation compilation =
        javac()
            .withProcessors(ProcessorTestUtilities.hensonProcessorWithoutParceler())
            .compile(source);
    assertThat(compilation)
        .hadErrorContaining("@BindExtra field must be a primitive or Serializable or Parcelable");
  }

  @Test
  public void intentBuilderGenerator_should_generateCode_when_extraIsParcelable() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import java.util.List;", //
                    "import android.os.Parcelable;", //
                    "import dart.BindExtra;", //
                    "import dart.DartModel;", //
                    "@DartModel(\"test.Test\")", //
                    "public class TestNavigationModel {", //
                    "    @BindExtra(\"key\") Foo extra;", //
                    "    class Foo implements Parcelable {", //
                    "        public void writeToParcel(android.os.Parcel out, int flags) {", //
                    "        }", //
                    "        public int describeContents() {", //
                    "            return 0;", //
                    "        }", //
                    "    }", //
                    "}" //
                    ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString(
            "test.navigation.Test__IntentBuilder",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import android.content.Context;", //
                    "import android.content.Intent;", //
                    "import dart.henson.Bundler;", //
                    "import java.lang.Class;", //
                    "import java.lang.Exception;", //
                    "import java.lang.String;", //
                    "public class Test__IntentBuilder {", //
                    "  private Intent intent;", //
                    "  private Bundler bundler = Bundler.create();", //
                    "  public Test__IntentBuilder(Context context) {", //
                    "    intent = new Intent(context, getClassDynamically(\"test.Test\"));", //
                    "  }", //
                    "  public Class getClassDynamically(String className) {", //
                    "    try {", //
                    "      return Class.forName(className);", //
                    "    } catch(Exception ex) {", //
                    "      throw new RuntimeException(ex);", //
                    "    }", //
                    "  }", //
                    "  public Test__IntentBuilder.AllSet key(TestNavigationModel.Foo extra) {", //
                    "    bundler.put(\"key\",(android.os.Parcelable) extra);", //
                    "    return new Test__IntentBuilder.AllSet();", //
                    "  }", //
                    "  public class AllSet {", //
                    "    public Intent build() {", //
                    "      intent.putExtras(bundler.get());", //
                    "      return intent;", //
                    "    }", //
                    "  }", //
                    "}" //
                    ));

    Compilation compilation =
        javac()
            .withProcessors(ProcessorTestUtilities.hensonProcessorWithoutParceler())
            .compile(source);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Test__IntentBuilder")
        .hasSourceEquivalentTo(builderSource);
  }

  @Test
  public void
      intentBuilderGenerator_should_generateCode_when_extraIsParcelableThatExtendsParcelable() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import java.util.List;", //
                    "import android.os.Parcelable;", //
                    "import dart.BindExtra;", //
                    "import dart.DartModel;", //
                    "@DartModel(\"test.Test\")", //
                    "public class TestNavigationModel {", //
                    "    @BindExtra(\"key\") Foo extra;", //
                    "    class FooParent implements Parcelable {", //
                    "        public void writeToParcel(android.os.Parcel out, int flags) {", //
                    "        }", //
                    "        public int describeContents() {", //
                    "            return 0;", //
                    "        }", //
                    "    }", //
                    "    class Foo extends FooParent implements Parcelable {", //
                    "        public void writeToParcel(android.os.Parcel out, int flags) {", //
                    "        }", //
                    "        public int describeContents() {", //
                    "            return 0;", //
                    "        }", //
                    "    }", //
                    "}" //
                    ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString(
            "test.navigation.Test__IntentBuilder",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import android.content.Context;", //
                    "import android.content.Intent;", //
                    "import dart.henson.Bundler;", //
                    "import java.lang.Class;", //
                    "import java.lang.Exception;", //
                    "import java.lang.String;", //
                    "public class Test__IntentBuilder {", //
                    "  private Intent intent;", //
                    "  private Bundler bundler = Bundler.create();", //
                    "  public Test__IntentBuilder(Context context) {", //
                    "    intent = new Intent(context, getClassDynamically(\"test.Test\"));", //
                    "  }", //
                    "  public Class getClassDynamically(String className) {", //
                    "    try {", //
                    "      return Class.forName(className);", //
                    "    } catch(Exception ex) {", //
                    "      throw new RuntimeException(ex);", //
                    "    }", //
                    "  }", //
                    "  public Test__IntentBuilder.AllSet key(TestNavigationModel.Foo extra) {", //
                    "    bundler.put(\"key\",(android.os.Parcelable) extra);", //
                    "    return new Test__IntentBuilder.AllSet();", //
                    "  }", //
                    "  public class AllSet {", //
                    "    public Intent build() {", //
                    "      intent.putExtras(bundler.get());", //
                    "      return intent;", //
                    "    }", //
                    "  }", //
                    "}" //
                    ));

    Compilation compilation =
        javac()
            .withProcessors(ProcessorTestUtilities.hensonProcessorWithoutParceler())
            .compile(source);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Test__IntentBuilder")
        .hasSourceEquivalentTo(builderSource);
  }
}
