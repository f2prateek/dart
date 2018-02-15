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

package dart.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static dart.processor.ProcessorTestUtilities.dartProcessorsWithoutParceler;

import com.google.common.base.Joiner;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

/** Tests {@link InjectExtraProcessor}. For tests not related to Parceler. */
public class BindExtraForFieldsTest {

  @Test
  public void bindingExtra() {
    JavaFileObject navigationModelSource =
        JavaFileObjects.forSourceString(
            "test.TestNavigationModel",
            Joiner.on('\n')
                .join(
                    "package test;",
                    "import dart.BindExtra;",
                    "public class TestNavigationModel {",
                    "    @BindExtra(\"key\") String extra;",
                    "}"));

    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.Test",
            Joiner.on('\n')
                .join(
                    "package test;",
                    "import dart.DartModel;",
                    "public class Test {",
                    "    @DartModel TestNavigationModel navigationModel;",
                    "}"));

    JavaFileObject binderSource =
        JavaFileObjects.forSourceString(
            "test/Test__ExtraBinder",
            Joiner.on('\n')
                .join(
                    "package test;",
                    "import dart.Dart;",
                    "import java.lang.Object;",
                    "public class Test__ExtraBinder {",
                    "  public static void bind(Dart.Finder finder, Test target, Object source) {",
                    "    new TestNavigationModel__ExtraBinder().bind(finder, target.navigationModel, source);",
                    "  }",
                    "}"));

    Compilation compilation =
        javac()
            .withProcessors(dartProcessorsWithoutParceler())
            .compile(navigationModelSource, source);
    assertThat(compilation)
        .generatedSourceFile("test/Test__ExtraBinder")
        .hasSourceEquivalentTo(binderSource);
  }
}
