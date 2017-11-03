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

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static com.google.common.truth.Truth.assert_;

/**
 * Tests {@link dart.processor.InjectExtraProcessor}.
 * For tests related to Parceler, but Parceler is not available.
 */
public class InjectExtraWithoutParcelerTest {

  @Test public void serializableCollection() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestSerializableCollection", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import dart.InjectExtra;", //
        "import java.lang.Object;", //
        "import java.lang.String;", //
        "import java.util.ArrayList;", //
        "public class TestSerializableCollection extends Activity {", //
        "  @InjectExtra(\"key\") ArrayList<String> extra;", //
        "}" //
    ));

    JavaFileObject expectedSource =
        JavaFileObjects.forSourceString("test/TestSerializableCollection$$ExtraInjector", Joiner.on('\n').join( //
            "package test;", //
            "import dart.Dart;", //
            "import java.lang.Object;", //
            "import java.lang.String;", //
            "import java.util.ArrayList;", //
            "public class TestSerializableCollection$$ExtraInjector {", //
            "  public static void inject(Dart.Finder finder, TestSerializableCollection target, Object source) {", //
            "    Object object;", //
            "    object = finder.getExtra(source, \"key\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key' for field 'extra' was not found. If this extra is optional add '@Nullable' annotation.\");", //
            "    }", //
            "    target.extra = (ArrayList<String>) object;", //
            "  }", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.dartProcessorsWithoutParceler())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test public void nonSerializableNonParcelableCollection_withoutParceler() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestNonSerializableNonParcelableCollection_withoutParceler", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import dart.InjectExtra;", //
        "import java.lang.Object;", //
        "import java.lang.String;", //
        "import java.util.List;", //
        "public class TestNonSerializableNonParcelableCollection_withoutParceler extends Activity {",
        //
        "  @InjectExtra(\"key\") List<String> extra;", //
        "}" //
    ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.dartProcessorsWithoutParceler())
        .failsToCompile()
        .withErrorContaining("@InjectExtra field must be a primitive or Serializable or Parcelable"
            + " (test.TestNonSerializableNonParcelableCollection_withoutParceler.extra). If you use Parceler, all types supported by Parceler are allowed.");
  }

  @Test public void parcelAnnotatedType() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestParcelAnnotated", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import dart.InjectExtra;", //
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
        .processedWith(ProcessorTestUtilities.dartProcessorsWithoutParceler())
        .failsToCompile()
        .withErrorContaining("@InjectExtra field must be a primitive or Serializable or Parcelable"
            + " (test.TestParcelAnnotated.extra). If you use Parceler, all types supported by Parceler are allowed.");
  }

  @Test public void collectionOfParcelAnnotatedType() {
    JavaFileObject source = JavaFileObjects.forSourceString("test.TestCollectionParcel", Joiner.on('\n').join( //
        "package test;", //
        "import android.app.Activity;", //
        "import dart.InjectExtra;", //
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
        .processedWith(ProcessorTestUtilities.dartProcessorsWithoutParceler())
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
            "import dart.InjectExtra;", //
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
        JavaFileObjects.forSourceString("test/Test$$ExtraInjector", Joiner.on('\n').join( //
            "package test;", //
            "import dart.Dart;", //
            "import java.lang.Object;", //
            "public class TestParcelableExtendsParcelable$$ExtraInjector {", //
            "  public static void inject(Dart.Finder finder, TestParcelableExtendsParcelable target, Object source) {", //
            "    Object object;", //
            "    object = finder.getExtra(source, \"key\");", //
            "    if (object == null) {", //
            "      throw new IllegalStateException(\"Required extra with key 'key' for field 'extra' was not found. If this extra is optional add '@Nullable' annotation.\");", //
            "    }", //
            "    target.extra = (Extra) object;", //
            "  }", //
            "}" //
        ));

    assert_().about(javaSource())
        .that(source)
        .processedWith(ProcessorTestUtilities.dartProcessorsWithoutParceler())
        .compilesWithoutError()
        .and()
        .generatesSources(builderSource);
  }
}
