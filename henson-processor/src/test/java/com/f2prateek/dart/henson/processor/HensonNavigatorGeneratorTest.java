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

package com.f2prateek.dart.henson.processor;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.annotation.processing.Processor;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.truth0.Truth.ASSERT;

public class HensonNavigatorGeneratorTest {

  @Test public void injectingExtra() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "public class Test extends Activity {", //
        "    @InjectExtra(\"key\") String extra;", //
        "}" //
    ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test/Henson", Joiner.on('\n').join( //
            "package test;", //
                "import android.content.Context;", //
                "public class Henson {", //
                "  private Henson() {", //
                "  }", //
                "  public static WithContextSetState with(Context context) {", //
                "    return new test.Henson.WithContextSetState(context);", //
                "  }", //
                "  public static class WithContextSetState {", //
                "    private Context context;", //
                "    private WithContextSetState(Context context) {", //
                "      this.context = context;", //
                "    }", //
                "    public Test$$IntentBuilder gotoTest() {", //
                "      return new test.Test$$IntentBuilder(context);", //
                "    }", //
                "  }", //
                "}" //
        ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }

  @Test public void henson() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.Henson;", //
        "@Henson public class Test extends Activity {", //
        "}" //
    ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test/Henson", Joiner.on('\n').join( //
            "package test;", //
                "import android.content.Context;", //
                "public class Henson {", //
                "  private Henson() {", //
                "  }", //
                "  public static WithContextSetState with(Context context) {", //
                "    return new test.Henson.WithContextSetState(context);", //
                "  }", //
                "  public static class WithContextSetState {", //
                "    private Context context;", //
                "    private WithContextSetState(Context context) {", //
                "      this.context = context;", //
                "    }", //
                "    public Test$$IntentBuilder gotoTest() {", //
                "      return new test.Test$$IntentBuilder(context);", //
                "    }", //
                "  }", //
                "}" //
        ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }

  @Test public void superclass() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "public class Test extends Activity {", //
        "    @InjectExtra(\"key\") String extra;", //
        "}", //
        "class TestOne extends Test {", //
        "    @InjectExtra(\"key\") String extra1;", //
        "}", //
        "class TestTwo extends Test {", //
        "}" //
    ));

    JavaFileObject expectedSource1 =
        JavaFileObjects.forSourceString("test/Henson", Joiner.on('\n').join( //
            "package test;", //
            "import android.content.Context;", //
            "public class Henson {", //
            "  private Henson() {", //
            "  }", //
            "  public static WithContextSetState with(Context context) {", //
            "    return new test.Henson.WithContextSetState(context);", //
            "  }", //
            "  public static class WithContextSetState {", //
            "    private Context context;", //
            "    private WithContextSetState(Context context) {", //
            "      this.context = context;", //
            "    }", //
            "    public TestOne$$IntentBuilder gotoTestOne() {", //
            "      return new test.TestOne$$IntentBuilder(context);", //
            "    }", //
            "    public Test$$IntentBuilder gotoTest() {", //
            "      return new test.Test$$IntentBuilder(context);", //
            "    }", //
            "  }", //
            "}" //
        ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource1);
  }

  @Test public void genericSuperclass() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "public class Test<T> extends Activity {", //
        "    @InjectExtra(\"key\") String extra;", //
        "}", //
        "class TestOne extends Test<String> {", //
        "    @InjectExtra(\"key\") String extra1;", //
        "}", //
        "class TestTwo extends Test<Object> {", //
        "}" //
    ));

    JavaFileObject expectedSource1 =
        JavaFileObjects.forSourceString("test/Henson", Joiner.on('\n').join( //
            "package test;", //
            "import android.content.Context;", //
            "public class Henson {", //
            "  private Henson() {", //
            "  }", //
            "  public static WithContextSetState with(Context context) {", //
            "    return new test.Henson.WithContextSetState(context);", //
            "  }", //
            "  public static class WithContextSetState {", //
            "    private Context context;", //
            "    private WithContextSetState(Context context) {", //
            "      this.context = context;", //
            "    }", //
            "    public TestOne$$IntentBuilder gotoTestOne() {", //
            "      return new test.TestOne$$IntentBuilder(context);", //
            "    }", //
            "    public Test$$IntentBuilder gotoTest() {", //
            "      return new test.Test$$IntentBuilder(context);", //
            "    }", //
            "  }", //
            "}" //
        ));

    ASSERT.about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource1);
  }
}
