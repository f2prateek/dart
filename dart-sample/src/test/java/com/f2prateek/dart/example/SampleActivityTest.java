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

import android.content.Intent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "src/main/AndroidManifest.xml")
public class SampleActivityTest {
  @Test public void verifyExtrasInjection() {
    ComplexParcelable parcelable = ComplexParcelable.random();
    ExampleParcel parcel = new ExampleParcel("andy");

    Intent intent = new SampleActivity$$IntentBuilder(Robolectric.application)
        .ExtraInt(4)
        .ExtraParcel(parcel)
        .ExtraParcelable(parcelable)
        .ExtraString("test")
        .defaultKeyExtra("defaultKeyExtra")
        .get();

    SampleActivity activity =
        Robolectric.buildActivity(SampleActivity.class).withIntent(intent).create().get();

    assertThat(activity.stringExtra).isEqualTo("test");
    assertThat(activity.intExtra).isEqualTo(4);
    assertThat(activity.parcelableExtra).isEqualTo(parcelable);
    assertThat(activity.parcelExtra).isEqualTo(parcel);
    assertThat(activity.defaultExtra).isEqualTo(SampleActivity.DEFAULT_EXTRA_VALUE);
    assertThat(activity.defaultKeyExtra).isEqualTo("defaultKeyExtra");
  }
}
