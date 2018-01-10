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

package com.f2prateek.dart;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.BindView;
import dart.Dart;
import com.f2prateek.dart.module1.R2;
import com.f2prateek.dart.module1.R;

public class Module1Activity extends Activity {

  @BindView(R2.id.default_key_extra) TextView defaultKeyExtraTextView;
  @BindView(R2.id.string_extra) TextView stringExtraTextView;
  @BindView(R2.id.int_extra) TextView intExtraTextView;
  @BindView(R2.id.parcelable_extra) TextView parcelableExtraTextView;
  @BindView(R2.id.optional_extra) TextView optionalExtraTextView;
  @BindView(R2.id.parcel_extra) TextView parcelExtraTextView;
  @BindView(R2.id.list_parcel_extra) TextView listParcelExtraTextView;
  @BindView(R2.id.sparse_array_parcel_extra) TextView sparseArrayParcelExtraTextView;
  @BindView(R2.id.default_extra) TextView defaultExtraTextView;

  Module1ActivityNavigationModel navigationModel = new Module1ActivityNavigationModel();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_navigation);

    ButterKnife.bind(this);
    Dart.bind(navigationModel, this);

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
