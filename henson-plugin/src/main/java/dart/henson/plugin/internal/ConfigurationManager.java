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

import static java.util.Arrays.asList;

import com.android.build.gradle.api.BaseVariant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.Logger;

/**
 * The plugin offers multiple configurations to interact with:
 *
 * <p>
 *
 * <ul>
 *   <li>navigation: used from a consumer project to include a navigation dependency towards a
 *       producer project. ex: <tt>
 *       <pre>
 *      consumer.dependencies {
 *          navigation project(':producer')
 *      }
 *  </pre>
 *       </tt>
 *   <li>__&lt;name&gt;Navigation: used internally to create a variant aware configuration on the
 *       navigation above. It is used to resolve, via attribute matching, the matching artifact from
 *       the consumer project. There is one such configuration per variant.
 *   <li>&lt;name&gtNavigation{Api, Implementation, CompileOnly, AnnotationProcessor}: these
 *       configurations are used in a producer project to add dependencies to the appropriate
 *       configuration. This configurations are used when compiling the navigation source set for a
 *       given variant. There is a such a configuration per variant, per build type, per product
 *       flavor and a main (name is empty) one. The configurations are taken into account in the
 *       same order as in the Gradle Android Plugin.
 *   <li>&lt;name&gtNavigationArtifact: a configuration that represents the artifact of a producer
 *       project. There is one such configuration per variant. The variant aware configuration has
 *       attibutes that will match __&lt;name&gt;Navigation from the consumer project.
 * </ul>
 */
public class ConfigurationManager {

  public static final String NAVIGATION_CONFIGURATION_SUFFIX_API = "Api";
  public static final String NAVIGATION_CONFIGURATION_SUFFIX_IMPLEMENTATION = "Implementation";
  public static final String NAVIGATION_CONFIGURATION_SUFFIX_COMPILE_ONLY = "CompileOnly";
  public static final String NAVIGATION_CONFIGURATION_SUFFIX_ANNOTATION_PROCESSOR =
      "AnnotationProcessor";
  private static final String NAVIGATION_CONFIGURATION = "navigation";
  private static final String NAVIGATION_CONFIGURATION_RADIX = "Navigation";
  private static final List<String> CONFIGURATION_SUFFIXES =
      asList(
          NAVIGATION_CONFIGURATION_SUFFIX_API,
          NAVIGATION_CONFIGURATION_SUFFIX_IMPLEMENTATION,
          NAVIGATION_CONFIGURATION_SUFFIX_COMPILE_ONLY,
          NAVIGATION_CONFIGURATION_SUFFIX_ANNOTATION_PROCESSOR);

  private Project project;
  private Logger logger;

  public ConfigurationManager(Project project, Logger logger) {
    this.project = project;
    this.logger = logger;
  }

  public Configuration createArtifactConfiguration(String artifactName) {
    Configuration artifactConfiguration = project.getConfigurations().create(artifactName);
    artifactConfiguration.setCanBeConsumed(true);
    artifactConfiguration.setCanBeResolved(false);
    return artifactConfiguration;
  }

  public Map<String, Configuration> maybeCreateNavigationConfigurations() {
    Map<String, Configuration> result = new HashMap<>(CONFIGURATION_SUFFIXES.size());
    CONFIGURATION_SUFFIXES.forEach(
        suffix -> {
          String configurationName = getConfigurationName(suffix);
          Configuration configuration = project.getConfigurations().maybeCreate(configurationName);
          configuration.setCanBeResolved(true);
          configuration.setCanBeConsumed(false);
          result.put(suffix, configuration);
        });
    return result;
  }

  public Configuration maybeCreateConsumableNavigationConfiguration() {
    Configuration consumableConfiguration =
            project.getConfigurations().findByName(NAVIGATION_CONFIGURATION);
    if (consumableConfiguration != null) {
      return consumableConfiguration;
    }
    consumableConfiguration = project.getConfigurations().create(NAVIGATION_CONFIGURATION);
    consumableConfiguration.setCanBeConsumed(true);
    //consumableConfiguration.setCanBeResolved(false);
    return consumableConfiguration;
  }

  private String getConfigurationName(String suffix) {
    return NAVIGATION_CONFIGURATION + suffix;
  }
}
