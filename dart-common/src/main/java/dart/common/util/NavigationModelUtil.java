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

import static javax.lang.model.element.ElementKind.PACKAGE;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

import dart.DartModel;
import dart.common.InjectionTarget;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;

public class NavigationModelUtil {

  private final LoggingUtil loggingUtil;
  private final InjectionTargetUtil injectionTargetUtil;
  private final Types typeUtils;

  private RoundEnvironment roundEnv;

  public NavigationModelUtil(
      LoggingUtil loggingUtil,
      InjectionTargetUtil injectionTargetUtil,
      ProcessingEnvironment processingEnv) {
    this.loggingUtil = loggingUtil;
    this.injectionTargetUtil = injectionTargetUtil;
    typeUtils = processingEnv.getTypeUtils();
  }

  public void setRoundEnvironment(RoundEnvironment roundEnv) {
    this.roundEnv = roundEnv;
  }

  public void parseNavigationModelAnnotatedElements(
      Map<TypeElement, InjectionTarget> targetClassMap) {
    for (Element element : roundEnv.getElementsAnnotatedWith(DartModel.class)) {
      try {
        parseNavigationModel((TypeElement) element, targetClassMap);
      } catch (Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        loggingUtil.error(
            element,
            "Unable to generate extra injector when parsing @DartModel.\n\n%s",
            stackTrace.toString());
      }
    }
  }

  private void parseNavigationModel(
      TypeElement element, Map<TypeElement, InjectionTarget> targetClassMap) {
    // Verify common generated code restrictions.
    if (!isValidUsageOfNavigationModel(element)) {
      return;
    }

    // Valid annotation value
    final String annotationValue = element.getAnnotation(DartModel.class).value();
    if (!StringUtil.isValidFqcn(annotationValue)) {
      throw new IllegalArgumentException(
          "Key has to be a full qualified class name. "
              + "https://docs.oracle.com/cd/E19798-01/821-1841/bnbuk/index.html");
    }

    // Assemble information on the injection point.
    InjectionTarget navigationModelTarget =
        injectionTargetUtil.getOrCreateTargetClass(targetClassMap, element);
    navigationModelTarget.setTargetClass(annotationValue);
  }

  private boolean isValidUsageOfNavigationModel(Element element) {
    boolean valid = true;

    // Verify modifiers.
    Set<Modifier> modifiers = element.getModifiers();
    if (modifiers.contains(PRIVATE) || modifiers.contains(STATIC) || modifiers.contains(ABSTRACT)) {
      loggingUtil.error(
          element,
          "@DartModel class %s must not be private, static or abstract.",
          element.getSimpleName());
      valid = false;
    }

    // Verify containing type.
    if (element.getEnclosingElement() == null
        || element.getEnclosingElement().getKind() != PACKAGE) {
      loggingUtil.error(
          element, "@DartModel class %s must be a top level class.", element.getSimpleName());
      valid = false;
    }

    return valid;
  }
}
