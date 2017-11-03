package dart.henson.processor;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static javax.tools.StandardLocation.SOURCE_OUTPUT;

public class IntentBuilderGeneratorTest {

  @Test
  public void intentBuilderGenerator_should_generateIntentBuilder_when_navigationModelIsDefined_and_containsExtras() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "@NavigationModel(\"test.Test\")", //
            "public class TestNavigationModel {", //
            "    @InjectExtra(\"key\") String extra;", //
            "}" //
        ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test.navmodel.Test$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navmodel;", //
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
                "  public Test$$IntentBuilder.AllSet key(String extra) {", //
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
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }

  @Test
  public void intentBuilderGenerator_should_generateIntentBuilder_when_navigationModelIsDefined_and_doesNotContainExtras() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "@NavigationModel(\"test.Test\")", //
            "public class TestNavigationModel {", //
            "}" //
        ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test.navmodel.Test$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navmodel;", //
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
                "  public Intent build() {", //
                "    intent.putExtras(bundler.get());", //
                "    return intent;", //
                "  }", //
                "}" //
            ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }

  @Test(expected = AssertionError.class)
  public void intentBuilderGenerator_should_notGenerateIntentBuilder_when_navigationModelIsNotDefined_and_containsExtras() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "public class TestNavigationModel {", //
            "    @InjectExtra(\"key\") String extra;", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesFileNamed(SOURCE_OUTPUT, "test.navmodel", "Test$$IntentBuilder.java");
  }

  @Test
  public void intentBuilderGenerator_should_generateIntentBuilder_when_navigationModelIsDefined_and_targetClassIsInner() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "@NavigationModel(\"test.Test$MyInnerTest\")", //
            "public class TestNavigationModel {", //
            "}" //
        ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test.navmodel.Test$MyInnerTest$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navmodel;", //
                "import android.content.Context;", //
                "import android.content.Intent;", //
                "import dart.henson.Bundler;", //
                "import java.lang.Class;", //
                "import java.lang.Exception;", //
                "import java.lang.String;", //
                "public class Test$MyInnerTest$$IntentBuilder {", //
                "  private Intent intent;", //
                "  private Bundler bundler = Bundler.create();", //
                "  public Test$MyInnerTest$$IntentBuilder(Context context) {", //
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

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }

  @Test
  public void intentBuilderGenerator_should_generateIntentBuilders_when_navigationModelIsDefined_and_containsExtras_and_sameForParent() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel1",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "@NavigationModel(\"test.Test1\")", //
            "public class TestNavigationModel1 extends TestNavigationModel2 {", //
            "    @InjectExtra(\"key1\") String extra1;", //
            "}", //
            "@NavigationModel(\"test.Test2\")", //
            "class TestNavigationModel2 {", //
            "    @InjectExtra(\"key2\") String extra2;", //
            "}" //
        ));

    JavaFileObject builderSource1 =
        JavaFileObjects.forSourceString("test.navmodel.Test1$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navmodel;", //
                "import android.content.Context;", //
                "import android.content.Intent;", //
                "import dart.henson.Bundler;", //
                "import java.lang.Class;", //
                "import java.lang.Exception;", //
                "import java.lang.String;", //
                "public class Test1$$IntentBuilder {", //
                "  private Intent intent;", //
                "  private Bundler bundler = Bundler.create();", //
                "  public Test1$$IntentBuilder(Context context) {", //
                "    intent = new Intent(context, getClassDynamically(\"test.Test1\"));", //
                "  }", //
                "  public Class getClassDynamically(String className) {", //
                "    try {", //
                "      return Class.forName(className);", //
                "    } catch(Exception ex) {", //
                "      throw new RuntimeException(ex);", //
                "    }", //
                "  }", //
                "  public Test1$$IntentBuilder.AfterSettingKey1 key1(String extra1) {", //
                "    bundler.put(\"key1\", extra1);", //
                "    return new Test1$$IntentBuilder.AfterSettingKey1();", //
                "  }", //
                "  public class AfterSettingKey1 {", //
                "    public Test1$$IntentBuilder.AllSet key2(String extra2) {", //
                "      bundler.put(\"key2\", extra2);", //
                "      return new Test1$$IntentBuilder.AllSet();", //
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
        JavaFileObjects.forSourceString("test.navmodel.Test2$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navmodel;", //
                "import android.content.Context;", //
                "import android.content.Intent;", //
                "import dart.henson.Bundler;", //
                "import java.lang.Class;", //
                "import java.lang.Exception;", //
                "import java.lang.String;", //
                "public class Test2$$IntentBuilder {", //
                "  private Intent intent;", //
                "  private Bundler bundler = Bundler.create();", //
                "  public Test2$$IntentBuilder(Context context) {", //
                "    intent = new Intent(context, getClassDynamically(\"test.Test2\"));", //
                "  }", //
                "  public Class getClassDynamically(String className) {", //
                "    try {", //
                "      return Class.forName(className);", //
                "    } catch(Exception ex) {", //
                "      throw new RuntimeException(ex);", //
                "    }", //
                "  }", //
                "  public Test2$$IntentBuilder.AllSet key2(String extra2) {", //
                "    bundler.put(\"key2\", extra2);", //
                "    return new Test2$$IntentBuilder.AllSet();", //
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
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource1, builderSource2);
  }

  @Test
  public void intentBuilderGenerator_should_generateIntentBuilders_when_navigationModelIsDefined_and_containsExtras_and_sameForParent_and_keyIsRepeated() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel1",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "@NavigationModel(\"test.Test1\")", //
            "public class TestNavigationModel1 extends TestNavigationModel2 {", //
            "    @InjectExtra(\"key\") String extra1;", //
            "}", //
            "@NavigationModel(\"test.Test2\")", //
            "class TestNavigationModel2 {", //
            "    @InjectExtra(\"key\") Integer extra2;", //
            "}" //
        ));

    JavaFileObject builderSource1 =
        JavaFileObjects.forSourceString("test.navmodel.Test1$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navmodel;", //
                "import android.content.Context;", //
                "import android.content.Intent;", //
                "import dart.henson.Bundler;", //
                "import java.lang.Class;", //
                "import java.lang.Exception;", //
                "import java.lang.Integer;", //
                "import java.lang.String;", //
                "public class Test1$$IntentBuilder {", //
                "  private Intent intent;", //
                "  private Bundler bundler = Bundler.create();", //
                "  public Test1$$IntentBuilder(Context context) {", //
                "    intent = new Intent(context, getClassDynamically(\"test.Test1\"));", //
                "  }", //
                "  public Class getClassDynamically(String className) {", //
                "    try {", //
                "      return Class.forName(className);", //
                "    } catch(Exception ex) {", //
                "      throw new RuntimeException(ex);", //
                "    }", //
                "  }", //
                "  public Test1$$IntentBuilder.AllSet key(Integer extra2) {", //
                "    bundler.put(\"key\", extra2);", //
                "    return new Test1$$IntentBuilder.AllSet();", //
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
        JavaFileObjects.forSourceString("test.navmodel.Test2$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navmodel;", //
                "import android.content.Context;", //
                "import android.content.Intent;", //
                "import dart.henson.Bundler;", //
                "import java.lang.Class;", //
                "import java.lang.Exception;", //
                "import java.lang.Integer;", //
                "import java.lang.String;", //
                "public class Test2$$IntentBuilder {", //
                "  private Intent intent;", //
                "  private Bundler bundler = Bundler.create();", //
                "  public Test2$$IntentBuilder(Context context) {", //
                "    intent = new Intent(context, getClassDynamically(\"test.Test2\"));", //
                "  }", //
                "  public Class getClassDynamically(String className) {", //
                "    try {", //
                "      return Class.forName(className);", //
                "    } catch(Exception ex) {", //
                "      throw new RuntimeException(ex);", //
                "    }", //
                "  }", //
                "  public Test2$$IntentBuilder.AllSet key(Integer extra2) {", //
                "    bundler.put(\"key\", extra2);", //
                "    return new Test2$$IntentBuilder.AllSet();", //
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
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource1, builderSource2);
  }

  @Test
  public void intentBuilderGenerator_should_generateIntentBuilderWithParentExtras_when_navigationModelIsDefined_and_doesNotContainExtras() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel1",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "@NavigationModel(\"test.Test1\")", //
            "public class TestNavigationModel1 extends TestNavigationModel2 {", //
            "}", //
            "class TestNavigationModel2 {", //
            "    @InjectExtra(\"key2\") String extra2;", //
            "}" //
        ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test.navmodel.Test1$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navmodel;", //
                "import android.content.Context;", //
                "import android.content.Intent;", //
                "import dart.henson.Bundler;", //
                "import java.lang.Class;", //
                "import java.lang.Exception;", //
                "import java.lang.String;", //
                "public class Test1$$IntentBuilder {", //
                "  private Intent intent;", //
                "  private Bundler bundler = Bundler.create();", //
                "  public Test1$$IntentBuilder(Context context) {", //
                "    intent = new Intent(context, getClassDynamically(\"test.Test1\"));", //
                "  }", //
                "  public Class getClassDynamically(String className) {", //
                "    try {", //
                "      return Class.forName(className);", //
                "    } catch(Exception ex) {", //
                "      throw new RuntimeException(ex);", //
                "    }", //
                "  }", //
                "  public Test1$$IntentBuilder.AllSet key2(String extra2) {", //
                "    bundler.put(\"key2\", extra2);", //
                "    return new Test1$$IntentBuilder.AllSet();", //
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
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }

  //Test for https://github.com/f2prateek/dart/issues/64
  @Test public void intentBuilderGenerator_should_generateIntentBuilderWithAncestorExtras() {
    JavaFileObject source =
        JavaFileObjects.forSourceString("test.navmodel.Test", Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "public abstract class Test {", //
            "    @InjectExtra(\"key\") String extra;", //
            "}", //
            "@NavigationModel(\"test.Test2\")", //
            "class TestAwo extends TestOne {", //
            "}", //
            "@NavigationModel(\"test.Test1\")", //
            "class TestOne extends Test {", //
            "}" //
        ));

    JavaFileObject expectedSource =
        JavaFileObjects.forSourceString("test.navmodel.Test2$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navmodel;", //
                "import android.content.Context;", //
                "import android.content.Intent;", //
                "import dart.henson.Bundler;", //
                "import java.lang.Class;", //
                "import java.lang.Exception;", //
                "import java.lang.String;", //
                "public class Test2$$IntentBuilder {", //
                "  private Intent intent;", //
                "  private Bundler bundler = Bundler.create();", //
                "  public Test2$$IntentBuilder(Context context) {", //
                "    intent = new Intent(context, getClassDynamically(\"test.Test2\"));", //
                "  }", //
                "  public Class getClassDynamically(String className) {", //
                "    try {", //
                "      return Class.forName(className);", //
                "    } catch(Exception ex) {", //
                "      throw new RuntimeException(ex);", //
                "    }", //
                "  }", //
                "  public Test2$$IntentBuilder.AllSet key(String extra) {", //
                "    bundler.put(\"key\", extra);", //
                "    return new Test2$$IntentBuilder.AllSet();", //
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
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test
  public void intentBuilderGenerator_should_generateIntentBuilderWithParentExtras_when_navigationModelIsDefined_and_doesNotContainExtras_and_parentIsAbstract() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel1",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
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
        JavaFileObjects.forSourceString("test.navmodel.Test1$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navmodel;", //
                "import android.content.Context;", //
                "import android.content.Intent;", //
                "import dart.henson.Bundler;", //
                "import java.lang.Class;", //
                "import java.lang.Exception;", //
                "import java.lang.String;", //
                "public class Test1$$IntentBuilder {", //
                "  private Intent intent;", //
                "  private Bundler bundler = Bundler.create();", //
                "  public Test1$$IntentBuilder(Context context) {", //
                "    intent = new Intent(context, getClassDynamically(\"test.Test1\"));", //
                "  }", //
                "  public Class getClassDynamically(String className) {", //
                "    try {", //
                "      return Class.forName(className);", //
                "    } catch(Exception ex) {", //
                "      throw new RuntimeException(ex);", //
                "    }", //
                "  }", //
                "  public Test1$$IntentBuilder.AllSet key2(String extra2) {", //
                "    bundler.put(\"key2\", extra2);", //
                "    return new Test1$$IntentBuilder.AllSet();", //
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
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }

  @Test
  public void intentBuilderGenerator_should_generateIntentBuilder_when_navigationModelIsDefined_and_parentContainsGenerics() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel1",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "@NavigationModel(\"test.Test1\")", //
            "class TestNavigationModel1 extends TestNavigationModel2<String> {", //
            "    @InjectExtra(\"key1\") String extra1;", //
            "}", //
            "class TestNavigationModel2<T> {", //
            "    @InjectExtra(\"key2\") String extra2;", //
            "}" //
        ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test.navmodel.Test1$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navmodel;", //
                "import android.content.Context;", //
                "import android.content.Intent;", //
                "import dart.henson.Bundler;", //
                "import java.lang.Class;", //
                "import java.lang.Exception;", //
                "import java.lang.String;", //
                "public class Test1$$IntentBuilder {", //
                "  private Intent intent;", //
                "  private Bundler bundler = Bundler.create();", //
                "  public Test1$$IntentBuilder(Context context) {", //
                "    intent = new Intent(context, getClassDynamically(\"test.Test1\"));", //
                "  }", //
                "  public Class getClassDynamically(String className) {", //
                "    try {", //
                "      return Class.forName(className);", //
                "    } catch(Exception ex) {", //
                "      throw new RuntimeException(ex);", //
                "    }", //
                "  }", //
                "  public Test1$$IntentBuilder.AfterSettingKey1 key1(String extra1) {", //
                "    bundler.put(\"key1\", extra1);", //
                "    return new Test1$$IntentBuilder.AfterSettingKey1();", //
                "  }", //
                "  public class AfterSettingKey1 {", //
                "    public Test1$$IntentBuilder.AllSet key2(String extra2) {", //
                "      bundler.put(\"key2\", extra2);", //
                "      return new Test1$$IntentBuilder.AllSet();", //
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

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }

  @Test
  public void intentBuilderGenerator_should_generateIntentBuilder_when_navigationModelIsDefined_and_containsPrimitiveExtras() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "@NavigationModel(\"test.Test\")", //
            "public class TestNavigationModel {", //
            "    @InjectExtra(\"key_bool\") boolean aBool;", //
            "    @InjectExtra(\"key_byte\") byte aByte;", //
            "    @InjectExtra(\"key_short\") short aShort;", //
            "    @InjectExtra(\"key_int\") int anInt;", //
            "    @InjectExtra(\"key_long\") long aLong;", //
            "    @InjectExtra(\"key_char\") char aChar;", //
            "    @InjectExtra(\"key_float\") float aFloat;", //
            "    @InjectExtra(\"key_double\") double aDouble;", //
            "}" //
        ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test.navmodel.Test$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navmodel;", //
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
                "  public Test$$IntentBuilder.AfterSettingKey_bool key_bool(boolean aBool) {", //
                "    bundler.put(\"key_bool\",aBool);", //
                "    return new Test$$IntentBuilder.AfterSettingKey_bool();", //
                "  }", //
                "  public class AfterSettingKey_bool {", //
                "    public Test$$IntentBuilder.AfterSettingKey_byte key_byte(byte aByte) {", //
                "      bundler.put(\"key_byte\",aByte);", //
                "      return new Test$$IntentBuilder.AfterSettingKey_byte();", //
                "    }", //
                "  }", //
                "  public class AfterSettingKey_byte {", //
                "    public Test$$IntentBuilder.AfterSettingKey_char key_char(char aChar) {", //
                "      bundler.put(\"key_char\",aChar);", //
                "      return new Test$$IntentBuilder.AfterSettingKey_char();", //
                "    }", //
                "  }", //
                "  public class AfterSettingKey_char {", //
                "    public Test$$IntentBuilder.AfterSettingKey_double key_double(double aDouble) {",
                //
                "      bundler.put(\"key_double\",aDouble);", //
                "      return new Test$$IntentBuilder.AfterSettingKey_double();", //
                "    }", //
                "  }", //
                "  public class AfterSettingKey_double {", //
                "    public Test$$IntentBuilder.AfterSettingKey_float key_float(float aFloat) {", //
                "      bundler.put(\"key_float\",aFloat);", //
                "      return new Test$$IntentBuilder.AfterSettingKey_float();", //
                "    }", //
                "  }", //
                "  public class AfterSettingKey_float {", //
                "    public Test$$IntentBuilder.AfterSettingKey_int key_int(int anInt) {", //
                "      bundler.put(\"key_int\",anInt);", //
                "      return new Test$$IntentBuilder.AfterSettingKey_int();", //
                "    }", //
                "  }", //
                "  public class AfterSettingKey_int {", //
                "    public Test$$IntentBuilder.AfterSettingKey_long key_long(long aLong) {", //
                "      bundler.put(\"key_long\",aLong);", //
                "      return new Test$$IntentBuilder.AfterSettingKey_long();", //
                "    }", //
                "  }", //
                "  public class AfterSettingKey_long {", //
                "    public Test$$IntentBuilder.AllSet key_short(short aShort) {", //
                "      bundler.put(\"key_short\",aShort);", //
                "      return new Test$$IntentBuilder.AllSet();", //
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

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }

  @Test
  public void intentBuilderGenerator_should_generateIntentBuilder_when_navigationModelIsDefined_and_containsSerializableAndParcelableExtra() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import android.os.Parcelable;", //
            "import java.io.Serializable;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "class Extra implements Serializable, Parcelable {", //
            "  public void writeToParcel(android.os.Parcel out, int flags) {", //
            "  }", //
            "  public int describeContents() {", //
            "    return 0;", //
            "  }", //
            "}", //
            "@NavigationModel(\"test.Test\")", //
            "public class TestNavigationModel {", //
            "    @InjectExtra(\"key\") Extra extra;", //
            "}" //
        ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test.navmodel.Test$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navmodel;", //
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
                "  public Test$$IntentBuilder.AllSet key(Extra extra) {", //
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
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }

  @Test public void intentBuilderGenerator_should_containOnlyOneSetter_when_keysAreRepeated() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "@NavigationModel(\"test.Test\")", //
            "public class TestNavigationModel {", //
            "    @InjectExtra(\"key\") String extra1;", //
            "    @InjectExtra(\"key\") String extra2;", //
            "    @InjectExtra(\"key\") String extra3;", //
            "}" //
        ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test.navmodel.Test$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navmodel;", //
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
                "  public Test$$IntentBuilder.AllSet key(String extra1) {", //
                "    bundler.put(\"key\", extra1);", //
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
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }

  @Test public void intentBuilderGenerator_should_useDefaultKey_when_noKeyIsProvided() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "@NavigationModel(\"test.Test\")", //
            "public class TestNavigationModel {", //
            "    @InjectExtra String extra;", //
            "}" //
        ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test.navmodel.Test$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navmodel;", //
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
                "  public Test$$IntentBuilder.AllSet extra(String extra) {", //
                "    bundler.put(\"extra\", extra);", //
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
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }

  @Test
  public void intentBuilderGenerator_should_generateIntentBuilder_when_containsMandatoryAndOptionalExtras() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "import java.lang.annotation.Retention;", //
            "import java.lang.annotation.Target;", //
            "import static java.lang.annotation.ElementType.FIELD;", //
            "import static java.lang.annotation.RetentionPolicy.CLASS;", //
            "@Retention(CLASS) @Target(FIELD) ", //
            "@interface Nullable {}", //
            "@NavigationModel(\"test.Test\")", //
            "public class TestNavigationModel {", //
            "    @InjectExtra(\"key1\") String extra1;", //
            "    @InjectExtra(\"key2\") @Nullable String extra2;", //
            "}" //
        ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test.navmodel.Test$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navmodel;", //
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
                "  public Test$$IntentBuilder.AllSet key1(String extra1) {", //
                "    bundler.put(\"key1\", extra1);", //
                "    return new Test$$IntentBuilder.AllSet();", //
                "  }", //
                "  public class AllSet {", //
                "    public Test$$IntentBuilder.AllSet key2(String extra2) {", //
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

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }

  @Test
  public void intentBuilderGenerator_should_generateIntentBuilder_when_containsOnlyOptionalExtras() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "import java.lang.annotation.Retention;", //
            "import java.lang.annotation.Target;", //
            "import static java.lang.annotation.ElementType.FIELD;", //
            "import static java.lang.annotation.RetentionPolicy.CLASS;", //
            "@Retention(CLASS) @Target(FIELD) ", //
            "@interface Nullable {}", //
            "@NavigationModel(\"test.Test\")", //
            "public class TestNavigationModel {", //
            "    @InjectExtra(\"key1\") @Nullable String extra1;", //
            "    @InjectExtra(\"key2\") @Nullable String extra2;", //
            "}" //
        ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test.navmodel.Test$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test.navmodel;", //
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
                "  public Test$$IntentBuilder key1(String extra1) {", //
                "    bundler.put(\"key1\", extra1);", //
                "    return this;", //
                "  }", //
                "  public Test$$IntentBuilder key2(String extra2) {", //
                "    bundler.put(\"key2\", extra2);", //
                "    return this;", //
                "  }", //
                "  public Intent build() {", //
                "    intent.putExtras(bundler.get());", //
                "    return intent;", //
                "  }", //
                "}" //
            ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }

  @Test public void intentBuilderGenerator_should_fail_when_extraKeyIsInvalid() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "@NavigationModel(\"test.Test\")", //
            "public class TestNavigationModel {", //
            "    @InjectExtra(\"my.key\") String extra;", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .failsToCompile()
        .withErrorContaining("Keys have to be valid java variable identifiers.");
  }

  @Test public void intentBuilderGenerator_should_fail_when_extraIsPrivate() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "@NavigationModel(\"test.Test\")", //
            "public class TestNavigationModel {", //
            "    @InjectExtra(\"key\") private String extra;", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .failsToCompile()
        .withErrorContaining("@InjectExtra fields must not be private or static.");
  }

  @Test public void intentBuilderGenerator_should_fail_when_extraIsStatic() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "@NavigationModel(\"test.Test\")", //
            "public class TestNavigationModel {", //
            "    @InjectExtra(\"key\") static String extra;", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .failsToCompile()
        .withErrorContaining("@InjectExtra fields must not be private or static.");
  }

  @Test public void intentBuilderGenerator_should_fail_when_extraIsInvalidType() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "@NavigationModel(\"test.Test\")", //
            "public class TestNavigationModel {", //
            "    @InjectExtra(\"key\") Object extra;", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .failsToCompile()
        .withErrorContaining(
            "@InjectExtra field must be a primitive or Serializable or Parcelable");
  }
}
