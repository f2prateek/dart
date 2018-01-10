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
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.f2prateek.dart.model.ComplexParcelable;
import com.f2prateek.dart.model.StringParcel;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ButterKnife.bind(this);
  }

  // Launch Sample Activity residing in the same module
  @OnClick(R.id.navigateToSampleActivity)
  public void onSampleActivityCTAClick() {
    StringParcel parcel1 = new StringParcel("Andy");
    StringParcel parcel2 = new StringParcel("Tony");

    List<StringParcel> parcelList = new ArrayList<>();
    parcelList.add(parcel1);
    parcelList.add(parcel2);

    SparseArray<StringParcel> parcelSparseArray = new SparseArray<>();
    parcelSparseArray.put(0, parcel1);
    parcelSparseArray.put(2, parcel2);

    Intent intent = HensonNavigator.gotoSampleActivity(this)
        .defaultKeyExtra("defaultKeyExtra")
        .extraInt(4)
        .extraListParcelable(parcelList)
        .extraParcel(parcel1)
        .extraParcelable(ComplexParcelable.random())
        .extraSparseArrayParcelable(parcelSparseArray)
        .extraString("a string")
        .build();

    startActivity(intent);
  }

  // Launch Navigation Activity residing in the navigation module
  @OnClick(R.id.navigateToModule1Activity)
  public void onNavigationActivityCTAClick() {
    StringParcel parcel1 = new StringParcel("Andy");
    StringParcel parcel2 = new StringParcel("Tony");

    List<StringParcel> parcelList = new ArrayList<>();
    parcelList.add(parcel1);
    parcelList.add(parcel2);

    SparseArray<StringParcel> parcelSparseArray = new SparseArray<>();
    parcelSparseArray.put(0, parcel1);
    parcelSparseArray.put(2, parcel2);

    Intent intent = HensonNavigator.gotoModule1Activity(this)
        .defaultKeyExtra("defaultKeyExtra")
        .extraInt(4)
        .extraListParcelable(parcelList)
        .extraParcel(parcel1)
        .extraParcelable(ComplexParcelable.random())
        .extraSparseArrayParcelable(parcelSparseArray)
        .extraString("a string")
        .build();

    startActivity(intent);
  }

  // Launch Navigation Service residing in the navigation module
  @OnClick(R.id.navigateToModule1Service)
  public void onNavigationServiceCTAClick() {
    Intent intentService = HensonNavigator.gotoModule1Service(this)
        .stringExtra("foo")
        .build();

    startService(intentService);
  }
}
