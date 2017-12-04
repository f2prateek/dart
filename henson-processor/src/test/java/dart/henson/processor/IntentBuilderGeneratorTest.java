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

import com.google.common.base.Joiner;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class IntentBuilderGeneratorTest {

  @Test
  public void
  intentBuilderGenerator_should_generateIntentBuilder_when_navigationModelIsDefined_and_containsExtras() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.DartModel;", //
                    "@DartModel", //
                    "public class TestNavigationModel {", //
                    "    String extra;", //
                    "}" //
                ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel__IntentBuilder",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import static dart.henson.ActivityClassFinder.getClassDynamically;", //
                    "import android.content.Context;", //
                    "import android.content.Intent;", //
                    "import dart.henson.AllRequiredSetState;", //
                    "import dart.henson.Bundler;", //
                    "import dart.henson.RequiredStateSequence;", //
                    "import java.lang.String;", //
                    "public class TestNavigationModel__IntentBuilder {", //
                    "  public static RequiredSequence<ResolvedAllSet> getInitialState(Context context) {", //
                    "    final Intent intent = new Intent(context, getClassDynamically(\"test.navigation.Test\"));", //
                    "    final Bundler bundler = Bundler.create();", //
                    "    final ResolvedAllSet resolvedAllSet = new ResolvedAllSet(bundler, intent);", //
                    "    return new RequiredSequence<>(bundler, resolvedAllSet);", //
                    "  }", //
                    "  public static <ALL_SET extends AllSet> RequiredSequence<ALL_SET> getInitialState(Bundler bundler,", //
                    "      ALL_SET allSetState) {", //
                    "    return new RequiredSequence<>(bundler, allSetState);", //
                    "  }", //
                    "  public static class RequiredSequence<ALL_SET extends AllSet> extends RequiredStateSequence<ALL_SET> {", //
                    "    public RequiredSequence(Bundler bundler, ALL_SET allRequiredSetState) {", //
                    "      super(bundler, allRequiredSetState);", //
                    "    }", //
                    "    public ALL_SET extra(String extra) {", //
                    "      bundler.put(\"extra\", extra);", //
                    "      return allRequiredSetState;", //
                    "    }", //
                    "  }", //
                    "", //
                    "  public static class AllSet<SELF extends AllSet<SELF>> extends AllRequiredSetState {", //
                    "    public AllSet(Bundler bundler, Intent intent) {", //
                    "      super(bundler, intent);", //
                    "    }", //
                    "  }", //
                    "  public static class ResolvedAllSet extends AllSet<ResolvedAllSet> {", //
                    "    public ResolvedAllSet(Bundler bundler, Intent intent) {", //
                    "      super(bundler, intent);", //
                    "    }", //
                    "  }", //
                    "}" //
                ));

    Compilation compilation =
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.TestNavigationModel__IntentBuilder")
        .hasSourceEquivalentTo(builderSource);
  }

  @Test
  public void
  intentBuilderGenerator_should_generateIntentBuilder_when_navigationModelIsDefined_and_doesNotContainExtras() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.BindExtra;", //
                    "import dart.DartModel;", //
                    "@DartModel(\"test.Test\")", //
                    "public class TestNavigationModel {", //
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
                    "  public Intent build() {", //
                    "    intent.putExtras(bundler.get());", //
                    "    return intent;", //
                    "  }", //
                    "}" //
                ));

    Compilation compilation =
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Test__IntentBuilder")
        .hasSourceEquivalentTo(builderSource);
  }

  @Test(expected = AssertionError.class)
  public void
  intentBuilderGenerator_should_notGenerateIntentBuilder_when_navigationModelIsNotDefined_and_containsExtras() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.BindExtra;", //
                    "import dart.DartModel;", //
                    "public class TestNavigationModel {", //
                    "    @BindExtra(\"key\") String extra;", //
                    "}" //
                ));

    Compilation compilation =
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation).generatedSourceFile("test.navigation.Test__IntentBuilder");
  }

  @Test
  public void
  intentBuilderGenerator_should_generateIntentBuilder_when_navigationModelIsDefined_and_targetClassIsInner() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.BindExtra;", //
                    "import dart.DartModel;", //
                    "@DartModel(\"test.Test$MyInnerTest\")", //
                    "public class TestNavigationModel {", //
                    "}" //
                ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString(
            "test.navigation.Test$MyInnerTest__IntentBuilder",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import android.content.Context;", //
                    "import android.content.Intent;", //
                    "import dart.henson.Bundler;", //
                    "import java.lang.Class;", //
                    "import java.lang.Exception;", //
                    "import java.lang.String;", //
                    "public class Test$MyInnerTest__IntentBuilder {", //
                    "  private Intent intent;", //
                    "  private Bundler bundler = Bundler.create();", //
                    "  public Test$MyInnerTest__IntentBuilder(Context context) {", //
                    "    intent = new Intent(context, getClassDynamically(\"test.Test.MyInnerTest\"));",
                    //
                    "  }", //
                    "  public Class getClassDynamically(String className) {", //
                    "    try {", //
                    "      return Class.forName(className);", //
                    "    } catch(Exception ex) {", //
                    "      throw new RuntimeException(ex);", //
                    "    }", //
                    "  }", //
                    "  public Intent build() {", //
                    "    intent.putExtras(bundler.get());", //
                    "    return intent;", //
                    "  }", //
                    "}" //
                ));

    Compilation compilation =
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Test$MyInnerTest__IntentBuilder")
        .hasSourceEquivalentTo(builderSource);
  }

  @Test
  public void
  intentBuilderGenerator_should_generateIntentBuilders_when_navigationModelIsDefined_and_containsExtras_and_sameForParent() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel1",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.BindExtra;", //
                    "import dart.DartModel;", //
                    "@DartModel(\"test.Test1\")", //
                    "public class TestNavigationModel1 extends TestNavigationModel2 {", //
                    "    @BindExtra(\"key1\") String extra1;", //
                    "}", //
                    "@DartModel(\"test.Test2\")", //
                    "class TestNavigationModel2 {", //
                    "    @BindExtra(\"key2\") String extra2;", //
                    "}" //
                ));

    JavaFileObject builderSource1 =
        JavaFileObjects.forSourceString(
            "test.navigation.Test1__IntentBuilder",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import android.content.Context;", //
                    "import android.content.Intent;", //
                    "import dart.henson.Bundler;", //
                    "import java.lang.Class;", //
                    "import java.lang.Exception;", //
                    "import java.lang.String;", //
                    "public class Test1__IntentBuilder {", //
                    "  private Intent intent;", //
                    "  private Bundler bundler = Bundler.create();", //
                    "  public Test1__IntentBuilder(Context context) {", //
                    "    intent = new Intent(context, getClassDynamically(\"test.Test1\"));", //
                    "  }", //
                    "  public Class getClassDynamically(String className) {", //
                    "    try {", //
                    "      return Class.forName(className);", //
                    "    } catch(Exception ex) {", //
                    "      throw new RuntimeException(ex);", //
                    "    }", //
                    "  }", //
                    "  public Test1__IntentBuilder.AfterSettingKey1 key1(String extra1) {", //
                    "    bundler.put(\"key1\", extra1);", //
                    "    return new Test1__IntentBuilder.AfterSettingKey1();", //
                    "  }", //
                    "  public class AfterSettingKey1 {", //
                    "    public Test1__IntentBuilder.AllSet key2(String extra2) {", //
                    "      bundler.put(\"key2\", extra2);", //
                    "      return new Test1__IntentBuilder.AllSet();", //
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

    JavaFileObject builderSource2 =
        JavaFileObjects.forSourceString(
            "test.navigation.Test2__IntentBuilder",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import android.content.Context;", //
                    "import android.content.Intent;", //
                    "import dart.henson.Bundler;", //
                    "import java.lang.Class;", //
                    "import java.lang.Exception;", //
                    "import java.lang.String;", //
                    "public class Test2__IntentBuilder {", //
                    "  private Intent intent;", //
                    "  private Bundler bundler = Bundler.create();", //
                    "  public Test2__IntentBuilder(Context context) {", //
                    "    intent = new Intent(context, getClassDynamically(\"test.Test2\"));", //
                    "  }", //
                    "  public Class getClassDynamically(String className) {", //
                    "    try {", //
                    "      return Class.forName(className);", //
                    "    } catch(Exception ex) {", //
                    "      throw new RuntimeException(ex);", //
                    "    }", //
                    "  }", //
                    "  public Test2__IntentBuilder.AllSet key2(String extra2) {", //
                    "    bundler.put(\"key2\", extra2);", //
                    "    return new Test2__IntentBuilder.AllSet();", //
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
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Test1__IntentBuilder")
        .hasSourceEquivalentTo(builderSource1);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Test2__IntentBuilder")
        .hasSourceEquivalentTo(builderSource2);
  }

  @Test
  public void
  intentBuilderGenerator_should_generateIntentBuilders_when_navigationModelIsDefined_and_containsExtras_and_sameForParent_and_keyIsRepeated() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel1",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.BindExtra;", //
                    "import dart.DartModel;", //
                    "@DartModel(\"test.Test1\")", //
                    "public class TestNavigationModel1 extends TestNavigationModel2 {", //
                    "    @BindExtra(\"key\") String extra1;", //
                    "}", //
                    "@DartModel(\"test.Test2\")", //
                    "class TestNavigationModel2 {", //
                    "    @BindExtra(\"key\") Integer extra2;", //
                    "}" //
                ));

    JavaFileObject builderSource1 =
        JavaFileObjects.forSourceString(
            "test.navigation.Test1__IntentBuilder",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import android.content.Context;", //
                    "import android.content.Intent;", //
                    "import dart.henson.Bundler;", //
                    "import java.lang.Class;", //
                    "import java.lang.Exception;", //
                    "import java.lang.Integer;", //
                    "import java.lang.String;", //
                    "public class Test1__IntentBuilder {", //
                    "  private Intent intent;", //
                    "  private Bundler bundler = Bundler.create();", //
                    "  public Test1__IntentBuilder(Context context) {", //
                    "    intent = new Intent(context, getClassDynamically(\"test.Test1\"));", //
                    "  }", //
                    "  public Class getClassDynamically(String className) {", //
                    "    try {", //
                    "      return Class.forName(className);", //
                    "    } catch(Exception ex) {", //
                    "      throw new RuntimeException(ex);", //
                    "    }", //
                    "  }", //
                    "  public Test1__IntentBuilder.AllSet key(Integer extra2) {", //
                    "    bundler.put(\"key\", extra2);", //
                    "    return new Test1__IntentBuilder.AllSet();", //
                    "  }", //
                    "  public class AllSet {", //
                    "    public Intent build() {", //
                    "      intent.putExtras(bundler.get());", //
                    "      return intent;", //
                    "    }", //
                    "  }", //
                    "}" //
                ));

    JavaFileObject builderSource2 =
        JavaFileObjects.forSourceString(
            "test.navigation.Test2__IntentBuilder",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import android.content.Context;", //
                    "import android.content.Intent;", //
                    "import dart.henson.Bundler;", //
                    "import java.lang.Class;", //
                    "import java.lang.Exception;", //
                    "import java.lang.Integer;", //
                    "import java.lang.String;", //
                    "public class Test2__IntentBuilder {", //
                    "  private Intent intent;", //
                    "  private Bundler bundler = Bundler.create();", //
                    "  public Test2__IntentBuilder(Context context) {", //
                    "    intent = new Intent(context, getClassDynamically(\"test.Test2\"));", //
                    "  }", //
                    "  public Class getClassDynamically(String className) {", //
                    "    try {", //
                    "      return Class.forName(className);", //
                    "    } catch(Exception ex) {", //
                    "      throw new RuntimeException(ex);", //
                    "    }", //
                    "  }", //
                    "  public Test2__IntentBuilder.AllSet key(Integer extra2) {", //
                    "    bundler.put(\"key\", extra2);", //
                    "    return new Test2__IntentBuilder.AllSet();", //
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
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Test1__IntentBuilder")
        .hasSourceEquivalentTo(builderSource1);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Test2__IntentBuilder")
        .hasSourceEquivalentTo(builderSource2);
  }

  @Test
  public void
  intentBuilderGenerator_should_generateIntentBuilderWithParentExtras_when_navigationModelIsDefined_and_doesNotContainExtras() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel1",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.BindExtra;", //
                    "import dart.DartModel;", //
                    "@DartModel(\"test.Test1\")", //
                    "public class TestNavigationModel1 extends TestNavigationModel2 {", //
                    "}", //
                    "class TestNavigationModel2 {", //
                    "    @BindExtra(\"key2\") String extra2;", //
                    "}" //
                ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString(
            "test.navigation.Test1__IntentBuilder",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import android.content.Context;", //
                    "import android.content.Intent;", //
                    "import dart.henson.Bundler;", //
                    "import java.lang.Class;", //
                    "import java.lang.Exception;", //
                    "import java.lang.String;", //
                    "public class Test1__IntentBuilder {", //
                    "  private Intent intent;", //
                    "  private Bundler bundler = Bundler.create();", //
                    "  public Test1__IntentBuilder(Context context) {", //
                    "    intent = new Intent(context, getClassDynamically(\"test.Test1\"));", //
                    "  }", //
                    "  public Class getClassDynamically(String className) {", //
                    "    try {", //
                    "      return Class.forName(className);", //
                    "    } catch(Exception ex) {", //
                    "      throw new RuntimeException(ex);", //
                    "    }", //
                    "  }", //
                    "  public Test1__IntentBuilder.AllSet key2(String extra2) {", //
                    "    bundler.put(\"key2\", extra2);", //
                    "    return new Test1__IntentBuilder.AllSet();", //
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
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Test1__IntentBuilder")
        .hasSourceEquivalentTo(builderSource);
  }

  //Test for https://github.com/f2prateek/dart/issues/64
  @Test
  public void intentBuilderGenerator_should_generateIntentBuilderWithAncestorExtras() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.Test",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.BindExtra;", //
                    "import dart.DartModel;", //
                    "public abstract class Test {", //
                    "    @BindExtra(\"key\") String extra;", //
                    "}", //
                    "@DartModel(\"test.Test2\")", //
                    "class TestAwo extends TestOne {", //
                    "}", //
                    "@DartModel(\"test.Test1\")", //
                    "class TestOne extends Test {", //
                    "}" //
                ));

    JavaFileObject expectedSource =
        JavaFileObjects.forSourceString(
            "test.navigation.Test2__IntentBuilder",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import android.content.Context;", //
                    "import android.content.Intent;", //
                    "import dart.henson.Bundler;", //
                    "import java.lang.Class;", //
                    "import java.lang.Exception;", //
                    "import java.lang.String;", //
                    "public class Test2__IntentBuilder {", //
                    "  private Intent intent;", //
                    "  private Bundler bundler = Bundler.create();", //
                    "  public Test2__IntentBuilder(Context context) {", //
                    "    intent = new Intent(context, getClassDynamically(\"test.Test2\"));", //
                    "  }", //
                    "  public Class getClassDynamically(String className) {", //
                    "    try {", //
                    "      return Class.forName(className);", //
                    "    } catch(Exception ex) {", //
                    "      throw new RuntimeException(ex);", //
                    "    }", //
                    "  }", //
                    "  public Test2__IntentBuilder.AllSet key(String extra) {", //
                    "    bundler.put(\"key\", extra);", //
                    "    return new Test2__IntentBuilder.AllSet();", //
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
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Test2__IntentBuilder")
        .hasSourceEquivalentTo(expectedSource);
  }

  @Test
  public void
  intentBuilderGenerator_should_generateIntentBuilderWithParentExtras_when_navigationModelIsDefined_and_doesNotContainExtras_and_parentIsAbstract() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel1",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.BindExtra;", //
                    "import dart.DartModel;", //
                    "@DartModel(\"test.Test1\")", //
                    "public class TestNavigationModel1 extends TestNavigationModel2 {", //
                    "}", //
                    "abstract class TestNavigationModel2 {", //
                    "    @BindExtra(\"key2\") String extra2;", //
                    "}" //
                ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString(
            "test.navigation.Test1__IntentBuilder",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import android.content.Context;", //
                    "import android.content.Intent;", //
                    "import dart.henson.Bundler;", //
                    "import java.lang.Class;", //
                    "import java.lang.Exception;", //
                    "import java.lang.String;", //
                    "public class Test1__IntentBuilder {", //
                    "  private Intent intent;", //
                    "  private Bundler bundler = Bundler.create();", //
                    "  public Test1__IntentBuilder(Context context) {", //
                    "    intent = new Intent(context, getClassDynamically(\"test.Test1\"));", //
                    "  }", //
                    "  public Class getClassDynamically(String className) {", //
                    "    try {", //
                    "      return Class.forName(className);", //
                    "    } catch(Exception ex) {", //
                    "      throw new RuntimeException(ex);", //
                    "    }", //
                    "  }", //
                    "  public Test1__IntentBuilder.AllSet key2(String extra2) {", //
                    "    bundler.put(\"key2\", extra2);", //
                    "    return new Test1__IntentBuilder.AllSet();", //
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
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Test1__IntentBuilder")
        .hasSourceEquivalentTo(builderSource);
  }

  @Test
  public void
  intentBuilderGenerator_should_generateIntentBuilder_when_navigationModelIsDefined_and_parentContainsGenerics() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel1",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.BindExtra;", //
                    "import dart.DartModel;", //
                    "@DartModel(\"test.Test1\")", //
                    "class TestNavigationModel1 extends TestNavigationModel2<String> {", //
                    "    @BindExtra(\"key1\") String extra1;", //
                    "}", //
                    "class TestNavigationModel2<T> {", //
                    "    @BindExtra(\"key2\") String extra2;", //
                    "}" //
                ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString(
            "test.navigation.Test1__IntentBuilder",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import android.content.Context;", //
                    "import android.content.Intent;", //
                    "import dart.henson.Bundler;", //
                    "import java.lang.Class;", //
                    "import java.lang.Exception;", //
                    "import java.lang.String;", //
                    "public class Test1__IntentBuilder {", //
                    "  private Intent intent;", //
                    "  private Bundler bundler = Bundler.create();", //
                    "  public Test1__IntentBuilder(Context context) {", //
                    "    intent = new Intent(context, getClassDynamically(\"test.Test1\"));", //
                    "  }", //
                    "  public Class getClassDynamically(String className) {", //
                    "    try {", //
                    "      return Class.forName(className);", //
                    "    } catch(Exception ex) {", //
                    "      throw new RuntimeException(ex);", //
                    "    }", //
                    "  }", //
                    "  public Test1__IntentBuilder.AfterSettingKey1 key1(String extra1) {", //
                    "    bundler.put(\"key1\", extra1);", //
                    "    return new Test1__IntentBuilder.AfterSettingKey1();", //
                    "  }", //
                    "  public class AfterSettingKey1 {", //
                    "    public Test1__IntentBuilder.AllSet key2(String extra2) {", //
                    "      bundler.put(\"key2\", extra2);", //
                    "      return new Test1__IntentBuilder.AllSet();", //
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

    Compilation compilation =
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Test1__IntentBuilder")
        .hasSourceEquivalentTo(builderSource);
  }

  @Test
  public void
  intentBuilderGenerator_should_generateIntentBuilder_when_navigationModelIsDefined_and_containsPrimitiveExtras() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.BindExtra;", //
                    "import dart.DartModel;", //
                    "@DartModel(\"test.Test\")", //
                    "public class TestNavigationModel {", //
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
                    "  public Test__IntentBuilder.AfterSettingKey_bool key_bool(boolean aBool) {",
                    //
                    "    bundler.put(\"key_bool\",aBool);", //
                    "    return new Test__IntentBuilder.AfterSettingKey_bool();", //
                    "  }", //
                    "  public class AfterSettingKey_bool {", //
                    "    public Test__IntentBuilder.AfterSettingKey_byte key_byte(byte aByte) {", //
                    "      bundler.put(\"key_byte\",aByte);", //
                    "      return new Test__IntentBuilder.AfterSettingKey_byte();", //
                    "    }", //
                    "  }", //
                    "  public class AfterSettingKey_byte {", //
                    "    public Test__IntentBuilder.AfterSettingKey_char key_char(char aChar) {", //
                    "      bundler.put(\"key_char\",aChar);", //
                    "      return new Test__IntentBuilder.AfterSettingKey_char();", //
                    "    }", //
                    "  }", //
                    "  public class AfterSettingKey_char {", //
                    "    public Test__IntentBuilder.AfterSettingKey_double key_double(double aDouble) {",
                    //
                    "      bundler.put(\"key_double\",aDouble);", //
                    "      return new Test__IntentBuilder.AfterSettingKey_double();", //
                    "    }", //
                    "  }", //
                    "  public class AfterSettingKey_double {", //
                    "    public Test__IntentBuilder.AfterSettingKey_float key_float(float aFloat) {",
                    //
                    "      bundler.put(\"key_float\",aFloat);", //
                    "      return new Test__IntentBuilder.AfterSettingKey_float();", //
                    "    }", //
                    "  }", //
                    "  public class AfterSettingKey_float {", //
                    "    public Test__IntentBuilder.AfterSettingKey_int key_int(int anInt) {", //
                    "      bundler.put(\"key_int\",anInt);", //
                    "      return new Test__IntentBuilder.AfterSettingKey_int();", //
                    "    }", //
                    "  }", //
                    "  public class AfterSettingKey_int {", //
                    "    public Test__IntentBuilder.AfterSettingKey_long key_long(long aLong) {", //
                    "      bundler.put(\"key_long\",aLong);", //
                    "      return new Test__IntentBuilder.AfterSettingKey_long();", //
                    "    }", //
                    "  }", //
                    "  public class AfterSettingKey_long {", //
                    "    public Test__IntentBuilder.AllSet key_short(short aShort) {", //
                    "      bundler.put(\"key_short\",aShort);", //
                    "      return new Test__IntentBuilder.AllSet();", //
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

    Compilation compilation =
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Test__IntentBuilder")
        .hasSourceEquivalentTo(builderSource);
  }

  @Test
  public void
  intentBuilderGenerator_should_generateIntentBuilder_when_navigationModelIsDefined_and_containsSerializableAndParcelableExtra() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import android.os.Parcelable;", //
                    "import java.io.Serializable;", //
                    "import dart.BindExtra;", //
                    "import dart.DartModel;", //
                    "class Extra implements Serializable, Parcelable {", //
                    "  public void writeToParcel(android.os.Parcel out, int flags) {", //
                    "  }", //
                    "  public int describeContents() {", //
                    "    return 0;", //
                    "  }", //
                    "}", //
                    "@DartModel(\"test.Test\")", //
                    "public class TestNavigationModel {", //
                    "    @BindExtra(\"key\") Extra extra;", //
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
                    "  public Test__IntentBuilder.AllSet key(Extra extra) {", //
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
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Test__IntentBuilder")
        .hasSourceEquivalentTo(builderSource);
  }

  @Test
  public void intentBuilderGenerator_should_containOnlyOneSetter_when_keysAreRepeated() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.BindExtra;", //
                    "import dart.DartModel;", //
                    "@DartModel(\"test.Test\")", //
                    "public class TestNavigationModel {", //
                    "    @BindExtra(\"key\") String extra1;", //
                    "    @BindExtra(\"key\") String extra2;", //
                    "    @BindExtra(\"key\") String extra3;", //
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
                    "  public Test__IntentBuilder.AllSet key(String extra1) {", //
                    "    bundler.put(\"key\", extra1);", //
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
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Test__IntentBuilder")
        .hasSourceEquivalentTo(builderSource);
  }

  @Test
  public void intentBuilderGenerator_should_useDefaultKey_when_noKeyIsProvided() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.BindExtra;", //
                    "import dart.DartModel;", //
                    "@DartModel(\"test.Test\")", //
                    "public class TestNavigationModel {", //
                    "    @BindExtra String extra;", //
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
                    "  public Test__IntentBuilder.AllSet extra(String extra) {", //
                    "    bundler.put(\"extra\", extra);", //
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
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Test__IntentBuilder")
        .hasSourceEquivalentTo(builderSource);
  }

  @Test
  public void
  intentBuilderGenerator_should_generateIntentBuilder_when_containsMandatoryAndOptionalExtras() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.BindExtra;", //
                    "import dart.DartModel;", //
                    "import java.lang.annotation.Retention;", //
                    "import java.lang.annotation.Target;", //
                    "import static java.lang.annotation.ElementType.FIELD;", //
                    "import static java.lang.annotation.RetentionPolicy.CLASS;", //
                    "@Retention(CLASS) @Target(FIELD) ", //
                    "@interface Nullable {}", //
                    "@DartModel(\"test.Test\")", //
                    "public class TestNavigationModel {", //
                    "    @BindExtra(\"key1\") String extra1;", //
                    "    @BindExtra(\"key2\") @Nullable String extra2;", //
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
                    "  public Test__IntentBuilder.AllSet key1(String extra1) {", //
                    "    bundler.put(\"key1\", extra1);", //
                    "    return new Test__IntentBuilder.AllSet();", //
                    "  }", //
                    "  public class AllSet {", //
                    "    public Test__IntentBuilder.AllSet key2(String extra2) {", //
                    "      bundler.put(\"key2\", extra2);", //
                    "      return this;", //
                    "    }", //
                    "    public Intent build() {", //
                    "      intent.putExtras(bundler.get());", //
                    "      return intent;", //
                    "    }", //
                    "  }", //
                    "}" //
                ));

    Compilation compilation =
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Test__IntentBuilder")
        .hasSourceEquivalentTo(builderSource);
  }

  @Test
  public void
  intentBuilderGenerator_should_generateIntentBuilder_when_containsOnlyOptionalExtras() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.BindExtra;", //
                    "import dart.DartModel;", //
                    "import java.lang.annotation.Retention;", //
                    "import java.lang.annotation.Target;", //
                    "import static java.lang.annotation.ElementType.FIELD;", //
                    "import static java.lang.annotation.RetentionPolicy.CLASS;", //
                    "@Retention(CLASS) @Target(FIELD) ", //
                    "@interface Nullable {}", //
                    "@DartModel(\"test.Test\")", //
                    "public class TestNavigationModel {", //
                    "    @BindExtra(\"key1\") @Nullable String extra1;", //
                    "    @BindExtra(\"key2\") @Nullable String extra2;", //
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
                    "  public Test__IntentBuilder key1(String extra1) {", //
                    "    bundler.put(\"key1\", extra1);", //
                    "    return this;", //
                    "  }", //
                    "  public Test__IntentBuilder key2(String extra2) {", //
                    "    bundler.put(\"key2\", extra2);", //
                    "    return this;", //
                    "  }", //
                    "  public Intent build() {", //
                    "    intent.putExtras(bundler.get());", //
                    "    return intent;", //
                    "  }", //
                    "}" //
                ));

    Compilation compilation =
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .generatedSourceFile("test.navigation.Test__IntentBuilder")
        .hasSourceEquivalentTo(builderSource);
  }

  @Test
  public void intentBuilderGenerator_should_fail_when_extraKeyIsInvalid() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.BindExtra;", //
                    "import dart.DartModel;", //
                    "@DartModel(\"test.Test\")", //
                    "public class TestNavigationModel {", //
                    "    @BindExtra(\"my.key\") String extra;", //
                    "}" //
                ));

    Compilation compilation =
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation).hadErrorContaining("Keys have to be valid java variable identifiers.");
  }

  @Test
  public void intentBuilderGenerator_should_fail_when_extraIsPrivate() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.BindExtra;", //
                    "import dart.DartModel;", //
                    "@DartModel(\"test.Test\")", //
                    "public class TestNavigationModel {", //
                    "    @BindExtra(\"key\") private String extra;", //
                    "}" //
                ));

    Compilation compilation =
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation).hadErrorContaining("@BindExtra fields must not be private or static.");
  }

  @Test
  public void intentBuilderGenerator_should_fail_when_extraIsStatic() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.BindExtra;", //
                    "import dart.DartModel;", //
                    "@DartModel(\"test.Test\")", //
                    "public class TestNavigationModel {", //
                    "    @BindExtra(\"key\") static String extra;", //
                    "}" //
                ));

    Compilation compilation =
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation).hadErrorContaining("@BindExtra fields must not be private or static.");
  }

  @Test
  public void intentBuilderGenerator_should_fail_when_extraIsInvalidType() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.navigation.TestNavigationModel",
            Joiner.on('\n')
                .join( //
                    "package test.navigation;", //
                    "import dart.BindExtra;", //
                    "import dart.DartModel;", //
                    "@DartModel(\"test.Test\")", //
                    "public class TestNavigationModel {", //
                    "    @BindExtra(\"key\") Object extra;", //
                    "}" //
                ));

    Compilation compilation =
        javac().withProcessors(ProcessorTestUtilities.hensonProcessors()).compile(source);
    assertThat(compilation)
        .hadErrorContaining("@BindExtra field must be a primitive or Serializable or Parcelable");
  }
}
