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

package com.f2prateek.dart.example;

import android.support.annotation.Nullable;
import android.util.SparseArray;
import com.f2prateek.dart.model.ComplexParcelable;
import com.f2prateek.dart.model.StringParcel;
import dart.BindExtra;
import dart.DartModel;
import java.util.List;

@DartModel("com.f2prateek.dart.example.SampleActivity")
public class SampleActivityModel {

  public static final String DEFAULT_EXTRA_VALUE = "a default value";

  private static final String EXTRA_STRING = "extraString";
  private static final String EXTRA_INT = "extraInt";
  private static final String EXTRA_PARCELABLE = "extraParcelable";
  private static final String EXTRA_LIST_PARCELABLE = "extraListParcelable";
  private static final String EXTRA_SPARSE_ARRAY_PARCELABLE = "extraSparseArrayParcelable";
  private static final String EXTRA_OPTIONAL = "extraOptional";
  private static final String EXTRA_PARCEL = "extraParcel";
  private static final String EXTRA_WITH_DEFAULT = "extraWithDefault";

  public @BindExtra(EXTRA_STRING) String stringExtra;
  public @BindExtra(EXTRA_INT) int intExtra;
  public @BindExtra(EXTRA_PARCELABLE) ComplexParcelable parcelableExtra;
  public @BindExtra(EXTRA_PARCEL) StringParcel parcelExtra;
  public @BindExtra(EXTRA_LIST_PARCELABLE) List<StringParcel> listParcelExtra;
  public @BindExtra(EXTRA_SPARSE_ARRAY_PARCELABLE) SparseArray<StringParcel> sparseArrayParcelExtra;
  public @BindExtra(EXTRA_OPTIONAL) @Nullable String optionalExtra;
  public @BindExtra(EXTRA_WITH_DEFAULT) @Nullable String defaultExtra = DEFAULT_EXTRA_VALUE;
  public @BindExtra String defaultKeyExtra;
}
