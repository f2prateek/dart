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

import dart.common.InjectionTarget;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class InjectionTargetUtil {

  private final CompilerUtil compilerUtil;

  public InjectionTargetUtil(CompilerUtil compilerUtil) {
    this.compilerUtil = compilerUtil;
  }

  public InjectionTarget getOrCreateTargetClass(
      Map<TypeElement, InjectionTarget> targetClassMap, TypeElement typeElement) {
    InjectionTarget bindingTarget = targetClassMap.get(typeElement);
    if (bindingTarget == null) {
      final String targetType = typeElement.getQualifiedName().toString();
      final String classPackage = compilerUtil.getPackageName(typeElement);
      final String className = compilerUtil.getClassName(typeElement, classPackage);
      final boolean isAbstractType = typeElement.getModifiers().contains(Modifier.ABSTRACT);

      bindingTarget = new InjectionTarget(classPackage, className, targetType, isAbstractType);
      targetClassMap.put(typeElement, bindingTarget);
    }
    return bindingTarget;
  }

  public void createInjectionTargetTree(Map<TypeElement, InjectionTarget> targetClassMap) {
    final Set<TypeElement> targetTypeElements = targetClassMap.keySet();
    for (TypeElement typeElement : targetTypeElements) {
      TypeElement parentTypeElement = compilerUtil.findParent(typeElement, targetTypeElements);
      if (parentTypeElement != null) {
        String parentPackageName = compilerUtil.getPackageName(parentTypeElement);
        targetClassMap.get(typeElement).parentClassFqcn =
            parentPackageName
                + "."
                + compilerUtil.getClassName(parentTypeElement, parentPackageName);
        targetClassMap.get(parentTypeElement).addChild(typeElement);
      }
    }
  }

  public void inheritExtraInjections(Map<TypeElement, InjectionTarget> targetClassMap) {
    for (InjectionTarget bindingTarget : targetClassMap.values()) {
      // We start inheriting from the tree roots
      if (bindingTarget.parentClassFqcn == null) {
        addExtraInjectionsToDescendants(targetClassMap, bindingTarget);
      }
    }
  }

  public void filterNavigationModels(Map<TypeElement, InjectionTarget> targetClassMap) {
    final Iterator<Map.Entry<TypeElement, InjectionTarget>> iterator =
        targetClassMap.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<TypeElement, InjectionTarget> entry = iterator.next();
      if (!entry.getValue().isNavigationModel) {
        iterator.remove();
      }
    }
  }

  private void addExtraInjectionsToDescendants(
      Map<TypeElement, InjectionTarget> targetClassMap, InjectionTarget bindingTarget) {
    for (TypeElement child : bindingTarget.childClasses) {
      final InjectionTarget childInjectionTarget = targetClassMap.get(child);
      childInjectionTarget.bindingMap.putAll(bindingTarget.bindingMap);
      addExtraInjectionsToDescendants(targetClassMap, childInjectionTarget);
    }
  }
}
