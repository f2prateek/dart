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

import dart.common.NavigationModelBindingTarget;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

public class NavigationModelBindingTargetUtil {

  public static final String NAVIGATION_MODEL_BINDER_SUFFIX = "__NavigationModelBinder";

  private final CompilerUtil compilerUtil;
  private final Elements elementUtils;

  public NavigationModelBindingTargetUtil(
      CompilerUtil compilerUtil, ProcessingEnvironment processingEnv) {
    this.compilerUtil = compilerUtil;
    elementUtils = processingEnv.getElementUtils();
  }

  public NavigationModelBindingTarget createTargetClass(
      TypeElement classElement, VariableElement navigationModelVariableElement) {
    final String classPackage = compilerUtil.getPackageName(classElement);
    final String className = compilerUtil.getClassName(classElement, classPackage);
    final TypeElement navigationModelElement =
        (TypeElement) ((DeclaredType) navigationModelVariableElement.asType()).asElement();
    final String navigationModelPackage = compilerUtil.getPackageName(navigationModelElement);
    final String navigationModelClass =
        compilerUtil.getClassName(navigationModelElement, navigationModelPackage);
    final String navigationModelFieldName =
        navigationModelVariableElement.getSimpleName().toString();
    return new NavigationModelBindingTarget(
        classPackage,
        className,
        navigationModelPackage,
        navigationModelClass,
        navigationModelFieldName);
  }

  public void createBindingTargetTrees(
      Map<TypeElement, NavigationModelBindingTarget> targetClassMap) {
    final Set<TypeElement> targetTypeElements = targetClassMap.keySet();
    for (TypeElement typeElement : targetTypeElements) {
      TypeElement parentTypeElement = compilerUtil.findParent(typeElement, targetTypeElements);
      if (parentTypeElement != null) {
        final NavigationModelBindingTarget target = targetClassMap.get(typeElement);
        final NavigationModelBindingTarget parentTarget = targetClassMap.get(parentTypeElement);
        target.parentPackage = parentTarget.classPackage;
        target.parentClass = parentTarget.className;
      }
    }
    checkForParentsOutside(targetClassMap);
  }

  private void checkForParentsOutside(
      Map<TypeElement, NavigationModelBindingTarget> targetClassMap) {
    for (Map.Entry<TypeElement, NavigationModelBindingTarget> target : targetClassMap.entrySet()) {
      final TypeElement element = target.getKey();
      final NavigationModelBindingTarget navigationModelBindingTarget = target.getValue();
      // root inside module
      if (navigationModelBindingTarget.parentPackage == null) {
        TypeElement ancestorElement = element;
        while (true) {
          final TypeMirror superType = ancestorElement.getSuperclass();
          if (superType.getKind() == TypeKind.NONE) {
            // Got to the oldest ancestor and none contains NavigationModel field
            return;
          }
          ancestorElement = (TypeElement) ((DeclaredType) superType).asElement();
          // ancestor contains a NavigationModel field
          if (getNavigationModelBinder(ancestorElement) != null) {
            navigationModelBindingTarget.parentPackage =
                compilerUtil.getPackageName(ancestorElement);
            navigationModelBindingTarget.parentClass =
                compilerUtil.getClassName(
                    ancestorElement, navigationModelBindingTarget.parentPackage);
            return;
          }
        }
      }
    }
  }

  private TypeElement getNavigationModelBinder(TypeElement element) {
    final String modelFQN = element.getQualifiedName().toString();
    return elementUtils.getTypeElement(modelFQN + NAVIGATION_MODEL_BINDER_SUFFIX);
  }
}
