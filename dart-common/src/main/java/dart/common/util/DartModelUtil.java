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
import static javax.lang.model.util.ElementFilter.fieldsIn;
import static javax.lang.model.util.ElementFilter.typesIn;

import dart.DartModel;
import dart.common.BindingTarget;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class DartModelUtil {

  public static final String DART_MODEL_SUFFIX = "NavigationModel";

  private final LoggingUtil loggingUtil;
  private final BindingTargetUtil bindingTargetUtil;
  private final CompilerUtil compilerUtil;

  private RoundEnvironment roundEnv;

  public DartModelUtil(
      LoggingUtil loggingUtil, BindingTargetUtil bindingTargetUtil, CompilerUtil compilerUtil) {
    this.loggingUtil = loggingUtil;
    this.bindingTargetUtil = bindingTargetUtil;
    this.compilerUtil = compilerUtil;
  }

  public void setRoundEnvironment(RoundEnvironment roundEnv) {
    this.roundEnv = roundEnv;
  }

  public void parseDartModelAnnotatedTypes(Map<TypeElement, BindingTarget> targetClassMap) {
    for (Element element : typesIn(roundEnv.getElementsAnnotatedWith(DartModel.class))) {
      try {
        parseTypesForDartModel((TypeElement) element, targetClassMap);
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

  public void parseDartModelAnnotatedFields(Map<TypeElement, BindingTarget> targetClassMap) {
    for (Element element : fieldsIn(roundEnv.getElementsAnnotatedWith(DartModel.class))) {
      try {
        parseFieldsForDartModel(element, targetClassMap);
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

  boolean isValidUsageOfDartModel(Element element) {
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
    if (element.getKind() == CLASS) {
      // Verify containing type.
      if (element.getEnclosingElement() == null
          || element.getEnclosingElement().getKind() != PACKAGE) {
        loggingUtil.error(
            element, "DartModel class %s must be a top level class.", element.getSimpleName());
        valid = false;
      }

      // Verify Dart Model suffix.
      final String classPackage = compilerUtil.getPackageName((TypeElement) element);
      final String className = compilerUtil.getClassName((TypeElement)element, classPackage);
      if (!className.endsWith(DART_MODEL_SUFFIX)) {
        loggingUtil.error(
                element,
                "DartModel class %s does not follow the naming convention: my.package.TargetComponentNavigationModel.",
                element.getSimpleName());
        valid = false;
      }
    } else if(element.getKind() != ElementKind.FIELD) {
      loggingUtil.error(element, "Invalid usage of @DartModel on %s. It only apply to classes or fields.", element.getSimpleName());
      valid = false;
    }



    return valid;
  }

  private void parseTypesForDartModel(TypeElement element, Map<TypeElement, BindingTarget> targetClassMap) {
    // The BindingTarget was already created, @BindExtra processed first
    if (targetClassMap.containsKey(element)) {
      return;
    }

    // Verify common generated code restrictions.
    if (!isValidUsageOfDartModel(element)) {
      return;
    }

    // Assemble information on the binding point.
    final BindingTarget navigationModelTarget = bindingTargetUtil.createTargetForType(element);
    targetClassMap.put(element, navigationModelTarget);
  }

  private void parseFieldsForDartModel(Element fieldElement, Map<TypeElement, BindingTarget> targetClassMap) {
    TypeElement element = (TypeElement) fieldElement.getEnclosingElement();
    // The BindingTarget was already created, @BindExtra processed first
    if (targetClassMap.containsKey(element)) {
      return;
    }

    // Verify common generated code restrictions.
    if (!isValidUsageOfDartModel(fieldElement)) {
      return;
    }

    // Assemble information on the binding point.
    final BindingTarget navigationModelTarget = bindingTargetUtil.createTargetForField(element, fieldElement);
    targetClassMap.put(element, navigationModelTarget);
  }
}
