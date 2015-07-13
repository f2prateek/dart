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

package com.f2prateek.dart.internal;

import org.junit.Test;

import static com.f2prateek.dart.internal.ExtraInjector.emitHumanDescription;
import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;

public class ExtraInjectorTest {
  @Test public void humanDescriptionJoinWorks() {
    Binding one = new TestBinding("one");
    Binding two = new TestBinding("two");
    Binding three = new TestBinding("three");

    StringBuilder builder1 = new StringBuilder();
    emitHumanDescription(builder1, asList(one));
    assertThat(builder1.toString()).isEqualTo("one");

    StringBuilder builder2 = new StringBuilder();
    emitHumanDescription(builder2, asList(one, two));
    assertThat(builder2.toString()).isEqualTo("one and two");

    StringBuilder builder3 = new StringBuilder();
    emitHumanDescription(builder3, asList(one, two, three));
    assertThat(builder3.toString()).isEqualTo("one, two, and three");
  }

  private static class TestBinding implements Binding {
    private final String description;

    private TestBinding(String description) {
      this.description = description;
    }

    @Override public String getDescription() {
      return description;
    }

    @Override public boolean isRequired() {
      throw new AssertionError();
    }
  }
}
