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

package dart.common.util;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

import dart.DartModel;
import dart.common.NavigationModelBindingTarget;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

public class NavigationModelFieldUtil {

  private final LoggingUtil loggingUtil;
  private final NavigationModelBindingTargetUtil navigationModelBindingTargetUtil;

  private RoundEnvironment roundEnv;

  public NavigationModelFieldUtil(
      LoggingUtil loggingUtil, NavigationModelBindingTargetUtil navigationModelBindingTargetUtil) {
    this.loggingUtil = loggingUtil;
    this.navigationModelBindingTargetUtil = navigationModelBindingTargetUtil;
  }

  public void setRoundEnvironment(RoundEnvironment roundEnv) {
    this.roundEnv = roundEnv;
  }

  public void parseDartModelAnnotatedFields(
      Map<TypeElement, NavigationModelBindingTarget> targetClassMap) {
    for (VariableElement element :
        ElementFilter.fieldsIn(roundEnv.getElementsAnnotatedWith(DartModel.class))) {
      try {
        parseNavigationModelField(element, targetClassMap);
      } catch (Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        loggingUtil.error(
            element,
            "Unable to generate extra binder when parsing @DartModel.\n\n%s",
            stackTrace.toString());
      }
    }
  }

  private void parseNavigationModelField(
      VariableElement element, Map<TypeElement, NavigationModelBindingTarget> targetClassMap) {
    final TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
    if (targetClassMap.containsKey(enclosingElement)) {
      loggingUtil.error(
          enclosingElement,
          "Component %s cannot bind more than one NavigationModel.",
          enclosingElement.getSimpleName());
      return;
    }

    // Verify common generated code restrictions.
    if (!isValidUsageOfNavigationModelField(element)) {
      return;
    }

    // Assemble information on the binding point.
    final NavigationModelBindingTarget navigationModelBindingTarget =
        navigationModelBindingTargetUtil.createTargetClass(enclosingElement, element);
    targetClassMap.put(enclosingElement, navigationModelBindingTarget);
  }

  private boolean isValidUsageOfNavigationModelField(VariableElement element) {
    final TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
    boolean valid = true;

    // Verify modifiers.
    Set<Modifier> modifiers = element.getModifiers();
    if (modifiers.contains(PRIVATE) || modifiers.contains(STATIC)) {
      loggingUtil.error(
          element,
          "@DartModel field must not be private or static. (%s.%s)",
          enclosingElement.getQualifiedName(),
          element.getSimpleName());
      valid = false;
    }

    return valid;
  }
}
