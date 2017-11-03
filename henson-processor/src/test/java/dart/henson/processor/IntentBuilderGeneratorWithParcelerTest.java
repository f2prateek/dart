package dart.henson.processor;

import com.google.common.base.Joiner;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

/**
 * For tests related to Parceler, but Parceler is not available.
 */
public class IntentBuilderGeneratorWithParcelerTest {

  @Test
  public void intentBuilderGenerator_should_generateCode_when_extraIsSerializableCollection() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navigation.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navigation;", //
            "import java.util.ArrayList;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "@NavigationModel(\"test.Test\")", //
            "public class TestNavigationModel {", //
            "    @InjectExtra(\"key\") ArrayList<String> extra;", //
            "}" //
        ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test.navigation.Test$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navigation;", //
                "import android.content.Context;", //
                "import android.content.Intent;", //
                "import dart.henson.Bundler;", //
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
                "    bundler.put(\"key\", org.parceler.Parcels.wrap(extra));", //
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

      Compilation compilation = javac()
              .withProcessors(ProcessorTestUtilities.hensonProcessors())
              .compile(source);
      assertThat(compilation)
              .generatedSourceFile("test.navigation.Test$$IntentBuilder")
              .hasSourceEquivalentTo(builderSource);
  }

  @Test
  public void intentBuilderGenerator_should_generateCode_when_extraIsNonSerializableCollection() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navigation.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navigation;", //
            "import java.util.List;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "@NavigationModel(\"test.Test\")", //
            "public class TestNavigationModel {", //
            "    @InjectExtra(\"key\") List<String> extra;", //
            "}" //
        ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test.navigation.Test$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navigation;", //
                "import android.content.Context;", //
                "import android.content.Intent;", //
                "import dart.henson.Bundler;", //
                "import java.lang.Class;", //
                "import java.lang.Exception;", //
                "import java.lang.String;", //
                "import java.util.List;", //
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
                "  public Test$$IntentBuilder.AllSet key(List<String> extra) {", //
                "    bundler.put(\"key\", org.parceler.Parcels.wrap(extra));", //
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

      Compilation compilation = javac()
              .withProcessors(ProcessorTestUtilities.hensonProcessors())
              .compile(source);
      assertThat(compilation)
              .generatedSourceFile("test.navigation.Test$$IntentBuilder")
              .hasSourceEquivalentTo(builderSource);
  }

  @Test
  public void intentBuilderGenerator_should_generateCode_when_extraIsAnnotatedWithParceler_and_parcelerIsOn() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navigation.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navigation;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "import org.parceler.Parcel;", //
            "@NavigationModel(\"test.Test\")", //
            "public class TestNavigationModel {", //
            "    @InjectExtra(\"key\") Foo extra;", //
            "    @Parcel static class Foo {}", //
            "}" //
        ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test.navigation.Test$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navigation;", //
                "import android.content.Context;", //
                "import android.content.Intent;", //
                "import dart.henson.Bundler;", //
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
                "    bundler.put(\"key\", org.parceler.Parcels.wrap(extra));", //
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

      Compilation compilation = javac()
              .withProcessors(ProcessorTestUtilities.hensonProcessors())
              .compile(source);
      assertThat(compilation)
              .generatedSourceFile("test.navigation.Test$$IntentBuilder")
              .hasSourceEquivalentTo(builderSource);
  }

  @Test
  public void intentBuilderGenerator_should_generateCode_when_extraIsCollectionOfElementAnnotatedWithParceler_and_parcelerIsOn() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navigation.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navigation;", //
            "import java.util.List;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "import org.parceler.Parcel;", //
            "@NavigationModel(\"test.Test\")", //
            "public class TestNavigationModel {", //
            "    @InjectExtra(\"key\") List<Foo> extra;", //
            "    @Parcel static class Foo {}", //
            "}" //
        ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test.navigation.Test$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navigation;", //
                "import android.content.Context;", //
                "import android.content.Intent;", //
                "import dart.henson.Bundler;", //
                "import java.lang.Class;", //
                "import java.lang.Exception;", //
                "import java.lang.String;", //
                "import java.util.List;", //
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
                "  public Test$$IntentBuilder.AllSet key(List<TestNavigationModel.Foo> extra) {", //
                "    bundler.put(\"key\", org.parceler.Parcels.wrap(extra));", //
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
      Compilation compilation = javac()
              .withProcessors(ProcessorTestUtilities.hensonProcessors())
              .compile(source);
      assertThat(compilation)
              .generatedSourceFile("test.navigation.Test$$IntentBuilder")
              .hasSourceEquivalentTo(builderSource);
  }

  @Test
  public void intentBuilderGenerator_should_generateCode_when_multipleExtrasAreParceler_and_parcelerIsOn() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navigation.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navigation;", //
            "import java.util.List;", //
            "import java.util.Map;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "import org.parceler.Parcel;", //
            "import org.parceler.ParcelConstructor;", //
            "@Parcel", //
            "class ExampleParcel {", //
            "", //
            "  String name;", //
            "", //
            "  @ParcelConstructor", //
            "  public ExampleParcel(String name) {", //
            "    this.name = name;", //
            "  }", //
            "", //
            "  public String getName() {", //
            "    return name;", //
            "  }", //
            "}", //
            "@NavigationModel(\"test.Test\")", //
            "public class TestNavigationModel {", //
            "    @InjectExtra(\"key\") ExampleParcel extra;", //
            "    @InjectExtra(\"list\") List<ExampleParcel> listExtra;", //
            "    @InjectExtra(\"mapNestedExtra\") Map<List<String>, List<ExampleParcel>> mapNestedExtra;",
            //
            "}" //
        ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test.navigation.Test$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navigation;", //
                "import android.content.Context;", //
                "import android.content.Intent;", //
                "import dart.henson.Bundler;", //
                "import java.lang.Class;", //
                "import java.lang.Exception;", //
                "import java.lang.String;", //
                "import java.util.List;", //
                "import java.util.Map;", //
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
                "  public Test$$IntentBuilder.AfterSettingKey key(ExampleParcel extra) {", //
                "    bundler.put(\"key\", org.parceler.Parcels.wrap(extra));", //
                "    return new Test$$IntentBuilder.AfterSettingKey();", //
                "  }", //
                "  public class AfterSettingKey {", //
                "    public Test$$IntentBuilder.AfterSettingList list(List<ExampleParcel> listExtra) {",
                //
                "      bundler.put(\"list\", org.parceler.Parcels.wrap(listExtra));", //
                "      return new Test$$IntentBuilder.AfterSettingList();", //
                "    }", //
                "  }", //
                "  public class AfterSettingList {", //
                "    public Test$$IntentBuilder.AllSet mapNestedExtra(Map<List<String>, List<ExampleParcel>> mapNestedExtra) {",
                //
                "      bundler.put(\"mapNestedExtra\", org.parceler.Parcels.wrap(mapNestedExtra));",
                //
                "      return new Test$$IntentBuilder.AllSet();", //
                "    }", //
                "  }", //
                "  public class AllSet {", //
                "    public Intent build() {", //
                "      intent.putExtras(bundler.get());", //
                "      return intent;", //
                "    }", //
                "  }", //
                "}"));

      Compilation compilation = javac()
              .withProcessors(ProcessorTestUtilities.hensonProcessors())
              .compile(source);
      assertThat(compilation)
              .generatedSourceFile("test.navigation.Test$$IntentBuilder")
              .hasSourceEquivalentTo(builderSource);
  }

  @Test
  public void intentBuilderGenerator_should_generateCode_when_extraIsParcelableThatExtendsParcelable() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navigation.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navigation;", //
            "import java.util.List;", //
            "import android.os.Parcelable;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
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
        JavaFileObjects.forSourceString("test.navigation.Test$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navigation;", //
                "import android.content.Context;", //
                "import android.content.Intent;", //
                "import dart.henson.Bundler;", //
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

      Compilation compilation = javac()
              .withProcessors(ProcessorTestUtilities.hensonProcessors())
              .compile(source);
      assertThat(compilation)
              .generatedSourceFile("test.navigation.Test$$IntentBuilder")
              .hasSourceEquivalentTo(builderSource);
  }

  @Test
  public void intentBuilderGenerator_should_generateCode_when_extraIsParcelThatExtendsParcelable() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navigation.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navigation;", //
            "import java.util.List;", //
            "import android.os.Parcelable;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "import org.parceler.Parcel;", //
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
            "    @Parcel class Foo extends FooParent {} ", //
            "}" //
        ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test.navigation.Test$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navigation;", //
                "import android.content.Context;", //
                "import android.content.Intent;", //
                "import dart.henson.Bundler;", //
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
                "    bundler.put(\"key\", org.parceler.Parcels.wrap(extra);", //
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

      Compilation compilation = javac()
              .withProcessors(ProcessorTestUtilities.hensonProcessors())
              .compile(source);
      assertThat(compilation)
              .generatedSourceFile("test.navigation.Test$$IntentBuilder")
              .hasSourceEquivalentTo(builderSource);
  }
}
