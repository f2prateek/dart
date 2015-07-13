/*
 * Copyright 2014 Prateek Srivastava
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.f2prateek.dart.henson;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Fluent API for {@link Bundle}
 * Usage: {@code Bundle delegate = new Bundler().put(....).put(....).get();}
 */
public class Bundler {

  private final Bundle delegate;

  /** Returns a bundler that delegates to a copy of the source bundle. */
  public static Bundler copyOf(Bundle source) {
    return create().putAll(source);
  }

  /** Returns a bundler that delegates to the source bundle. */
  public static Bundler of(Bundle source) {
    return new Bundler(source);
  }

  /** Creates a bundler instance. */
  public static Bundler create() {
    return new Bundler(new Bundle());
  }

  /** Constructs a new Bundler instance that delegates to {@code delegate}. */
  private Bundler(Bundle delegate) {
    this.delegate = delegate;
  }

  /**
   * Inserts a Boolean value into the mapping of the underlying Bundle, replacing
   * any existing value for the given key.  Either key or value may be null.
   *
   * @param key a String, or null
   * @param value a Boolean, or null
   * @return this bundler instance to chain method calls
   */
  public Bundler put(String key, boolean value) {
    delegate.putBoolean(key, value);
    return this;
  }

  /**
   * Inserts a boolean array value into the mapping of the underlying Bundle, replacing
   * any existing value for the given key.  Either key or value may be null.
   *
   * @param key a String, or null
   * @param value a boolean array object, or null
   * @return this bundler instance to chain method calls
   */
  public Bundler put(String key, boolean[] value) {
    delegate.putBooleanArray(key, value);
    return this;
  }

  /**
   * Inserts an int value into the mapping of the underlying Bundle, replacing
   * any existing value for the given key.
   *
   * @param key a String, or null
   * @param value an int, or null
   * @return this bundler instance to chain method calls
   */
  public Bundler put(String key, int value) {
    delegate.putInt(key, value);
    return this;
  }

  /**
   * Inserts an int array value into the mapping of the underlying Bundle, replacing
   * any existing value for the given key.  Either key or value may be null.
   *
   * @param key a String, or null
   * @param value an int array object, or null
   * @return this bundler instance to chain method calls
   */
  public Bundler put(String key, int[] value) {
    delegate.putIntArray(key, value);
    return this;
  }

  /**
   * Inserts an ArrayList<Integer> value into the mapping of the underlying Bundle, replacing
   * any existing value for the given key.  Either key or value may be null.
   *
   * @param key a String, or null
   * @param value an ArrayList<Integer> object, or null
   * @return this bundler instance to chain method calls
   */
  public Bundler putIntegerArrayList(String key, ArrayList<Integer> value) {
    delegate.putIntegerArrayList(key, value);
    return this;
  }

  /**
   * Inserts a Bundle value into the mapping of the underlying Bundle, replacing
   * any existing value for the given key.  Either key or value may be null.
   *
   * @param key a String, or null
   * @param value a Bundle object, or null
   * @return this bundler instance to chain method calls
   */
  public Bundler put(String key, Bundle value) {
    delegate.putBundle(key, value);
    return this;
  }

  /**
   * Inserts a byte value into the mapping of the underlying Bundle, replacing
   * any existing value for the given key.
   *
   * @param key a String, or null
   * @param value a byte
   * @return this bundler instance to chain method calls
   */
  public Bundler put(String key, byte value) {
    delegate.putByte(key, value);
    return this;
  }

  /**
   * Inserts a byte array value into the mapping of the underlying Bundle, replacing
   * any existing value for the given key.  Either key or value may be null.
   *
   * @param key a String, or null
   * @param value a byte array object, or null
   * @return this bundler instance to chain method calls
   */
  public Bundler put(String key, byte[] value) {
    delegate.putByteArray(key, value);
    return this;
  }

  /**
   * Inserts a String value into the mapping of the underlying Bundle, replacing
   * any existing value for the given key.  Either key or value may be null.
   *
   * @param key a String, or null
   * @param value a String, or null
   * @return this bundler instance to chain method calls
   */
  public Bundler put(String key, String value) {
    delegate.putString(key, value);
    return this;
  }

  /**
   * Inserts a String array value into the mapping of the underlying Bundle, replacing
   * any existing value for the given key.  Either key or value may be null.
   *
   * @param key a String, or null
   * @param value a String array object, or null
   * @return this bundler instance to chain method calls
   */
  public Bundler put(String key, String[] value) {
    delegate.putStringArray(key, value);
    return this;
  }

  /**
   * Inserts an ArrayList<String> value into the mapping of the underlying Bundle, replacing
   * any existing value for the given key.  Either key or value may be null.
   *
   * @param key a String, or null
   * @param value an ArrayList<String> object, or null
   * @return this bundler instance to chain method calls
   */
  public Bundler putStringArrayList(String key, ArrayList<String> value) {
    delegate.putStringArrayList(key, value);
    return this;
  }

  /**
   * Inserts a long value into the mapping of the underlying Bundle, replacing
   * any existing value for the given key.
   *
   * @param key a String, or null
   * @param value a long
   * @return this bundler instance to chain method calls
   */
  public Bundler put(String key, long value) {
    delegate.putLong(key, value);
    return this;
  }

  /**
   * Inserts a long array value into the mapping of the underlying Bundle, replacing
   * any existing value for the given key.  Either key or value may be null.
   *
   * @param key a String, or null
   * @param value a long array object, or null
   * @return this bundler instance to chain method calls
   */
  public Bundler put(String key, long[] value) {
    delegate.putLongArray(key, value);
    return this;
  }

  /**
   * Inserts a float value into the mapping of the underlying Bundle, replacing
   * any existing value for the given key.
   *
   * @param key a String, or null
   * @param value a float
   * @return this bundler instance to chain method calls
   */
  public Bundler put(String key, float value) {
    delegate.putFloat(key, value);
    return this;
  }

  /**
   * Inserts a float array value into the mapping of the underlying Bundle, replacing
   * any existing value for the given key.  Either key or value may be null.
   *
   * @param key a String, or null
   * @param value a float array object, or null
   * @return this bundler instance to chain method calls
   */
  public Bundler put(String key, float[] value) {
    delegate.putFloatArray(key, value);
    return this;
  }

  /**
   * Inserts a char value into the mapping of the underlying Bundle, replacing
   * any existing value for the given key.
   *
   * @param key a String, or null
   * @param value a char, or null
   * @return this bundler instance to chain method calls
   */
  public Bundler put(String key, char value) {
    delegate.putChar(key, value);
    return this;
  }

  /**
   * Inserts a char array value into the mapping of the underlying Bundle, replacing
   * any existing value for the given key.  Either key or value may be null.
   *
   * @param key a String, or null
   * @param value a char array object, or null
   * @return this bundler instance to chain method calls
   */
  public Bundler put(String key, char[] value) {
    delegate.putCharArray(key, value);
    return this;
  }

  /**
   * Inserts a CharSequence value into the mapping of the underlying Bundle, replacing
   * any existing value for the given key.  Either key or value may be null.
   *
   * @param key a String, or null
   * @param value a CharSequence, or null
   * @return this bundler instance to chain method calls
   */
  public Bundler put(String key, CharSequence value) {
    delegate.putCharSequence(key, value);
    return this;
  }

  /**
   * Inserts a CharSequence array value into the mapping of the underlying Bundle, replacing
   * any existing value for the given key.  Either key or value may be null.
   *
   * @param key a String, or null
   * @param value a CharSequence array object, or null
   * @return this bundler instance to chain method calls
   */
  public Bundler put(String key, CharSequence[] value) {
    delegate.putCharSequenceArray(key, value);
    return this;
  }

  /**
   * Inserts an ArrayList<CharSequence> value into the mapping of the underlying Bundle, replacing
   * any existing value for the given key.  Either key or value may be null.
   *
   * @param key a String, or null
   * @param value an ArrayList<CharSequence> object, or null
   * @return this bundler instance to chain method calls
   */
  public Bundler putCharSequenceArrayList(String key, ArrayList<CharSequence> value) {
    delegate.putCharSequenceArrayList(key, value);
    return this;
  }

  /**
   * Inserts a double value into the mapping of the underlying Bundle, replacing
   * any existing value for the given key.
   *
   * @param key a String, or null
   * @param value a double
   * @return this bundler instance to chain method calls
   */
  public Bundler put(String key, double value) {
    delegate.putDouble(key, value);
    return this;
  }

  /**
   * Inserts a double array value into the mapping of the underlying Bundle, replacing
   * any existing value for the given key.  Either key or value may be null.
   *
   * @param key a String, or null
   * @param value a double array object, or null
   * @return this bundler instance to chain method calls
   */
  public Bundler put(String key, double[] value) {
    delegate.putDoubleArray(key, value);
    return this;
  }

  /**
   * Inserts a Parcelable value into the mapping of the underlying Bundle, replacing
   * any existing value for the given key.  Either key or value may be null.
   *
   * @param key a String, or null
   * @param value a Parcelable object, or null
   * @return this bundler instance to chain method calls
   */
  public Bundler put(String key, Parcelable value) {
    delegate.putParcelable(key, value);
    return this;
  }

  /**
   * Inserts an array of Parcelable values into the mapping of the underlying Bundle,
   * replacing any existing value for the given key.  Either key or value may
   * be null.
   *
   * @param key a String, or null
   * @param value an array of Parcelable objects, or null
   * @return this bundler instance to chain method calls
   */
  public Bundler put(String key, Parcelable[] value) {
    delegate.putParcelableArray(key, value);
    return this;
  }

  /**
   * Inserts a List of Parcelable values into the mapping of the underlying Bundle,
   * replacing any existing value for the given key.  Either key or value may
   * be null.
   *
   * @param key a String, or null
   * @param value an ArrayList of Parcelable objects, or null
   * @return this bundler instance to chain method calls
   */
  public Bundler putParcelableArrayList(String key, ArrayList<? extends Parcelable> value) {
    delegate.putParcelableArrayList(key, value);
    return this;
  }

  /**
   * Inserts a SparceArray of Parcelable values into the mapping of this
   * Bundle, replacing any existing value for the given key.  Either key
   * or value may be null.
   *
   * @param key a String, or null
   * @param value a SparseArray of Parcelable objects, or null
   * @return this bundler instance to chain method calls
   */
  public Bundler putSparseParcelableArray(String key, SparseArray<? extends Parcelable> value) {
    delegate.putSparseParcelableArray(key, value);
    return this;
  }

  /**
   * Inserts a short value into the mapping of the underlying Bundle, replacing
   * any existing value for the given key.
   *
   * @param key a String, or null
   * @param value a short
   * @return this bundler instance to chain method calls
   */
  public Bundler put(String key, short value) {
    delegate.putShort(key, value);
    return this;
  }

  /**
   * Inserts a short array value into the mapping of the underlying Bundle, replacing
   * any existing value for the given key.  Either key or value may be null.
   *
   * @param key a String, or null
   * @param value a short array object, or null
   * @return this bundler instance to chain method calls
   */
  public Bundler put(String key, short[] value) {
    delegate.putShortArray(key, value);
    return this;
  }

  /**
   * Inserts a Serializable value into the mapping of the underlying Bundle, replacing
   * any existing value for the given key.  Either key or value may be null.
   *
   * @param key a String, or null
   * @param value a Serializable object, or null
   * @return this bundler instance to chain method calls
   */
  public Bundler put(String key, Serializable value) {
    delegate.putSerializable(key, value);
    return this;
  }

  /**
   * Inserts all mappings from the given Bundle into the underlying Bundle.
   *
   * @param bundle a Bundle
   * @return this bundler instance to chain method calls
   */
  public Bundler putAll(Bundle bundle) {
    delegate.putAll(bundle);
    return this;
  }

  /** Get a reference underlying delegate. */
  public Bundle get() {
    return delegate;
  }

  /** Get a copy of the underlying delegate. */
  public Bundle copy() {
    return new Bundle(delegate);
  }
}
