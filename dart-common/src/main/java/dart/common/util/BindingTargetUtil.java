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

import static dart.common.util.DartModelUtil.DART_MODEL_SUFFIX;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.util.ElementFilter.fieldsIn;
import static javax.lang.model.util.ElementFilter.methodsIn;

import dart.common.BindingTarget;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class BindingTargetUtil {

  public static final String INITIAL_STATE_METHOD = "getInitialState";
  public static final String BUNDLE_BUILDER_SUFFIX = "__IntentBuilder";

  private final CompilerUtil compilerUtil;
  private final BindExtraUtil bindExtraUtil;
  private final LoggingUtil loggingUtil;
  private final Elements elementUtils;
  private final Types typeUtils;

  public BindingTargetUtil(
      CompilerUtil compilerUtil,
      ProcessingEnvironment processingEnv,
      LoggingUtil loggingUtil,
      BindExtraUtil bindExtraUtil) {
    this.compilerUtil = compilerUtil;
    this.bindExtraUtil = bindExtraUtil;
    this.loggingUtil = loggingUtil;
    elementUtils = processingEnv.getElementUtils();
    typeUtils = processingEnv.getTypeUtils();
  }

  public BindingTarget createTargetClass(TypeElement typeElement) {
    final String classPackage = compilerUtil.getPackageName(typeElement);
    final String className = compilerUtil.getClassName(typeElement, classPackage);
    BindingTarget bindingTarget = new BindingTarget(classPackage, className);

    for (VariableElement field : fieldsIn(typeElement.getEnclosedElements())) {
      Set<Modifier> modifiers = field.getModifiers();
      // We omit FINAL STATIC fields: constants that should not be taken into account
      if (!modifiers.contains(FINAL) || !modifiers.contains(STATIC)) {
        bindExtraUtil.parseInjectExtra(field, bindingTarget);
      }
    }

    return bindingTarget;
  }

  public void createBindingTargetTrees(Map<TypeElement, BindingTarget> targetClassMap) {
    final Set<TypeElement> targetTypeElements = targetClassMap.keySet();
    for (TypeElement typeElement : targetTypeElements) {
      TypeElement parentTypeElement = compilerUtil.findParent(typeElement, targetTypeElements);
      if (parentTypeElement != null) {
        final BindingTarget target = targetClassMap.get(typeElement);
        final BindingTarget parentTarget = targetClassMap.get(parentTypeElement);
        target.parentPackage = parentTarget.classPackage;
        target.parentClass = parentTarget.className;
        parentTarget.addChild(typeElement);
      }
    }
    checkForParentsOutside(targetClassMap);
  }

  public void addClosestRequiredAncestorForTargets(Map<TypeElement, BindingTarget> targetClassMap) {
    for (Map.Entry<TypeElement, BindingTarget> target : targetClassMap.entrySet()) {
      final TypeElement element = target.getKey();
      final BindingTarget bindingTarget = target.getValue();
      if (bindingTarget.topLevel) {
        // check if parent is outside the current module
        if (bindingTarget.parentPackage != null) {
          setClosestRequiredAncestor(bindingTarget, getIntentBuilder(element.getSuperclass()));
        }
        spreadClosestRequiredAncestorToChildren(targetClassMap, bindingTarget);
      }
    }
  }

  private void checkForParentsOutside(Map<TypeElement, BindingTarget> targetClassMap) {
    for (Map.Entry<TypeElement, BindingTarget> target : targetClassMap.entrySet()) {
      final TypeElement element = target.getKey();
      final BindingTarget bindingTarget = target.getValue();
      // root inside module
      if (bindingTarget.parentPackage == null) {
        bindingTarget.topLevel = true;
        final TypeMirror superType = element.getSuperclass();
        // has superclass
        if (!typeUtils.isSameType(
            superType, elementUtils.getTypeElement("java.lang.Object").asType())) {

          final TypeElement superTypeElement = (TypeElement) ((DeclaredType) superType).asElement();
          // DartModel contains a parent outside the module that does not have a IntentBuilder:
          // Parent is not a DartModel
          if (getIntentBuilder(superType) == null) {
            loggingUtil.error(
                element,
                "@DartModel %s parent does not have an IntentBuilder. Is %s a @DartModel?",
                element.getQualifiedName(),
                superTypeElement.getQualifiedName());
            return;
          }
          bindingTarget.parentPackage = compilerUtil.getPackageName(superTypeElement);
          final String nmClass =
              compilerUtil.getClassName(superTypeElement, bindingTarget.parentPackage);
          bindingTarget.parentClass = nmClass.substring(0, nmClass.indexOf(DART_MODEL_SUFFIX));
        }
      }
    }
  }

  private void setClosestRequiredAncestor(
      BindingTarget bindingTarget, TypeElement superIntentBuilder) {
    for (ExecutableElement method : methodsIn(superIntentBuilder.getEnclosedElements())) {
      if (method.getSimpleName().contentEquals(INITIAL_STATE_METHOD)) {
        final TypeMirror returnTypeMirror = method.getReturnType();
        if (compilerUtil.isAssignable(returnTypeMirror, "dart.henson.AllRequiredSetState")) {
          return;
        }
        final Element reqElement = ((DeclaredType) typeUtils.erasure(returnTypeMirror)).asElement();
        final TypeElement intentBuilderTypeElement = (TypeElement) reqElement.getEnclosingElement();
        bindingTarget.closestRequiredAncestorPackage =
            compilerUtil.getPackageName(intentBuilderTypeElement);
        final String intentBuilderClass =
            compilerUtil.getClassName(
                intentBuilderTypeElement, bindingTarget.closestRequiredAncestorPackage);
        bindingTarget.closestRequiredAncestorClass =
            intentBuilderClass.substring(0, intentBuilderClass.indexOf(BUNDLE_BUILDER_SUFFIX));
      }
    }
  }

  private void spreadClosestRequiredAncestorToChildren(
      Map<TypeElement, BindingTarget> targetClassMap, BindingTarget bindingTarget) {
    for (TypeElement child : bindingTarget.childClasses) {
      final BindingTarget childTarget = targetClassMap.get(child);
      if (bindingTarget.hasRequiredFields) {
        childTarget.closestRequiredAncestorPackage = bindingTarget.classPackage;
        childTarget.closestRequiredAncestorClass = bindingTarget.className;
      } else {
        childTarget.closestRequiredAncestorPackage = bindingTarget.closestRequiredAncestorPackage;
        childTarget.closestRequiredAncestorClass = bindingTarget.closestRequiredAncestorClass;
      }
      spreadClosestRequiredAncestorToChildren(targetClassMap, childTarget);
    }
  }

  private TypeElement getIntentBuilder(TypeMirror dartModelMirror) {
    final TypeElement dartModel = (TypeElement) ((DeclaredType) dartModelMirror).asElement();
    final String intentBuilderFQN = dartModel.getQualifiedName().toString() + BUNDLE_BUILDER_SUFFIX;
    return elementUtils.getTypeElement(intentBuilderFQN);
  }
}
