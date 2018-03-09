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

public class NavigationModelBindingTarget {
  public final String classPackage;
  public final String className;
  public final String navigationModelPackage;
  public final String navigationModelClass;
  public String navigationModelFieldName;
  public String parentPackage;
  public String parentClass;

  public NavigationModelBindingTarget(
      String classPackage,
      String className,
      String navigationModelPackage,
      String navigationModelClass,
      String navigationModelFieldName) {
    this.classPackage = classPackage;
    this.className = className;
    this.navigationModelPackage = navigationModelPackage;
    this.navigationModelClass = navigationModelClass;
    this.navigationModelFieldName = navigationModelFieldName;
  }

  public String getFQN() {
    return classPackage + "." + className;
  }

  public String getParentFQN() {
    return parentPackage + "." + parentClass;
  }
}
