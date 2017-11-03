package dart.henson.processor;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static javax.tools.StandardLocation.SOURCE_OUTPUT;

public class HensonNavigatorGeneratorTest {

  @Test
  public void hensonNavigatorGenerator_should_generateHensonClass_when_navigationModelIsDefined_and_containsExtras() {
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
        JavaFileObjects.forSourceString("test.navmodel.Henson", Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import android.content.Context;", //
            "public class Henson {", //
            "  private Henson() {", //
            "  }", //
            "  public static WithContextSetState with(Context context) {", //
            "    return new test.navmodel.Henson.WithContextSetState(context);", //
            "  }", //
            "  public static class WithContextSetState {", //
            "    private Context context;", //
            "    private WithContextSetState(Context context) {", //
            "      this.context = context;", //
            "    }", //
            "    public Test$$IntentBuilder gotoTest() {", //
            "      return new test.navmodel.Test$$IntentBuilder(context);", //
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
  public void hensonNavigatorGenerator_should_generateHensonClass_when_navigationModelIsDefined_and_DoesNotContainExtras() {
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
        JavaFileObjects.forSourceString("test.navmodel.Henson", Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import android.content.Context;", //
            "public class Henson {", //
            "  private Henson() {", //
            "  }", //
            "  public static WithContextSetState with(Context context) {", //
            "    return new test.navmodel.Henson.WithContextSetState(context);", //
            "  }", //
            "  public static class WithContextSetState {", //
            "    private Context context;", //
            "    private WithContextSetState(Context context) {", //
            "      this.context = context;", //
            "    }", //
            "    public Test$$IntentBuilder gotoTest() {", //
            "      return new test.navmodel.Test$$IntentBuilder(context);", //
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
  public void hensonNavigatorGenerator_should_generateHensonClass_when_navigationModelIsDefined_and_targetClassIsInner() {
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
        JavaFileObjects.forSourceString("test.navmodel.Henson", Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import android.content.Context;", //
            "public class Henson {", //
            "  private Henson() {", //
            "  }", //
            "  public static WithContextSetState with(Context context) {", //
            "    return new test.navmodel.Henson.WithContextSetState(context);", //
            "  }", //
            "  public static class WithContextSetState {", //
            "    private Context context;", //
            "    private WithContextSetState(Context context) {", //
            "      this.context = context;", //
            "    }", //
            "    public Test$MyInnerTest$$IntentBuilder gotoTest$MyInnerTest() {", //
            "      return new test.navmodel.Test$MyInnerTest$$IntentBuilder(context);", //
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

  @Test(expected = AssertionError.class)
  public void hensonNavigatorGenerator_should_notGenerateHensonClass_when_navigationModelIsNotDefined_and_containExtras() {
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
        .generatesFileNamed(SOURCE_OUTPUT, "test.navmodel", "Henson.java");
  }

  @Test
  public void hensonNavigatorGenerator_should_generateHensonClass_when_navigationModelIsDefinedForMultipleClasses() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel1",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
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
        JavaFileObjects.forSourceString("test.navmodel.Henson", Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import android.content.Context;", //
            "public class Henson {", //
            "  private Henson() {", //
            "  }", //
            "  public static WithContextSetState with(Context context) {", //
            "    return new test.navmodel.Henson.WithContextSetState(context);", //
            "  }", //
            "  public static class WithContextSetState {", //
            "    private Context context;", //
            "    private WithContextSetState(Context context) {", //
            "      this.context = context;", //
            "    }", //
            "    public Test1$$IntentBuilder gotoTest1() {", //
            "      return new test.navmodel.Test1$$IntentBuilder(context);", //
            "    }", //
            "    public Test2$$IntentBuilder gotoTest2() {", //
            "      return new test.navmodel.Test2$$IntentBuilder(context);", //
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
  public void hensonNavigatorGenerator_should_generateHensonClass_when_navigationModelIsDefined_and_usingGenerics() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel1",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
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
        JavaFileObjects.forSourceString("test.navmodel.Henson", Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import android.content.Context;", //
            "public class Henson {", //
            "  private Henson() {", //
            "  }", //
            "  public static WithContextSetState with(Context context) {", //
            "    return new test.navmodel.Henson.WithContextSetState(context);", //
            "  }", //
            "  public static class WithContextSetState {", //
            "    private Context context;", //
            "    private WithContextSetState(Context context) {", //
            "      this.context = context;", //
            "    }", //
            "    public Test1$$IntentBuilder gotoTest1() {", //
            "      return new test.navmodel.Test1$$IntentBuilder(context);", //
            "    }", //
            "    public Test2$$IntentBuilder gotoTest2() {", //
            "      return new test.navmodel.Test2$$IntentBuilder(context);", //
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
  public void hensonNavigatorGenerator_should_generateHensonClass_when_navigationModelIsDefined_and_superClassIsAbstract() {
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
        JavaFileObjects.forSourceString("test.navmodel.Henson", Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import android.content.Context;", //
            "public class Henson {", //
            "  private Henson() {", //
            "  }", //
            "  public static WithContextSetState with(Context context) {", //
            "    return new test.navmodel.Henson.WithContextSetState(context);", //
            "  }", //
            "  public static class WithContextSetState {", //
            "    private Context context;", //
            "    private WithContextSetState(Context context) {", //
            "      this.context = context;", //
            "    }", //
            "    public Test1$$IntentBuilder gotoTest1() {", //
            "      return new test.navmodel.Test1$$IntentBuilder(context);", //
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
  public void hensonNavigatorGenerator_should_fail_when_navigationModelAnnotatedClassIsAbstract() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel1",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "@NavigationModel(\"test.Test1\")", //
            "public class TestNavigationModel1 {", //
            "}", //
            "@NavigationModel(\"test.Test2\")", //
            "abstract class TestNavigationModel2 {", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .failsToCompile()
        .withErrorContaining(
            "@NavigationModel class TestNavigationModel2 must not be private, static or abstract.");
  }

  @Test
  public void hensonNavigatorGenerator_should_fail_when_navigationModelAnnotatedClassIsInner() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel1",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "@NavigationModel(\"test.Test1\")", //
            "public class TestNavigationModel1 {", //
            "  @NavigationModel(\"test.Test2\")", //
            "  class TestNavigationModel2 {", //
            "  }", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .failsToCompile()
        .withErrorContaining(
            "@NavigationModel class TestNavigationModel2 must be a top level class.");
  }

  @Test
  public void hensonNavigatorGenerator_should_fail_when_navigationModelAnnotatedClassIsInnerStatic() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel1",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "@NavigationModel(\"test.Test1\")", //
            "public class TestNavigationModel1 {", //
            "  @NavigationModel(\"test.Test2\")", //
            "  static class TestNavigationModel2 {", //
            "  }", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .failsToCompile()
        .withErrorContaining(
            "@NavigationModel class TestNavigationModel2 must not be private, static or abstract.");
  }

  @Test
  public void hensonNavigatorGenerator_should_fail_when_navigationModelAnnotatedClassIsPrivate() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.navmodel.TestNavigationModel1",
        Joiner.on('\n').join( //
            "package test.navmodel;", //
            "import dart.InjectExtra;", //
            "import dart.NavigationModel;", //
            "@NavigationModel(\"test.Test1\")", //
            "public class TestNavigationModel1 {", //
            "  @NavigationModel(\"test.Test2\")", //
            "  private class TestNavigationModel2 {", //
            "  }", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .failsToCompile()
        .withErrorContaining(
            "@NavigationModel class TestNavigationModel2 must not be private, static or abstract.");
  }
}
