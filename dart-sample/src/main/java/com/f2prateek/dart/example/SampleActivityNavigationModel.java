package com.f2prateek.dart.example;

import android.util.SparseArray;
import com.f2prateek.dart.InjectExtra;
import com.f2prateek.dart.NavigationModel;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.CLASS;

@NavigationModel("com.f2prateek.dart.example.SampleActivity")
public class SampleActivityNavigationModel {

  public static final String DEFAULT_EXTRA_VALUE = "a default value";

  private static final String EXTRA_STRING = "extraString";
  private static final String EXTRA_INT = "extraInt";
  private static final String EXTRA_PARCELABLE = "extraParcelable";
  private static final String EXTRA_LIST_PARCELABLE = "extraListParcelable";
  private static final String EXTRA_SPARSE_ARRAY_PARCELABLE = "extraSparseArrayParcelable";
  private static final String EXTRA_OPTIONAL = "extraOptional";
  private static final String EXTRA_PARCEL = "extraParcel";
  private static final String EXTRA_WITH_DEFAULT = "extraWithDefault";

  @InjectExtra(EXTRA_STRING) String stringExtra;
  @InjectExtra(EXTRA_INT) int intExtra;
  @InjectExtra(EXTRA_PARCELABLE) ComplexParcelable parcelableExtra;
  @InjectExtra(EXTRA_PARCEL) ExampleParcel parcelExtra;
  @InjectExtra(EXTRA_LIST_PARCELABLE) List<ExampleParcel> listParcelExtra;
  @InjectExtra(EXTRA_SPARSE_ARRAY_PARCELABLE) SparseArray<ExampleParcel> sparseArrayParcelExtra;
  @InjectExtra(EXTRA_OPTIONAL) @Nullable String optionalExtra;
  @InjectExtra(EXTRA_WITH_DEFAULT) @Nullable String defaultExtra = DEFAULT_EXTRA_VALUE;
  @InjectExtra String defaultKeyExtra;

  @Retention(CLASS) @Target(FIELD) @interface Nullable {
  }
}
