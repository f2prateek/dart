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

package com.f2prateek.dart.henson;

import android.app.Activity;
import com.f2prateek.dart.henson.Henson;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.entry;

@RunWith(RobolectricTestRunner.class) @Config(manifest = Config.NONE)
public class HensonTest {
  @Before @After // Clear out cache of injectors  before and after each test.
  public void resetExtrasCache() {
    Henson.INJECTORS.clear();
  }

  @Test public void zeroInjectionsInjectDoesNotThrowException() {
    class Example {
    }

    Example example = new Example();
    Henson.inject(example, null, null);
    assertThat(Henson.INJECTORS).contains(entry(Example.class, Henson.NO_OP));
  }

  @Test public void injectingKnownPackagesIsNoOp() {
    Henson.inject(new Activity());
    assertThat(Henson.INJECTORS).isEmpty();
    Henson.inject(new Object(), new Activity());
    assertThat(Henson.INJECTORS).isEmpty();
  }
}
