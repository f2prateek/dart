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

package dart;

import static dart.Dart.EXTRA_BINDERS;
import static dart.Dart.NAVIGATION_MODEL_BINDERS;
import static dart.Dart.NO_OP;
import static dart.Dart.bind;
import static dart.Dart.bindNavigationModel;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.entry;

import android.app.Activity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class DartTest {
  @Before
  @After // Clear out cache of biners  before and after each test.
  public void resetExtrasCache() {
    EXTRA_BINDERS.clear();
    NAVIGATION_MODEL_BINDERS.clear();
  }

  @Test
  public void zeroInjectionsInjectDoesNotThrowException() {
    class Example {}

    Example example = new Example();
    bind(example, null);
    bindNavigationModel(example, null, null);
    assertThat(EXTRA_BINDERS).contains(entry(Example.class, NO_OP));
    assertThat(NAVIGATION_MODEL_BINDERS).contains(entry(Example.class, NO_OP));
  }

  @Test
  public void bindingKnownPackagesIsNoOp() {
    bind(new Activity());
    bindNavigationModel(new Object(), new Activity());
    assertThat(EXTRA_BINDERS).isEmpty();
    assertThat(NAVIGATION_MODEL_BINDERS).isEmpty();
  }
}
