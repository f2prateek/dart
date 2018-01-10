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

package dart.henson.plugin.internal;

import static java.lang.String.format;

import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.logging.Logger;

public class DependencyManager {

  private Project project;
  private Logger logger;

  public DependencyManager(Project project, Logger logger) {
    this.project = project;
    this.logger = logger;
  }

  public void addDartAndHensonDependenciesToVariantConfigurations(String dartVersionName) {
    DependencyHandler dependencies = project.getDependencies();
    dependencies.add("implementation", format("com.f2prateek.dart:henson:%s", dartVersionName));
  }
}
