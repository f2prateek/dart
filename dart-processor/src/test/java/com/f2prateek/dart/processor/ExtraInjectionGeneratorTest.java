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

package com.f2prateek.dart.processor;

import com.f2prateek.dart.common.Binding;
import com.f2prateek.dart.common.InjectionTarget;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;

public class ExtraInjectionGeneratorTest {

  ExtraInjectionGenerator extraInjectionGenerator;

  @Before
  public void setup() {
    final InjectionTarget injectionTarget = new InjectionTarget("foo", "bar", "qurtz", false);
    extraInjectionGenerator = new ExtraInjectionGenerator(injectionTarget);
  }

  @Test public void humanDescriptionJoinWorks() {
    Binding one = new TestBinding("one");
    Binding two = new TestBinding("two");
    Binding three = new TestBinding("three");

    String actual1 = extraInjectionGenerator.emitHumanDescription(asList(one));
    assertThat(actual1).isEqualTo("one");

    String actual2 = extraInjectionGenerator.emitHumanDescription(asList(one, two));
    assertThat(actual2).isEqualTo("one and two");

    String actual3 = extraInjectionGenerator.emitHumanDescription(asList(one, two, three));
    assertThat(actual3).isEqualTo("one, two, and three");
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
