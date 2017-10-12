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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ButterKnife.inject(this);
  }

  @OnClick(R.id.button) public void onLaunchButtonClick() {
    ExampleParcel parcel1 = new ExampleParcel("Andy");
    ExampleParcel parcel2 = new ExampleParcel("Tony");

    List<ExampleParcel> parcelList = new ArrayList<>();
    parcelList.add(parcel1);
    parcelList.add(parcel2);

    SparseArray<ExampleParcel> parcelSparseArray = new SparseArray<>();
    parcelSparseArray.put(0, parcel1);
    parcelSparseArray.put(2, parcel2);

    Intent intent = Henson.with(this)
        .gotoSampleActivity()
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

  @OnClick(R.id.button2) public void onLaunchButton2Click() {
    // Service
    Intent intentService = Henson.with(this)
        .gotoSampleService()
        .stringExtra("foo")
        .build();

    startService(intentService);
  }
}
