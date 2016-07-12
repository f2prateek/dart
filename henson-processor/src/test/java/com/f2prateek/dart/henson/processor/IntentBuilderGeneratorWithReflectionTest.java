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
import com.google.testing.compile.CompileTester;
import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static com.google.common.truth.Truth.assert_;

/**
 * Tests {@link HensonExtraProcessor}.
 * For tests not related to Parceler.
 */
public class IntentBuilderGeneratorWithReflectionTest {

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
        JavaFileObjects.forSourceString("test/Test$$IntentBuilder", Joiner.on('\n').join( //
            "package test;", //
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
            "  public Test$$IntentBuilder.AllSet key(String extra) {", //
            "    bundler.put(\"key\",extra);", //
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
          .processedWith(ProcessorTestUtilities.hensonProcessorsWithReflection())
          .compilesWithoutError()
          .and()
          .generatesSources(builderSource);
  }
}
