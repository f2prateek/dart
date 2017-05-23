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

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Random;

// A parcelable of primitives
public class SimpleParcelable implements Parcelable {

  int anInt;
  boolean aBoolean;
  float aFloat;

  public SimpleParcelable(int anInt, boolean aBoolean, float aFloat) {
    this.anInt = anInt;
    this.aBoolean = aBoolean;
    this.aFloat = aFloat;
  }

  static SimpleParcelable random() {
    Random random = new Random();
    int anInt = random.nextInt();
    boolean aBoolean = random.nextBoolean();
    float aFloat = random.nextFloat();
    return new SimpleParcelable(anInt, aBoolean, aFloat);
  }

  protected SimpleParcelable(Parcel in) {
    anInt = in.readInt();
    aBoolean = in.readByte() != 0x00;
    aFloat = in.readFloat();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(anInt);
    dest.writeByte((byte) (aBoolean ? 0x01 : 0x00));
    dest.writeFloat(aFloat);
  }

  @SuppressWarnings("unused")
  public static final Parcelable.Creator<SimpleParcelable> CREATOR =
      new Parcelable.Creator<SimpleParcelable>() {
        @Override
        public SimpleParcelable createFromParcel(Parcel in) {
          return new SimpleParcelable(in);
        }

        @Override
        public SimpleParcelable[] newArray(int size) {
          return new SimpleParcelable[size];
        }
      };

  @Override public String toString() {
    return "A parcelable with " + anInt + ", " + aBoolean + " and " + aFloat;
  }
}