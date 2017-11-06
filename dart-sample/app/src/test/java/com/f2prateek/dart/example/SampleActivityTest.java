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
import android.util.SparseArray;
import com.f2prateek.dart.model.ComplexParcelable;
import com.f2prateek.dart.model.StringParcel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static com.f2prateek.dart.example.SampleActivityModel.DEFAULT_EXTRA_VALUE;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "app/src/main/AndroidManifest.xml")
public class SampleActivityTest {
  @Test public void verifyExtrasInjection() {
    ComplexParcelable parcelable = ComplexParcelable.random();
    StringParcel parcel1 = new StringParcel("Andy");
    StringParcel parcel2 = new StringParcel("Tony");
    List<StringParcel> parcelList = new ArrayList<>();
    parcelList.add(parcel1);
    parcelList.add(parcel2);
    SparseArray<StringParcel> parcelSparseArray = new SparseArray<>();
    parcelSparseArray.put(0, parcel1);
    parcelSparseArray.put(2, parcel2);

    Intent intent = new SampleActivity__IntentBuilder(Robolectric.application)
        .defaultKeyExtra("defaultKeyExtra")
        .extraInt(4)
        .extraListParcelable(parcelList)
        .extraParcel(parcel1)
        .extraParcelable(parcelable)
        .extraSparseArrayParcelable(parcelSparseArray)
        .extraString("test")
        .build();

    SampleActivity activity =
        Robolectric.buildActivity(SampleActivity.class)
            .withIntent(intent)
            .create()
            .get();

    assertThat(activity.navigationModel.stringExtra).isEqualTo("test");
    assertThat(activity.navigationModel.intExtra).isEqualTo(4);
    assertThat(activity.navigationModel.parcelableExtra).isEqualTo(parcelable);
    assertThat(activity.navigationModel.parcelExtra).isEqualTo(parcel1);
    assertThat(activity.navigationModel.listParcelExtra.size()).isEqualTo(2);
    assertThat(activity.navigationModel.listParcelExtra.get(0)).isEqualTo(parcel1);
    assertThat(activity.navigationModel.listParcelExtra.get(1)).isEqualTo(parcel2);
    assertThat(activity.navigationModel.sparseArrayParcelExtra.size()).isEqualTo(2);
    assertThat(activity.navigationModel.sparseArrayParcelExtra.get(0)).isEqualTo(parcel1);
    assertThat(activity.navigationModel.sparseArrayParcelExtra.get(2)).isEqualTo(parcel2);
    assertThat(activity.navigationModel.defaultExtra).isEqualTo(DEFAULT_EXTRA_VALUE);
    assertThat(activity.navigationModel.defaultKeyExtra).isEqualTo("defaultKeyExtra");
  }
}
