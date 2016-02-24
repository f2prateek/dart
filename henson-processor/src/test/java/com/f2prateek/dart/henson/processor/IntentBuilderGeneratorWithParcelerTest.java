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
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.truth0.Truth.ASSERT;

public class IntentBuilderGeneratorWithParcelerTest {

  @Test public void serializableCollection() {
    JavaFileObject source =
        JavaFileObjects.forSourceString("test.TestSerializableCollection", Joiner.on('\n').join( //
            "package test;", //
            "import android.app.Activity;", //
            "import java.util.ArrayList;", //
            "import com.f2prateek.dart.InjectExtra;", //
            "public class TestSerializableCollection extends Activity {", //
            "    @InjectExtra(\"key\") ArrayList<String> extra;", //
            "}" //
        ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test/TestSerializableCollection$$IntentBuilder",
            Joiner.on('\n').join( //
                "package test;", //
                "import android.content.Context;", //
                "import android.content.Intent;", //
                "import com.f2prateek.dart.henson.Bundler;", //
                "import java.lang.String;", //
                "import java.util.ArrayList;", //
                "public class TestSerializableCollection$$IntentBuilder {", //
                "  private Intent intent;", //
                "  private Bundler bundler = Bundler.create();", //
                "  public TestSerializableCollection$$IntentBuilder(Context context) {", //
                "    intent = new Intent(context, TestSerializableCollection.class);", //
                "  }", //
                "  public TestSerializableCollection$$IntentBuilder.AllSet key(ArrayList<String> extra) {",
                //
                "    bundler.put(\"key\", org.parceler.Parcels.wrap(extra));", //
                "    return new TestSerializableCollection$$IntentBuilder.AllSet();", //
                "  }", //
                "  public class AllSet {", //
                "    public Intent build() {", //
                "      intent.putExtras(bundler.get());", //
                "      return intent;", //
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

  @Test public void nonSerializableCollection() {
    JavaFileObject source =
        JavaFileObjects.forSourceString("test.TestNonSerializableCollection", Joiner.on('\n').join( //
            "package test;", //
            "import android.app.Activity;", //
            "import java.util.List;", //
            "import com.f2prateek.dart.InjectExtra;", //
            "public class TestNonSerializableCollection extends Activity {", //
            "    @InjectExtra(\"key\") List<String> extra;", //
            "}" //
        ));

      JavaFileObject builderSource =
          JavaFileObjects.forSourceString("test/TestNonSerializableCollection$$IntentBuilder",
              Joiner.on('\n').join( //
                  "package test;", //
                  "import android.content.Context;", //
                  "import android.content.Intent;", //
                  "import com.f2prateek.dart.henson.Bundler;", //
                  "import java.lang.String;", //
                  "import java.util.List;", //
                  "public class TestNonSerializableCollection$$IntentBuilder {", //
                  "  private Intent intent;", //
                  "  private Bundler bundler = Bundler.create();", //
                  "  public TestNonSerializableCollection$$IntentBuilder(Context context) {", //
                  "    intent = new Intent(context, TestNonSerializableCollection.class);", //
                  "  }", //
                  "  public TestNonSerializableCollection$$IntentBuilder.AllSet key(List<String> extra) {",
                  //
                  "    bundler.put(\"key\", org.parceler.Parcels.wrap(extra));", //
                  "    return new TestNonSerializableCollection$$IntentBuilder.AllSet();", //
                  "  }", //
                  "  public class AllSet {", //
                  "    public Intent build() {", //
                  "      intent.putExtras(bundler.get());", //
                  "      return intent;", //
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

  @Test public void parcelAnnotatedType() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestParcelAnnotated", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "import java.lang.Object;", //
        "import java.lang.String;", //
        "import org.parceler.Parcel;", //
        "public class TestParcelAnnotated extends Activity {", //
        "  @InjectExtra(\"key\") Foo extra;", //
        "@Parcel static class Foo {}", //
        "}"
    ));

      JavaFileObject builderSource =
          JavaFileObjects.forSourceString("test/TestParcelAnnotated$$IntentBuilder",
              Joiner.on('\n').join( //
                  "package test;", //
                  "import android.content.Context;", //
                  "import android.content.Intent;", //
                  "import com.f2prateek.dart.henson.Bundler;", //
                  "public class TestParcelAnnotated$$IntentBuilder {", //
                  "  private Intent intent;", //
                  "  private Bundler bundler = Bundler.create();", //
                  "  public TestParcelAnnotated$$IntentBuilder(Context context) {", //
                  "    intent = new Intent(context, TestParcelAnnotated.class);", //
                  "  }", //
                  "  public TestParcelAnnotated$$IntentBuilder.AllSet key(TestParcelAnnotated.Foo extra) {",
                  //
                  "    bundler.put(\"key\", org.parceler.Parcels.wrap(extra));", //
                  "    return new TestParcelAnnotated$$IntentBuilder.AllSet();", //
                  "  }", //
                  "  public class AllSet {", //
                  "    public Intent build() {", //
                  "      intent.putExtras(bundler.get());", //
                  "      return intent;", //
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

  @Test public void collectionOfParcelAnnotatedType() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestCollectionParcel", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "import java.lang.Object;", //
        "import java.util.List;", //
        "import org.parceler.Parcel;", //
        "public class TestCollectionParcel extends Activity {", //
        "  @InjectExtra(\"key\") List<Foo> extra;", //
        "@Parcel static class Foo {}", //
        "}"
    ));

      JavaFileObject builderSource =
          JavaFileObjects.forSourceString("test/TestCollectionParcel$$IntentBuilder",
              Joiner.on('\n').join( //
                  "package test;", //
                  "import android.content.Context;", //
                  "import android.content.Intent;", //
                  "import com.f2prateek.dart.henson.Bundler;", //
                  "import java.util.List;", //
                  "public class TestCollectionParcel$$IntentBuilder {", //
                  "  private Intent intent;", //
                  "  private Bundler bundler = Bundler.create();", //
                  "  public TestCollectionParcel$$IntentBuilder(Context context) {", //
                  "    intent = new Intent(context, TestCollectionParcel.class);", //
                  "  }", //
                  "  public TestCollectionParcel$$IntentBuilder.AllSet key(List<TestCollectionParcel.Foo> extra) {",
                  //
                  "    bundler.put(\"key\", org.parceler.Parcels.wrap(extra));", //
                  "    return new TestCollectionParcel$$IntentBuilder.AllSet();", //
                  "  }", //
                  "  public class AllSet {", //
                  "    public Intent build() {", //
                  "      intent.putExtras(bundler.get());", //
                  "      return intent;", //
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
}
