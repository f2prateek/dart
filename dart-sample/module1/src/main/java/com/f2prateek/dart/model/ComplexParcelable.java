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

package com.f2prateek.dart.model;

import android.os.Parcel;
import android.os.Parcelable;

// A parcelable containing a parcelable
public class ComplexParcelable implements Parcelable {

  SimpleParcelable parcelable;

  public ComplexParcelable(SimpleParcelable parcelable) {
    this.parcelable = parcelable;
  }

  public static ComplexParcelable random() {
    return new ComplexParcelable(SimpleParcelable.random());
  }

  protected ComplexParcelable(Parcel in) {
    parcelable = (SimpleParcelable) in.readValue(SimpleParcelable.class.getClassLoader());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeValue(parcelable);
  }

  @SuppressWarnings("unused")
  public static final Parcelable.Creator<ComplexParcelable> CREATOR =
      new Parcelable.Creator<ComplexParcelable>() {
        @Override
        public ComplexParcelable createFromParcel(Parcel in) {
          return new ComplexParcelable(in);
        }

        @Override
        public ComplexParcelable[] newArray(int size) {
          return new ComplexParcelable[size];
        }
      };

  @Override public String toString() {
    return "A parcelable with another parcelable.\n" + parcelable;
  }
}
