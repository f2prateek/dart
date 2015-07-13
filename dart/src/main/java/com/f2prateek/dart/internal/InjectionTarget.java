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

package com.f2prateek.dart.internal;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.lang.model.type.TypeMirror;

final class InjectionTarget {
  final Map<String, ExtraInjection> injectionMap = new LinkedHashMap<String, ExtraInjection>();
  final String classPackage;
  final String className;
  final String targetClass;
  String parentTarget;

  InjectionTarget(String classPackage, String className, String targetClass) {
    this.classPackage = classPackage;
    this.className = className;
    this.targetClass = targetClass;
  }

  void addField(String key, String name, TypeMirror type, boolean required, boolean parcel) {
    getOrCreateExtraBinding(key).addFieldBinding(new FieldBinding(name, type, required, parcel));
  }

  void setParentTarget(String parentTarget) {
    this.parentTarget = parentTarget;
  }

  private ExtraInjection getOrCreateExtraBinding(String key) {
    ExtraInjection extraInjection = injectionMap.get(key);
    if (extraInjection == null) {
      extraInjection = new ExtraInjection(key);
      injectionMap.put(key, extraInjection);
    }
    return extraInjection;
  }

  String getFqcn() {
    return classPackage + "." + className;
  }
}
