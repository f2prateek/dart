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

import java.util.HashMap;
import java.util.Map;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.logging.Logger;

public class DependencyManager {

  private Project project;
  private Logger logger;

  public DependencyManager(Project project, Logger logger) {
    this.project = project;
    this.logger = logger;
  }

  public void addDartAndHensonDependenciesToNavigationConfigurations(
      String prefix, String dartVersionName) {
    DependencyHandler dependencies = project.getDependencies();
    String compileOnly = format("%sNavigationCompileOnly", prefix);
    String processors = format("%sNavigationAnnotationProcessor", prefix);
    String apiRuntime = format("%sNavigationApi", prefix);

    String android = "com.google.android:android:4.1.1.4";
    String dartRuntime = format("com.f2prateek.dart:dart:%s", dartVersionName);
    String hensonRuntime = format("com.f2prateek.dart:henson:%s", dartVersionName);
    String hensonProcessor = format("com.f2prateek.dart:henson-processor:%s", dartVersionName);
    String dartProcessor = format("com.f2prateek.dart:dart-processor:%s", dartVersionName);
    String dartAnnotations = format("com.f2prateek.dart:dart-annotations:%s", dartVersionName);

    dependencies.add(compileOnly, android);
    dependencies.add(compileOnly, dartRuntime);
    dependencies.add(compileOnly, hensonRuntime);
    dependencies.add(processors, hensonProcessor);
    dependencies.add(processors, dartProcessor);
    dependencies.add(apiRuntime, dartAnnotations);
  }

  public void addDartAndHensonDependenciesToVariantConfigurations(String dartVersionName) {
    DependencyHandler dependencies = project.getDependencies();
    dependencies.add("implementation", format("com.f2prateek.dart:henson:%s", dartVersionName));
  }

  public void addNavigationArtifactToVariantConfiguration(
      String artifactName, Configuration internalConfiguration) {
    //we use the api configuration to make sure the resulting apk will contain the classes of the navigation jar.
    String configurationName = internalConfiguration.getName();
    Map<String, Object> map = new HashMap(2);
    map.put("path", project.getPath());
    map.put("configuration", artifactName);
    project.getDependencies().add(configurationName, project.getDependencies().project(map));
  }
}
