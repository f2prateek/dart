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
import static com.google.common.truth.Truth.assert_;

/**
 * Tests {@link com.f2prateek.dart.henson.processor.HensonExtraProcessor}.
 * For tests related to Parceler, but Parceler is not available.
 */
public class IntentBuilderGeneratorWithoutParcelerTest {

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
                "    bundler.put(\"key\",extra);", //
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

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessorsWithoutParceler())
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

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessorsWithoutParceler())
        .failsToCompile()
        .withErrorContaining("@InjectExtra field must be a primitive or Serializable or Parcelable"
            + " (test.TestNonSerializableCollection.extra). If you use Parceler, all types supported by Parceler are allowed.");
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

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessorsWithoutParceler())
        .failsToCompile()
        .withErrorContaining("@InjectExtra field must be a primitive or Serializable or Parcelable"
            + " (test.TestParcelAnnotated.extra). If you use Parceler, all types supported by Parceler are allowed.");
  }

  @Test public void collectionOfParcelAnnotatedType() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestCollectionParcel", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import com.f2prateek.dart.InjectExtra;", //
        "import java.lang.Object;", //
        "import java.lang.String;", //
        "import java.util.List;", //
        "import org.parceler.Parcel;", //
        "public class TestCollectionParcel extends Activity {", //
        "  @InjectExtra(\"key\") List<Foo> extra;", //
        "@Parcel static class Foo {}", //
        "}"
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.hensonProcessorsWithoutParceler())
        .failsToCompile()
        .withErrorContaining("@InjectExtra field must be a primitive or Serializable or Parcelable"
            + " (test.TestCollectionParcel.extra). If you use Parceler, all types supported by Parceler are allowed.");
  }

  @Test public void injectingParcelableThatExtendsParcelableExtra() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestParcelableExtendsParcelable",
        Joiner.on('\n').join( //
            "package test;", //
            "import android.app.Activity;", //
            "import android.os.Parcelable;", //
            "import com.f2prateek.dart.InjectExtra;", //
            "class ExtraParent implements Parcelable {", //
            "  public void writeToParcel(android.os.Parcel out, int flags) {", //
            "  }", //
            "  public int describeContents() {", //
            "    return 0;", //
            "  }", //
            "}", //
            "class Extra extends ExtraParent implements Parcelable {", //
            "  public void writeToParcel(android.os.Parcel out, int flags) {", //
            "  }", //
            "  public int describeContents() {", //
            "    return 0;", //
            "  }", //
            "}", //
            "public class TestParcelableExtendsParcelable extends Activity {", //
            "    @InjectExtra(\"key\") Extra extra;", //
            "}" //
        ));

    JavaFileObject builderSource =
        JavaFileObjects.forSourceString("test/Test$$IntentBuilder", Joiner.on('\n').join( //
            "package test;", //
            "import android.content.Context;", //
            "import android.content.Intent;", //
            "import com.f2prateek.dart.henson.Bundler;", //
            "public class TestParcelableExtendsParcelable$$IntentBuilder {", //
            "  private Intent intent;", //
            "  private Bundler bundler = Bundler.create();", //
            "  public TestParcelableExtendsParcelable$$IntentBuilder(Context context) {", //
            "    intent = new Intent(context, TestParcelableExtendsParcelable.class);", //
            "  }", //
            "  public TestParcelableExtendsParcelable$$IntentBuilder.AllSet key(Extra extra) {", //
            "    bundler.put(\"key\",(android.os.Parcelable) extra);", //
            "    return new TestParcelableExtendsParcelable$$IntentBuilder.AllSet();", //
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
        .processedWith(ProcessorTestUtilities.hensonProcessorsWithoutParceler())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }
}
