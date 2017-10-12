package com.f2prateek.dart.common.util;

import com.f2prateek.dart.common.InjectionTarget;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class InjectionTargetUtil {

  private final CompilerUtil compilerUtil;

  public InjectionTargetUtil(CompilerUtil compilerUtil) {
    this.compilerUtil = compilerUtil;
  }

  public InjectionTarget getOrCreateTargetClass(Map<TypeElement, InjectionTarget> targetClassMap,
      TypeElement typeElement) {
    InjectionTarget injectionTarget = targetClassMap.get(typeElement);
    if (injectionTarget == null) {
      final String targetType = typeElement.getQualifiedName().toString();
      final String classPackage = compilerUtil.getPackageName(typeElement);
      final String className = compilerUtil.getClassName(typeElement, classPackage);
      final boolean isAbstractType = typeElement.getModifiers().contains(Modifier.ABSTRACT);

      injectionTarget = new InjectionTarget(classPackage, className, targetType, isAbstractType);
      targetClassMap.put(typeElement, injectionTarget);
    }
    return injectionTarget;
  }

  public void createInjectionTargetTree(Map<TypeElement, InjectionTarget> targetClassMap) {
    final Set<TypeElement> targetTypeElements = targetClassMap.keySet();
    for (TypeElement typeElement : targetTypeElements) {
      TypeElement parentTypeElement = compilerUtil.findParent(typeElement, targetTypeElements);
      if (parentTypeElement != null) {
        String parentPackageName = compilerUtil.getPackageName(parentTypeElement);
        targetClassMap.get(typeElement).parentClassFqcn =
            parentPackageName + "." + compilerUtil.getClassName(parentTypeElement,
                parentPackageName);
        targetClassMap.get(parentTypeElement).addChild(typeElement);
      }
    }
  }

  public void inheritExtraInjections(Map<TypeElement, InjectionTarget> targetClassMap) {
    for (InjectionTarget injectionTarget : targetClassMap.values()) {
      // We start inheriting from the tree roots
      if (injectionTarget.parentClassFqcn == null) {
        addExtraInjectionsToDescendants(targetClassMap, injectionTarget);
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

  private void addExtraInjectionsToDescendants(Map<TypeElement, InjectionTarget> targetClassMap,
      InjectionTarget injectionTarget) {
    for (TypeElement child : injectionTarget.childClasses) {
      final InjectionTarget childInjectionTarget = targetClassMap.get(child);
      childInjectionTarget.injectionMap.putAll(injectionTarget.injectionMap);
      addExtraInjectionsToDescendants(targetClassMap, childInjectionTarget);
    }
  }
}
