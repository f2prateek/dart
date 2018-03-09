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

import static dart.common.util.DartModelUtil.DART_MODEL_SUFFIX;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public class ExtraBindingTarget {
  public final Map<String, ExtraInjection> bindingMap = new LinkedHashMap<>();
  public final String classPackage;
  public final String className;
  public String parentPackage;
  public String parentClass;
  // Closest ancestor with required fields
  // we could have made a simpler model and just remember
  // the name of the required state but we decided to make a richer model
  // and to remember the name of the full ancestor class that contains required fields.
  // Closest here means first one in the hierarchy, we discover it without scanning the hierarchy,
  // just having a look at the parent class is enough to retrieve this information.
  public String closestRequiredAncestorPackage;
  public String closestRequiredAncestorClass;

  public boolean hasRequiredFields;
  public boolean topLevel;
  public List<TypeElement> childClasses;

  public ExtraBindingTarget(String classPackage, String className) {
    this.classPackage = classPackage;
    this.className = className.substring(0, className.indexOf(DART_MODEL_SUFFIX));
    this.childClasses = new ArrayList<>();
    this.topLevel = false;
  }

  public String getFQN() {
    return classPackage + "." + className;
  }

  public String getParentFQN() {
    return parentPackage + "." + parentClass;
  }

  public void addField(String key, String name, TypeMirror type, boolean required, boolean parcel) {
    ExtraInjection extraInjection = bindingMap.get(key);
    if (extraInjection == null) {
      extraInjection = new ExtraInjection(key);
      bindingMap.put(key, extraInjection);
    }
    extraInjection.addFieldBinding(new FieldBinding(name, type, required, parcel));
    hasRequiredFields = hasRequiredFields || required;
  }

  public void addChild(TypeElement typeElement) {
    childClasses.add(typeElement);
  }
}
