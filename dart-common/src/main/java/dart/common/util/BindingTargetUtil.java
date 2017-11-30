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

import dart.common.BindingTarget;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import static javax.lang.model.util.ElementFilter.fieldsIn;

public class BindingTargetUtil {

  private final CompilerUtil compilerUtil;
  private final BindExtraUtil bindExtraUtil;

  public BindingTargetUtil(CompilerUtil compilerUtil, BindExtraUtil bindExtraUtil) {
    this.compilerUtil = compilerUtil;
    this.bindExtraUtil = bindExtraUtil;
  }

  public BindingTarget createTargetClass(TypeElement typeElement) {
    final String targetType = typeElement.getQualifiedName().toString();
    final String classPackage = compilerUtil.getPackageName(typeElement);
    final String className = compilerUtil.getClassName(typeElement, classPackage);
    BindingTarget bindingTarget = new BindingTarget(classPackage, className, targetType);

    for (VariableElement field : fieldsIn(typeElement.getEnclosedElements())) {
      bindExtraUtil.parseInjectExtra(field, bindingTarget);
    }

    return bindingTarget;
  }

  public void createInjectionTargetTree(Map<TypeElement, BindingTarget> targetClassMap) {
    final Set<TypeElement> targetTypeElements = targetClassMap.keySet();
    for (TypeElement typeElement : targetTypeElements) {
      TypeElement parentTypeElement = compilerUtil.findParent(typeElement, targetTypeElements);
      if (parentTypeElement != null) {
        String parentPackageName = compilerUtil.getPackageName(parentTypeElement);
        targetClassMap.get(typeElement).parentClassFqcn =
            parentPackageName + "." +
                compilerUtil.getClassName(parentTypeElement, parentPackageName);
        targetClassMap.get(parentTypeElement).addChild(typeElement);
      }
    }
  }
}
