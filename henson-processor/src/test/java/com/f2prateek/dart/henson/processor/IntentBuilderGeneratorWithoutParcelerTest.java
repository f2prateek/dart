package com.f2prateek.dart.henson.processor;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

/**
 * For tests related to Parceler, but Parceler is not available.
 */
public class IntentBuilderGeneratorWithoutParcelerTest {

  @Test
  public void intentBuilderGenerator_should_generateCode_when_extraIsSerializableCollection() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import java.util.ArrayList;", //
            "import com.f2prateek.dart.InjectExtra;", //
            "import com.f2prateek.dart.NavigationModel;", //
            "@NavigationModel(\"test.Test\")", //
            "public class TestNavigationModel {", //
            "    @InjectExtra(\"key\") ArrayList<String> extra;", //
            "}" //
        ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test.navmodel.Test$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navmodel;", //
                "import android.content.Context;", //
                "import android.content.Intent;", //
                "import com.f2prateek.dart.henson.Bundler;", //
                "import java.lang.Class;", //
                "import java.lang.Exception;", //
                "import java.lang.String;", //
                "import java.util.ArrayList;", //
                "public class Test$$IntentBuilder {", //
                "  private Intent intent;", //
                "  private Bundler bundler = Bundler.create();", //
                "  public Test$$IntentBuilder(Context context) {", //
                "    intent = new Intent(context, getClassDynamically(\"test.Test\"));", //
                "  }", //
                "  public Class getClassDynamically(String className) {", //
                "    try {", //
                "      return Class.forName(className);", //
                "    } catch(Exception ex) {", //
                "      throw new RuntimeException(ex);", //
                "    }", //
                "  }", //
                "  public Test$$IntentBuilder.AllSet key(ArrayList<String> extra) {", //
                "    bundler.put(\"key\", extra);", //
                "    return new Test$$IntentBuilder.AllSet();", //
                "  }", //
                "  public class AllSet {", //
                "    public Intent build() {", //
                "      intent.putExtras(bundler.get());", //
                "      return intent;", //
                "    }", //
                "  }", //
                "}" //
            ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessorWithoutParceler())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }

  @Test public void intentBuilderGenerator_should_fail_when_extraIsNonSerializableCollection() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import java.util.List;", //
            "import com.f2prateek.dart.InjectExtra;", //
            "import com.f2prateek.dart.NavigationModel;", //
            "@NavigationModel(\"test.Test\")", //
            "public class TestNavigationModel {", //
            "    @InjectExtra(\"key\") List<String> extra;", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessorWithoutParceler())
        .failsToCompile()
        .withErrorContaining(
            "@InjectExtra field must be a primitive or Serializable or Parcelable");
  }

  @Test
  public void intentBuilderGenerator_should_fail_when_extraIsAnnotatedWithParceler_and_parcelerIsOff() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import com.f2prateek.dart.InjectExtra;", //
            "import com.f2prateek.dart.NavigationModel;", //
            "import org.parceler.Parcel;", //
            "@NavigationModel(\"test.Test\")", //
            "public class TestNavigationModel {", //
            "    @InjectExtra(\"key\") Foo extra;", //
            "    @Parcel static class Foo {}", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessorWithoutParceler())
        .failsToCompile()
        .withErrorContaining(
            "@InjectExtra field must be a primitive or Serializable or Parcelable");
  }

  @Test
  public void intentBuilderGenerator_should_fail_when_extraIsCollectionOfElementAnnotatedWithParceler_and_parcelerIsOff() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import java.util.List;", //
            "import com.f2prateek.dart.InjectExtra;", //
            "import com.f2prateek.dart.NavigationModel;", //
            "@NavigationModel(\"test.Test\")", //
            "public class TestNavigationModel {", //
            "    @InjectExtra(\"key\") List<Foo> extra;", //
            "    @Parcel static class Foo {}", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessorWithoutParceler())
        .failsToCompile()
        .withErrorContaining(
            "@InjectExtra field must be a primitive or Serializable or Parcelable");
  }

  @Test public void intentBuilderGenerator_should_generateCode_when_extraIsParcelable() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import java.util.List;", //
            "import android.os.Parcelable;", //
            "import com.f2prateek.dart.InjectExtra;", //
            "import com.f2prateek.dart.NavigationModel;", //
            "@NavigationModel(\"test.Test\")", //
            "public class TestNavigationModel {", //
            "    @InjectExtra(\"key\") Foo extra;", //
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
        JavaFileObjects.forSourceString("test.navmodel.Test$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navmodel;", //
                "import android.content.Context;", //
                "import android.content.Intent;", //
                "import com.f2prateek.dart.henson.Bundler;", //
                "import java.lang.Class;", //
                "import java.lang.Exception;", //
                "import java.lang.String;", //
                "public class Test$$IntentBuilder {", //
                "  private Intent intent;", //
                "  private Bundler bundler = Bundler.create();", //
                "  public Test$$IntentBuilder(Context context) {", //
                "    intent = new Intent(context, getClassDynamically(\"test.Test\"));", //
                "  }", //
                "  public Class getClassDynamically(String className) {", //
                "    try {", //
                "      return Class.forName(className);", //
                "    } catch(Exception ex) {", //
                "      throw new RuntimeException(ex);", //
                "    }", //
                "  }", //
                "  public Test$$IntentBuilder.AllSet key(TestNavigationModel.Foo extra) {", //
                "    bundler.put(\"key\",(android.os.Parcelable) extra);", //
                "    return new Test$$IntentBuilder.AllSet();", //
                "  }", //
                "  public class AllSet {", //
                "    public Intent build() {", //
                "      intent.putExtras(bundler.get());", //
                "      return intent;", //
                "    }", //
                "  }", //
                "}" //
            ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessorWithoutParceler())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }

  @Test
  public void intentBuilderGenerator_should_generateCode_when_extraIsParcelableThatExtendsParcelable() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import java.util.List;", //
            "import android.os.Parcelable;", //
            "import com.f2prateek.dart.InjectExtra;", //
            "import com.f2prateek.dart.NavigationModel;", //
            "@NavigationModel(\"test.Test\")", //
            "public class TestNavigationModel {", //
            "    @InjectExtra(\"key\") Foo extra;", //
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
        JavaFileObjects.forSourceString("test.navmodel.Test$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navmodel;", //
                "import android.content.Context;", //
                "import android.content.Intent;", //
                "import com.f2prateek.dart.henson.Bundler;", //
                "import java.lang.Class;", //
                "import java.lang.Exception;", //
                "import java.lang.String;", //
                "public class Test$$IntentBuilder {", //
                "  private Intent intent;", //
                "  private Bundler bundler = Bundler.create();", //
                "  public Test$$IntentBuilder(Context context) {", //
                "    intent = new Intent(context, getClassDynamically(\"test.Test\"));", //
                "  }", //
                "  public Class getClassDynamically(String className) {", //
                "    try {", //
                "      return Class.forName(className);", //
                "    } catch(Exception ex) {", //
                "      throw new RuntimeException(ex);", //
                "    }", //
                "  }", //
                "  public Test$$IntentBuilder.AllSet key(TestNavigationModel.Foo extra) {", //
                "    bundler.put(\"key\",(android.os.Parcelable) extra);", //
                "    return new Test$$IntentBuilder.AllSet();", //
                "  }", //
                "  public class AllSet {", //
                "    public Intent build() {", //
                "      intent.putExtras(bundler.get());", //
                "      return intent;", //
                "    }", //
                "  }", //
                "}" //
            ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessorWithoutParceler())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }
}
