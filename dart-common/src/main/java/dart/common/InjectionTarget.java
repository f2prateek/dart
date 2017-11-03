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

package dart.common;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public class InjectionTarget {
  public final Map<String, ExtraInjection> injectionMap = new LinkedHashMap<>();
  public final String classPackage;
  public final String className;
  public final String classFqcnCanonical; // Canonical: my.package.class.innerclass
  public final boolean isAbstractClass;
  public String parentClassFqcn; // Non-canonical: my.package.class$innerclass
  public List<TypeElement> childClasses;
  public boolean isNavigationModel;
  public String targetClassFqcn; // Non-canonical: my.package.class$innerclass
  public String targetClassName;

  public InjectionTarget(
      String classPackage, String className, String classFqcnCanonical, boolean isAbstractClass) {
    this.classPackage = classPackage;
    this.className = className;
    this.classFqcnCanonical = classFqcnCanonical;
    this.isAbstractClass = isAbstractClass;
    childClasses = new ArrayList<>();
  }

  public void addField(String key, String name, TypeMirror type, boolean required, boolean parcel) {
    ExtraInjection extraInjection = injectionMap.get(key);
    if (extraInjection == null) {
      extraInjection = new ExtraInjection(key);
      injectionMap.put(key, extraInjection);
    }
    extraInjection.addFieldBinding(new FieldBinding(name, type, required, parcel));
  }

  public void setTargetClass(String targetFqcnClass) {
    this.targetClassFqcn = targetFqcnClass;
    targetClassName = targetFqcnClass.substring(targetFqcnClass.lastIndexOf('.') + 1);
    isNavigationModel = true;
  }

  public void addChild(TypeElement typeElement) {
    childClasses.add(typeElement);
  }
}
