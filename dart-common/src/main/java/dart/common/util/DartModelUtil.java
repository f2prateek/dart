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

import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.PACKAGE;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

import dart.DartModel;
import dart.common.ExtraBindingTarget;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

public class DartModelUtil {

  public static final String DART_MODEL_SUFFIX = "NavigationModel";

  private final LoggingUtil loggingUtil;
  private final ExtraBindingTargetUtil extraBindingTargetUtil;
  private final CompilerUtil compilerUtil;

  private RoundEnvironment roundEnv;

  public DartModelUtil(
      LoggingUtil loggingUtil,
      ExtraBindingTargetUtil extraBindingTargetUtil,
      CompilerUtil compilerUtil) {
    this.loggingUtil = loggingUtil;
    this.extraBindingTargetUtil = extraBindingTargetUtil;
    this.compilerUtil = compilerUtil;
  }

  public void setRoundEnvironment(RoundEnvironment roundEnv) {
    this.roundEnv = roundEnv;
  }

  public void parseDartModelAnnotatedTypes(Map<TypeElement, ExtraBindingTarget> targetClassMap) {
    for (TypeElement element :
        ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(DartModel.class))) {
      try {
        parseDartModel(element, targetClassMap);
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

  boolean isValidUsageOfDartModel(TypeElement element) {
    boolean valid = true;

    // Verify modifiers.
    Set<Modifier> modifiers = element.getModifiers();
    if (modifiers.contains(PRIVATE) || modifiers.contains(STATIC) || modifiers.contains(ABSTRACT)) {
      loggingUtil.error(
          element,
          "DartModel class %s must not be private, static or abstract.",
          element.getSimpleName());
      valid = false;
    }

    // Verify type.
    if (element.getKind() != CLASS) {
      loggingUtil.error(element, "DartModel element %s must be a class.", element.getSimpleName());
      valid = false;
    }

    // Verify containing type.
    if (element.getEnclosingElement() == null
        || element.getEnclosingElement().getKind() != PACKAGE) {
      loggingUtil.error(
          element, "DartModel class %s must be a top level class.", element.getSimpleName());
      valid = false;
    }

    // Verify Dart Model suffix.
    final String classPackage = compilerUtil.getPackageName(element);
    final String className = compilerUtil.getClassName(element, classPackage);
    if (!className.endsWith(DART_MODEL_SUFFIX)) {
      loggingUtil.error(
          element,
          "DartModel class %s does not follow the naming convention: my.package.TargetComponentNavigationModel.",
          element.getSimpleName());
      valid = false;
    }

    return valid;
  }

  private void parseDartModel(
      TypeElement element, Map<TypeElement, ExtraBindingTarget> targetClassMap) {
    // The ExtraBindingTarget was already created, @BindExtra processed first
    if (targetClassMap.containsKey(element)) {
      return;
    }

    // Verify common generated code restrictions.
    if (!isValidUsageOfDartModel(element)) {
      return;
    }

    // Assemble information on the binding point.
    final ExtraBindingTarget navigationModelTarget =
        extraBindingTargetUtil.createTargetClass(element);
    targetClassMap.put(element, navigationModelTarget);
  }
}
