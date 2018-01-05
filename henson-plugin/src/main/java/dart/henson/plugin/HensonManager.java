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

package dart.henson.plugin;

import com.android.build.gradle.api.BaseVariant;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.Logger;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.SourceSet;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;

import dart.henson.plugin.internal.ArtifactManager;
import dart.henson.plugin.internal.ConfigurationManager;
import dart.henson.plugin.internal.DependencyManager;
import dart.henson.plugin.internal.SourceSetManager;
import dart.henson.plugin.internal.TaskManager;
import dart.henson.plugin.variant.NavigationVariant;
import dart.henson.plugin.variant.VariantManager;

public class HensonManager {
  private final Project project;
  public final Logger logger;
  public final ObjectFactory factory;
  public final VariantManager variantManager;
  public final TaskManager taskManager;
  public final ArtifactManager artifactManager;
  public final ConfigurationManager configurationManager;
  public final DependencyManager dependencyManager;
  public final SourceSetManager sourceSetManager;
  public final HensonPluginExtension hensonExtension;

  public HensonManager(Project project) {
    this.project = project;
    this.logger = project.getLogger();
    this.factory = project.getObjects();
    this.variantManager = new VariantManager(logger);
    this.artifactManager = new ArtifactManager(logger);
    this.configurationManager = new ConfigurationManager(project, logger);
    this.sourceSetManager = new SourceSetManager(project, logger);
    this.taskManager = new TaskManager(project, logger);
    this.dependencyManager = new DependencyManager(project, logger);
    this.hensonExtension = (HensonPluginExtension) project.getExtensions().getByName("henson");
  }

  /**
   * Creates a task to list all navigation source sets.
   */
  public void createListNavigationSourceSetsTask() {
    List<SourceSet> allNavigationSourceSets = sourceSetManager.getAllNavigationSourceSets();
    taskManager.createListSourceSetTask(allNavigationSourceSets);
  }

  /**
   * Creates the navigation configurations (navigation{Api, Implementation, etc.} and the navigation
   * source set. The configurations and the source set are "used" by a producer module, to represent
   * in gradle the navigation source tree containing the navigation models and its dependencies.
   * The configurations will be used to configure the dependencies needed to compile the navigation
   * source set.
   */
  public NavigationVariant createNavigationVariant(String dartVersionName) {
    Map<String, Configuration> mapSuffixToConfiguration = configurationManager.maybeCreateNavigationConfigurations();
    SourceSet sourceSet = sourceSetManager.maybeCreateNavigationSourceSet();

    NavigationVariant navigationVariant = variantManager.createNavigationVariant(sourceSet, mapSuffixToConfiguration);
    dependencyManager.addDartAndHensonDependenciesToNavigationConfigurations(navigationVariant, dartVersionName);
    taskManager.createNavigationCompilerAndJarTasks(navigationVariant);
    return navigationVariant;
  }

  public void createConsumableNavigationConfigurationAndArtifact(NavigationVariant navigationVariant) {
    Configuration navigationConfiguration = configurationManager.maybeCreateConsumableNavigationConfiguration();
    project.getArtifacts().add(navigationConfiguration.getName(), navigationVariant.jarTask);
  }

  public void createHensonNavigatorGenerationTask(BaseVariant variant) {
    if (hensonExtension == null || hensonExtension.getNavigatorPackageName() == null) {
      throw new InvalidParameterException(
              "The property 'henson.navigatorPackageName' must be defined in your build.gradle");
    }
    String hensonNavigatorPackageName = hensonExtension.getNavigatorPackageName();

    taskManager.createHensonNavigatorGenerationTask(variant, hensonNavigatorPackageName);
  }
}
