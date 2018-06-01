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

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import dart.Dart;
import dart.DartModel;

public class SampleActivity extends Activity {

  @BindView(R.id.default_key_extra) TextView defaultKeyExtraTextView;
  @BindView(R.id.string_extra) TextView stringExtraTextView;
  @BindView(R.id.int_extra) TextView intExtraTextView;
  @BindView(R.id.parcelable_extra) TextView parcelableExtraTextView;
  @BindView(R.id.optional_extra) TextView optionalExtraTextView;
  @BindView(R.id.parcel_extra) TextView parcelExtraTextView;
  @BindView(R.id.list_parcel_extra) TextView listParcelExtraTextView;
  @BindView(R.id.sparse_array_parcel_extra) TextView sparseArrayParcelExtraTextView;
  @BindView(R.id.default_extra) TextView defaultExtraTextView;

  @DartModel SampleActivityNavigationModel navigationModel;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sample);

    ButterKnife.bind(this);
    Dart.bind(this);

    // Contrived code to use the "bound" extras.
    stringExtraTextView.setText(navigationModel.stringExtra);
    intExtraTextView.setText(String.valueOf(navigationModel.intExtra));
    parcelableExtraTextView.setText(String.valueOf(navigationModel.parcelableExtra));
    optionalExtraTextView.setText(String.valueOf(navigationModel.optionalExtra));
    parcelExtraTextView.setText(String.valueOf(navigationModel.parcelExtra.getName()));
    listParcelExtraTextView.setText(String.valueOf(navigationModel.listParcelExtra.size()));
    sparseArrayParcelExtraTextView.setText(
        String.valueOf(navigationModel.sparseArrayParcelExtra.size()));
    defaultExtraTextView.setText(String.valueOf(navigationModel.defaultExtra));
    defaultKeyExtraTextView.setText(navigationModel.defaultKeyExtra);
  }
}
