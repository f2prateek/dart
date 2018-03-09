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
import static javax.lang.model.util.ElementFilter.methodsIn;

import dart.common.ExtraBindingTarget;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class ExtraBindingTargetUtil {

  public static final String NEXT_STATE_METHOD = "getNextState";
  public static final String BUNDLE_BUILDER_SUFFIX = "__IntentBuilder";

  private final CompilerUtil compilerUtil;
  private final LoggingUtil loggingUtil;
  private final Elements elementUtils;
  private final Types typeUtils;

  public ExtraBindingTargetUtil(
      CompilerUtil compilerUtil, ProcessingEnvironment processingEnv, LoggingUtil loggingUtil) {
    this.compilerUtil = compilerUtil;
    this.loggingUtil = loggingUtil;
    elementUtils = processingEnv.getElementUtils();
    typeUtils = processingEnv.getTypeUtils();
  }

  public ExtraBindingTarget createTargetClass(TypeElement typeElement) {
    final String classPackage = compilerUtil.getPackageName(typeElement);
    final String className = compilerUtil.getClassName(typeElement, classPackage);
    return new ExtraBindingTarget(classPackage, className);
  }

  public void createBindingTargetTrees(Map<TypeElement, ExtraBindingTarget> targetClassMap) {
    final Set<TypeElement> targetTypeElements = targetClassMap.keySet();
    for (TypeElement typeElement : targetTypeElements) {
      TypeElement parentTypeElement = compilerUtil.findParent(typeElement, targetTypeElements);
      if (parentTypeElement != null) {
        final ExtraBindingTarget target = targetClassMap.get(typeElement);
        final ExtraBindingTarget parentTarget = targetClassMap.get(parentTypeElement);
        target.parentPackage = parentTarget.classPackage;
        target.parentClass = parentTarget.className;
        parentTarget.addChild(typeElement);
      }
    }
    checkForParentsOutside(targetClassMap);
  }

  public void addClosestRequiredAncestorForTargets(
      Map<TypeElement, ExtraBindingTarget> targetClassMap) {
    for (Map.Entry<TypeElement, ExtraBindingTarget> target : targetClassMap.entrySet()) {
      final TypeElement element = target.getKey();
      final ExtraBindingTarget extraBindingTarget = target.getValue();
      if (extraBindingTarget.topLevel) {
        // check if parent is outside the current module
        if (extraBindingTarget.parentPackage != null) {
          setClosestRequiredAncestor(extraBindingTarget, getIntentBuilder(element.getSuperclass()));
        }
        spreadClosestRequiredAncestorToChildren(targetClassMap, extraBindingTarget);
      }
    }
  }

  private void checkForParentsOutside(Map<TypeElement, ExtraBindingTarget> targetClassMap) {
    for (Map.Entry<TypeElement, ExtraBindingTarget> target : targetClassMap.entrySet()) {
      final TypeElement element = target.getKey();
      final ExtraBindingTarget extraBindingTarget = target.getValue();
      // root inside module
      if (extraBindingTarget.parentPackage == null) {
        extraBindingTarget.topLevel = true;
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
                "DartModel %s parent does not have an IntentBuilder. Is %s "
                    + "annotated with @DartModel or contains @BindExtra fields?",
                element.getQualifiedName(),
                superTypeElement.getQualifiedName());
            return;
          }
          extraBindingTarget.parentPackage = compilerUtil.getPackageName(superTypeElement);
          final String nmClass =
              compilerUtil.getClassName(superTypeElement, extraBindingTarget.parentPackage);
          extraBindingTarget.parentClass = nmClass.substring(0, nmClass.indexOf(DART_MODEL_SUFFIX));
        }
      }
    }
  }

  private void setClosestRequiredAncestor(
      ExtraBindingTarget extraBindingTarget, TypeElement superIntentBuilder) {
    for (ExecutableElement method : methodsIn(superIntentBuilder.getEnclosedElements())) {
      if (method.getSimpleName().contentEquals(NEXT_STATE_METHOD)) {
        final TypeMirror returnTypeMirror = method.getReturnType();
        if (compilerUtil.isAssignable(returnTypeMirror, "dart.henson.AllRequiredSetState")) {
          return;
        }
        final Element reqElement = ((DeclaredType) typeUtils.erasure(returnTypeMirror)).asElement();
        final TypeElement intentBuilderTypeElement = (TypeElement) reqElement.getEnclosingElement();
        extraBindingTarget.closestRequiredAncestorPackage =
            compilerUtil.getPackageName(intentBuilderTypeElement);
        final String intentBuilderClass =
            compilerUtil.getClassName(
                intentBuilderTypeElement, extraBindingTarget.closestRequiredAncestorPackage);
        extraBindingTarget.closestRequiredAncestorClass =
            intentBuilderClass.substring(0, intentBuilderClass.indexOf(BUNDLE_BUILDER_SUFFIX));
      }
    }
  }

  private void spreadClosestRequiredAncestorToChildren(
      Map<TypeElement, ExtraBindingTarget> targetClassMap, ExtraBindingTarget extraBindingTarget) {
    for (TypeElement child : extraBindingTarget.childClasses) {
      final ExtraBindingTarget childTarget = targetClassMap.get(child);
      if (extraBindingTarget.hasRequiredFields) {
        childTarget.closestRequiredAncestorPackage = extraBindingTarget.classPackage;
        childTarget.closestRequiredAncestorClass = extraBindingTarget.className;
      } else {
        childTarget.closestRequiredAncestorPackage =
            extraBindingTarget.closestRequiredAncestorPackage;
        childTarget.closestRequiredAncestorClass = extraBindingTarget.closestRequiredAncestorClass;
      }
      spreadClosestRequiredAncestorToChildren(targetClassMap, childTarget);
    }
  }

  private TypeElement getIntentBuilder(TypeMirror dartModelMirror) {
    final TypeElement dartModel = (TypeElement) ((DeclaredType) dartModelMirror).asElement();
    final String modelFQN = dartModel.getQualifiedName().toString();
    final int indexOfSuffix = modelFQN.indexOf(DART_MODEL_SUFFIX);
    if (indexOfSuffix == -1) {
      return null;
    }
    final String targetComponentFQN = modelFQN.substring(0, indexOfSuffix);
    return elementUtils.getTypeElement(targetComponentFQN + BUNDLE_BUILDER_SUFFIX);
  }
}
