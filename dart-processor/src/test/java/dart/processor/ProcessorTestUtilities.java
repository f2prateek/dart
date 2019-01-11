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

package dart.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

final class ProcessorTestUtilities {
  static ExtraBinderProcessor extraBinderProcessors() {
    return new ExtraBinderProcessor();
  }

  static ExtraBinderProcessor extraBinderProcessorsWithoutParceler() {
    ExtraBinderProcessor bindExtraProcessor = new ExtraBinderProcessor();
    bindExtraProcessor.enableParceler(false);
    return bindExtraProcessor;
  }

  static NavigationModelBinderProcessor navigationModelBinderProcessors() {
    return new NavigationModelBinderProcessor();
  }

  static TypeElement getMostEnclosingElement(Element element) {
    if (element == null) {
      return null;
    }

    while (element.getEnclosingElement() != null
            && element.getEnclosingElement().getKind().isClass()
        || element.getEnclosingElement().getKind().isInterface()) {
      element = element.getEnclosingElement();
    }
    return (TypeElement) element;
  }
}
